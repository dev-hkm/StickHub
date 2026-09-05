package com.hkm.stickhub.ime

import android.content.ClipDescription
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import coil.compose.AsyncImage
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerRepository
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
class StickerInputMethodService : InputMethodService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setContent {
                val app = applicationContext
                val stickers by repository.stickersFlow.collectAsState()
                StickHubTheme(
                    visualTheme = ThemePreferences.getVisualTheme(app),
                    darkTheme = ThemePreferences.resolveIsDark(
                        app,
                        ThemePreferences.getThemeMode(app)
                    )
                ) {
                    ImeStickerPanel(
                        stickers = stickers,
                        onInsert = ::insertSticker,
                        richInsertAvailable = editorSupportsRichInsert()
                    )
                }
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        editorInfoSnapshot = info
        editorMimes = info?.let { runCatching { EditorInfoCompat.getContentMimeTypes(it) }.getOrNull() }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        editorInfoSnapshot = null
        editorMimes = null
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    internal fun editorSupportsRichInsert(): Boolean {
        return StickerImeInsertController.editorSupportsImage(editorMimes)
    }

    private fun insertSticker(sticker: StickerItem, onStatus: (String) -> Unit) {
        serviceScope.launch {
            val payload = withContext(Dispatchers.IO) {
                StickerExportService.export(
                    applicationContext,
                    StickerExportService.ExportSource.LibraryFile(File(sticker.filePath)),
                    StickerExportService.ExportPurpose.IME
                )
            }
            val outcome = controller.insertSticker(payload)
            when {
                outcome.committed -> {
                    onStatus("Inserted")
                    serviceScope.launch {
                        runCatching { repository.recordUsage(sticker.id) }
                    }
                }
                outcome.fallbackUsed -> {
                    onStatus("Copied to clipboard instead")
                    serviceScope.launch {
                        runCatching { repository.recordUsage(sticker.id) }
                    }
                }
                outcome.ignoredDueToInflight -> Unit
                else -> onStatus("Couldn't insert this sticker")
            }
        }
    }

    private inner class FrameworkGateway : StickerImeInsertController.Gateway {
        override fun sdkInt(): Int = Build.VERSION.SDK_INT

        override fun editorContentMimes(): Array<String>? = editorMimes

        override fun inputConnection(): InputConnection? = currentInputConnection

        override fun commitImage(connection: InputConnection, uri: Uri, mimeType: String): Boolean {
            if (Build.VERSION.SDK_INT < StickerImeInsertController.MIN_COMMIT_SDK) return false
            val editor = editorInfoSnapshot ?: return false
            // Settle any in-progress composition first so the commit cannot
            // silently eat text the user was still typing.
            runCatching { connection.finishComposingText() }
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
    onInsert: (StickerItem, (String) -> Unit) -> Unit,
    richInsertAvailable: Boolean
) {
    var query by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    val visible = remember(stickers, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) stickers
        else stickers.filter {
            it.title.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.tags.lowercase().contains(q)
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (richInsertAvailable) {
                    "Tap a sticker to insert it into this app."
                } else {
                    "This app doesn't support direct insert — stickers will be copied instead."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search stickers") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(visible, key = { it.id }) { sticker ->
                    Surface(
                        onClick = { onInsert(sticker) { message -> status = message } },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        AsyncImage(
                            model = File(sticker.filePath),
                            contentDescription = sticker.title,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
            status?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
