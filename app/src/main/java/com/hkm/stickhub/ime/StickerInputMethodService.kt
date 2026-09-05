package com.hkm.stickhub.ime

import android.content.ClipDescription
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.platform.createLifecycleAwareWindowRecomposer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import coil.compose.AsyncImage
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.ui.theme.StickHubTheme
import com.hkm.stickhub.ui.theme.ThemePreferences
import com.hkm.stickhub.util.StickerExportService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
 * Opt-in StickHub Keyboard: the only public-API path that can insert rich
 * content straight into a compatible editor.
 *
 * Boundaries, all deliberate:
 * - Never enabled by default, never changes the user's keyboard, needs no
 *   AccessibilityService, no root, no network, no account.
 * - Application context only; repository singleton shared with the rest of
 *   the app (no second database); own coroutine scope cancelled in
 *   onDestroy; no Activity reference anywhere.
 * - One shared export implementation ([StickerExportService]); one commit
 *   attempt per tap; clipboard fallback exactly once and only after the
 *   commit path genuinely failed.
 * - A successful commit is NOT a promise of native sticker rendering — the
 *   target editor still decides photo vs sticker (see controller contract).
 */
class StickerInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val store by lazy { ViewModelStore() }
    override val viewModelStore: ViewModelStore get() = store

    private val savedStateRegistryController by lazy { SavedStateRegistryController.create(this) }
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var savedStateRestored = false
    @Volatile private var isServiceDestroyed = false

    private val sessionSequence = AtomicLong(1)
    @Volatile private var currentSessionSnapshot: StickerImeInsertController.InputSessionSnapshot? = null

    private val repository: StickerRepository by lazy {
        StickerRepository.getInstance(applicationContext)
    }

    private val controller: StickerImeInsertController by lazy {
        StickerImeInsertController(FrameworkGateway())
    }

    @Volatile
    private var editorMimes: Array<String>? = null

    @Volatile
    private var editorInfoSnapshot: EditorInfo? = null

    private var composeView: ComposeView? = null

    private fun ensureSavedStateRestored() {
        if (!savedStateRestored) {
            runCatching { savedStateRegistryController.performAttach() }
            runCatching { savedStateRegistryController.performRestore(null) }
            savedStateRestored = true
        }
    }

    private fun moveToLifecycleState(target: Lifecycle.State) {
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) return
        if (lifecycleRegistry.currentState == target) return

        if (target >= Lifecycle.State.CREATED) {
            ensureSavedStateRestored()
        }
        lifecycleRegistry.currentState = target
    }

    override fun onCreate() {
        super.onCreate()
        isServiceDestroyed = false
        moveToLifecycleState(Lifecycle.State.CREATED)
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        updateSessionSnapshot(attribute)
        moveToLifecycleState(Lifecycle.State.STARTED)
    }

    override fun onCreateInputView(): View {
        composeView?.disposeComposition()
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@StickerInputMethodService)
            setViewTreeViewModelStoreOwner(this@StickerInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@StickerInputMethodService)
            val recomposer = createLifecycleAwareWindowRecomposer(
                lifecycle = this@StickerInputMethodService.lifecycle
            )
            compositionContext = recomposer
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val app = applicationContext
                val stickers by repository.stickersFlow.collectAsState()
                val categories by repository.categoriesFlow.collectAsState()
                StickHubTheme(
                    visualTheme = ThemePreferences.getVisualTheme(app),
                    darkTheme = ThemePreferences.resolveIsDark(
                        app,
                        ThemePreferences.getThemeMode(app)
                    )
                ) {
                    ImeStickerPanel(
                        stickers = stickers,
                        categories = categories.map { it.name },
                        onInsert = ::insertSticker,
                        onSwitchKeyboard = ::switchKeyboard,
                        richInsertAvailable = editorSupportsRichInsert()
                    )
                }
            }
        }
        composeView = view
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        updateSessionSnapshot(info)
        moveToLifecycleState(Lifecycle.State.RESUMED)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        if (finishingInput) {
            currentSessionSnapshot = null
            editorInfoSnapshot = null
            editorMimes = null
        }
        moveToLifecycleState(Lifecycle.State.STARTED)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentSessionSnapshot = null
        editorInfoSnapshot = null
        editorMimes = null
        sessionSequence.incrementAndGet()
        moveToLifecycleState(Lifecycle.State.CREATED)
    }

    override fun onUnbindInput() {
        super.onUnbindInput()
        moveToLifecycleState(Lifecycle.State.CREATED)
    }

    override fun onDestroy() {
        moveToLifecycleState(Lifecycle.State.DESTROYED)
        isServiceDestroyed = true
        currentSessionSnapshot = null
        editorInfoSnapshot = null
        editorMimes = null
        composeView?.disposeComposition()
        composeView = null
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun updateSessionSnapshot(info: EditorInfo?) {
        editorInfoSnapshot = info
        val mimes = info?.let { runCatching { EditorInfoCompat.getContentMimeTypes(it) }.getOrNull() }
        editorMimes = mimes
        val nextId = sessionSequence.incrementAndGet()
        currentSessionSnapshot = StickerImeInsertController.InputSessionSnapshot(
            sessionId = nextId,
            packageName = info?.packageName,
            fieldId = info?.fieldId ?: 0,
            inputType = info?.inputType ?: 0,
            imeOptions = info?.imeOptions ?: 0,
            acceptedMimes = mimes,
            connection = currentInputConnection
        )
    }

    internal fun editorSupportsRichInsert(): Boolean {
        return StickerImeInsertController.editorSupportsImage(editorMimes)
    }

    private fun switchKeyboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val switched = runCatching { switchToNextInputMethod(false) }.getOrDefault(false)
            if (!switched) {
                showInputMethodPicker()
            }
        } else {
            showInputMethodPicker()
        }
    }

    private fun showInputMethodPicker() {
        runCatching {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }
    }

    private fun insertSticker(sticker: StickerItem, onResult: (message: String, success: Boolean) -> Unit) {
        val sessionAtTap = currentSessionSnapshot
        serviceScope.launch {
            val payload = withContext(Dispatchers.IO) {
                StickerExportService.export(
                    applicationContext,
                    StickerExportService.ExportSource.LibraryFile(File(sticker.filePath)),
                    StickerExportService.ExportPurpose.IME
                )
            }
            val outcome = controller.insertSticker(payload, sessionAtTap)
            when {
                outcome.committed -> {
                    onResult("Inserted", true)
                    serviceScope.launch {
                        runCatching { repository.recordUsage(sticker.id) }
                    }
                }
                outcome.fallbackUsed -> {
                    onResult("Copied to clipboard", true)
                    serviceScope.launch {
                        runCatching { repository.recordUsage(sticker.id) }
                    }
                }
                outcome.stale -> {
                    onResult("Input field changed", false)
                }
                outcome.ignoredDueToInflight -> Unit
                else -> onResult("Couldn't insert sticker", false)
            }
        }
    }

    private inner class FrameworkGateway : StickerImeInsertController.Gateway {
        override fun sdkInt(): Int = Build.VERSION.SDK_INT

        override fun editorContentMimes(): Array<String>? = editorMimes

        override fun inputConnection(): InputConnection? = currentInputConnection

        override fun currentSession(): StickerImeInsertController.InputSessionSnapshot? = currentSessionSnapshot

        override fun isServiceAlive(): Boolean = !isServiceDestroyed

        override fun commitImage(connection: InputConnection, uri: Uri, mimeType: String): Boolean {
            if (Build.VERSION.SDK_INT < StickerImeInsertController.MIN_COMMIT_SDK) return false
            val editor = editorInfoSnapshot ?: return false
            val description = ClipDescription("Sticker", arrayOf(mimeType))
            val content = InputContentInfoCompat(uri, description, null)
            return InputConnectionCompat.commitContent(
                connection,
                editor,
                content,
                InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION,
                Bundle()
            )
        }

        override fun fallbackCopy(uri: Uri, mimeType: String): Boolean {
            return try {
                val clipboard = applicationContext.getSystemService(CLIPBOARD_SERVICE)
                    as? android.content.ClipboardManager ?: return false
                clipboard.setPrimaryClip(
                    StickerExportService.buildClipData(applicationContext, uri, mimeType)
                )
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}

@Composable
private fun ImeStickerPanel(
    stickers: List<StickerItem>,
    categories: List<String>,
    onInsert: (StickerItem, (String, Boolean) -> Unit) -> Unit,
    onSwitchKeyboard: () -> Unit,
    richInsertAvailable: Boolean
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var inFlightId by remember { mutableStateOf<Long?>(null) }

    val haptics = rememberStickHubHaptics()

    val availableCategories = remember(categories, stickers) {
        val list = mutableListOf("All")
        val favoritesExist = stickers.any { it.isFavorite }
        if (favoritesExist) {
            list.add("Favorites")
        }
        val fromDb = categories.filter { it.isNotBlank() && it !in list }
        list.addAll(fromDb)
        stickers.forEach { s ->
            val cat = s.category.trim()
            if (cat.isNotEmpty() && cat !in list) {
                list.add(cat)
            }
        }
        list
    }

    val visible = remember(stickers, query, selectedCategory) {
        val q = query.trim().lowercase()
        stickers.filter { item ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> item.isFavorite
                else -> item.category.equals(selectedCategory, ignoreCase = true)
            }
            if (!matchesCategory) return@filter false

            if (q.isEmpty()) true
            else {
                item.title.lowercase().contains(q) ||
                    item.category.lowercase().contains(q) ||
                    item.tags.lowercase().contains(q)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Header Row: Status badge, message, switch keyboard button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (richInsertAvailable) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (richInsertAvailable) LucideR.drawable.lucide_ic_check
                                else LucideR.drawable.lucide_ic_copy
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = if (richInsertAvailable) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = if (richInsertAvailable) "Direct Insert" else "Copy mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (richInsertAvailable) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                AnimatedVisibility(
                    visible = statusMessage != null,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    statusMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        haptics.performTick()
                        onSwitchKeyboard()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_keyboard),
                        contentDescription = "Switch keyboard",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar with Clear Button
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        "Search stickers",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_search),
                        contentDescription = "Search",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                query = ""
                                haptics.performTick()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_x),
                                contentDescription = "Clear",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp)
                    .padding(vertical = 2.dp)
            )

            // Category Chips Rail
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(availableCategories, key = { it }) { category ->
                    val isSelected = category == selectedCategory
                    val iconRes = when (category) {
                        "All" -> LucideR.drawable.lucide_ic_layers
                        "Favorites" -> LucideR.drawable.lucide_ic_heart
                        else -> LucideR.drawable.lucide_ic_folder
                    }
                    Surface(
                        onClick = {
                            if (!isSelected) {
                                haptics.performTick()
                                selectedCategory = category
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            // Adaptive Grid / Empty State
            if (stickers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_image),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No stickers in StickHub yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (visible.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_search),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No stickers found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 68.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(visible, key = { it.id }) { sticker ->
                        val inserting = inFlightId == sticker.id
                        Surface(
                            onClick = {
                                if (inFlightId == null) {
                                    inFlightId = sticker.id
                                    onInsert(sticker) { message, success ->
                                        inFlightId = null
                                        statusMessage = message
                                        if (success) {
                                            haptics.performConfirm()
                                        } else {
                                            haptics.performReject()
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.aspectRatio(1f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = File(sticker.filePath),
                                    contentDescription = sticker.title,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                                if (inserting) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
