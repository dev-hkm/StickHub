package com.hkm.stickhub.ui

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import android.os.SystemClock
import androidx.compose.material3.ButtonDefaults
import com.hkm.stickhub.service.OverlayStartFilterMode
import com.hkm.stickhub.service.OverlayAfterCopyAction
import com.hkm.stickhub.service.QuickStickersOnboardingPolicy

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.ClipboardImportResult
import com.hkm.stickhub.data.repository.StickerOrderPolicy
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.service.OverlayPreferences
import com.hkm.stickhub.service.OverlayService
import com.hkm.stickhub.ui.components.AddCategoryDialog
import com.hkm.stickhub.ui.components.CategoryChips
import com.hkm.stickhub.ui.components.CategoryManagementScreen
import com.hkm.stickhub.ui.components.CheckerboardBackground
import com.hkm.stickhub.ui.components.ClipboardImportSheet
import com.hkm.stickhub.ui.components.CompactStickerCard
import com.hkm.stickhub.ui.components.LargeStickerCard
import com.hkm.stickhub.ui.components.SettingsScreen
import com.hkm.stickhub.ui.components.StickerCard
import com.hkm.stickhub.ui.components.StickerDetailBottomSheet
import com.hkm.stickhub.ui.components.StickerListItem
import com.hkm.stickhub.ui.components.StickerStudioBottomSheet
import com.hkm.stickhub.ui.components.SubjectCutoutSheet
import com.hkm.stickhub.ui.components.TopSearchBar
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.ui.library.StickerLibraryLayoutPickerSheet
import com.hkm.stickhub.ui.library.StickerLibraryPreferences
import com.hkm.stickhub.ui.library.LibrarySnapshotState
import com.hkm.stickhub.ui.library.LibraryStartupRefreshPolicy
import com.hkm.stickhub.ui.library.StickerLibraryViewMode
import com.hkm.stickhub.ui.theme.AppThemeMode
import com.hkm.stickhub.ui.theme.AppVisualTheme
import com.hkm.stickhub.ui.theme.specimenDecor
import com.hkm.stickhub.ui.theme.BotanicalLeafMotif
import com.hkm.stickhub.ui.theme.ThemePreferences
import com.hkm.stickhub.ui.theme.AuroraRibbonMotif
import com.hkm.stickhub.ui.theme.SynthSunMotif
import com.hkm.stickhub.ui.theme.DecoSunMotif
import com.hkm.stickhub.ui.theme.WaveMotif
import com.hkm.stickhub.ui.theme.PixelInvaderMotif
import com.hkm.stickhub.ui.theme.KawaiiCloudMotif
import com.hkm.stickhub.ui.theme.SolarLeafMotif
import com.hkm.stickhub.ui.theme.NoirLampMotif
import com.hkm.stickhub.ui.theme.GlassDropletMotif
import com.hkm.stickhub.ui.theme.NouveauBloomMotif
import com.hkm.stickhub.ui.theme.CottageRoseMotif
import com.hkm.stickhub.ui.theme.StarbasePlanetMotif
import com.hkm.stickhub.ui.theme.AtelierFrameMotif
import com.hkm.stickhub.ui.theme.PressFrontPageMotif
import com.hkm.stickhub.ui.theme.OldMoneySealMotif
import com.hkm.stickhub.ui.theme.NeoStickerMotif
import com.hkm.stickhub.ui.theme.SketchDoodleMotif
import com.hkm.stickhub.ui.theme.StickHubMotion
import com.hkm.stickhub.util.BackupHelper
import com.hkm.stickhub.util.ClipboardHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppRoute {
    LIBRARY,
    SETTINGS,
    CATEGORY_MANAGEMENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickHubApp(
    repository: StickerRepository,
    incomingSharedUri: Uri? = null,
    onClearSharedUri: () -> Unit = {},
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    visualTheme: AppVisualTheme = AppVisualTheme.DEFAULT,
    onVisualThemeChange: (AppVisualTheme) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appFocusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = rememberStickHubHaptics()

    val allStickers by repository.stickersFlow.collectAsState()
    val categories by repository.categoriesFlow.collectAsState()
    val startupRefreshPolicy = remember(repository) { LibraryStartupRefreshPolicy() }
    var librarySnapshotState by remember(repository) {
        mutableStateOf<LibrarySnapshotState>(LibrarySnapshotState.Loading)
    }
    var libraryRefreshAttempt by remember(repository) { mutableStateOf(0) }

    // Repository snapshots begin empty until the first disk read. Never render that transient
    // snapshot as an empty library: the overlay owns a separate repository instance and may have
    // already loaded the same on-device sticker files.
    LaunchedEffect(repository, libraryRefreshAttempt) {
        if (libraryRefreshAttempt == 0 && !startupRefreshPolicy.claimInitialRefresh()) return@LaunchedEffect
        librarySnapshotState = LibrarySnapshotState.Loading
        librarySnapshotState = try {
            repository.refresh()
            LibrarySnapshotState.Ready
        } catch (error: Exception) {
            LibrarySnapshotState.Failed(
                error.localizedMessage ?: "Could not load your on-device sticker library."
            )
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }

    // Navigation Controller and Route
    val navigator = remember { AppNavigator(initialRoute = AppRoute.LIBRARY) }
    var currentRoute by rememberSaveable { mutableStateOf(AppRoute.LIBRARY) }

    // Re-sync the controller with the restored route after process death.
    LaunchedEffect(Unit) {
        navigator.restoreTo(currentRoute)
    }

    LaunchedEffect(currentRoute) {
        delay(320)
        navigator.onTransitionSettled()
    }

    // Multi-Select Mode
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedStickerIds by remember { mutableStateOf(setOf<Long>()) }
    var showBatchMoveMenu by remember { mutableStateOf(false) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    // Detail & Studio BottomSheets
    var selectedStickerForDetail by remember { mutableStateOf<StickerItem?>(null) }
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedStickerForStudio by remember { mutableStateOf<StickerItem?>(null) }
    val studioSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Subject Cutout Sheet
    var activeCutoutUri by remember { mutableStateOf<Uri?>(null) }
    val cutoutSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target -> target != SheetValue.Hidden }
    )

    // Library View Mode
    var libraryViewMode by remember {
        mutableStateOf(StickerLibraryPreferences.getViewMode(context))
    }
    val libraryLayoutPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showLibraryLayoutPicker by remember { mutableStateOf(false) }
    var pendingLibraryLayoutMode by remember { mutableStateOf<StickerLibraryViewMode?>(null) }
    var isDismissingLibraryPicker by remember { mutableStateOf(false) }

    fun dismissLibraryLayoutPicker() {
        if (isDismissingLibraryPicker) return
        isDismissingLibraryPicker = true
        scope.launch {
            try {
                libraryLayoutPickerSheetState.hide()
            } finally {
                showLibraryLayoutPicker = false
                pendingLibraryLayoutMode = null
                isDismissingLibraryPicker = false
            }
        }
    }

    fun selectLibraryLayoutMode(newMode: StickerLibraryViewMode) {
        if (isDismissingLibraryPicker) return
        if (newMode == libraryViewMode) {
            dismissLibraryLayoutPicker()
            return
        }
        pendingLibraryLayoutMode = newMode
        isDismissingLibraryPicker = true
        haptics.performCopyAck()
        scope.launch {
            try {
                libraryLayoutPickerSheetState.hide()
            } finally {
                showLibraryLayoutPicker = false
                pendingLibraryLayoutMode = null
                isDismissingLibraryPicker = false
                libraryViewMode = newMode
                StickerLibraryPreferences.setViewMode(context, newMode)
            }
        }
    }

    // Library Visibility Preferences
    var showLibrarySearch by remember {
        mutableStateOf(StickerLibraryPreferences.isSearchVisible(context))
    }
    var showLibraryCategoryFilters by remember {
        mutableStateOf(StickerLibraryPreferences.isCategoryFiltersVisible(context))
    }

    val onToggleShowSearch: (Boolean) -> Unit = { visible ->
        showLibrarySearch = visible
        StickerLibraryPreferences.setSearchVisible(context, visible)
        if (!visible && searchQuery.isNotEmpty()) {
            searchQuery = ""
        }
    }

    val onToggleShowCategoryFilters: (Boolean) -> Unit = { visible ->
        showLibraryCategoryFilters = visible
        StickerLibraryPreferences.setCategoryFiltersVisible(context, visible)
        if (!visible && selectedCategory != "All") {
            selectedCategory = "All"
        }
    }

    // Back handlers
    BackHandler(enabled = showLibraryLayoutPicker) {
        dismissLibraryLayoutPicker()
    }
    BackHandler(enabled = currentRoute == AppRoute.CATEGORY_MANAGEMENT) {
        if (navigator.requestPop()) {
            currentRoute = navigator.currentRoute
        }
    }
    BackHandler(enabled = currentRoute == AppRoute.SETTINGS) {
        if (navigator.requestPop()) {
            currentRoute = navigator.currentRoute
        }
    }
    BackHandler(enabled = currentRoute == AppRoute.LIBRARY && isSelectionMode) {
        isSelectionMode = false
        selectedStickerIds = emptySet()
    }

    // Dialogs
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryItem?>(null) }
    var showCreateSourceDialog by remember { mutableStateOf(false) }

    var isOverlayRunning by remember { mutableStateOf(OverlayService.isRunning) }
    var overlayBubbleSizeDp by remember { mutableStateOf(OverlayPreferences.bubbleSizeDp(context)) }
    var overlayBubbleOpacity by remember { mutableFloatStateOf(OverlayPreferences.bubbleOpacity(context)) }
    var popupMasterOpacity by remember { mutableFloatStateOf(OverlayPreferences.popupMasterOpacity(context)) }
    var popupSurfaceOpacity by remember { mutableFloatStateOf(OverlayPreferences.popupSurfaceOpacity(context)) }
    var popupStickersOpacity by remember { mutableFloatStateOf(OverlayPreferences.popupStickersOpacity(context)) }
    var popupChromeOpacity by remember { mutableFloatStateOf(OverlayPreferences.popupChromeOpacity(context)) }
    var popupCloseOpacity by remember { mutableFloatStateOf(OverlayPreferences.popupCloseOpacity(context)) }
    var popupResizeOpacity by remember { mutableFloatStateOf(OverlayPreferences.popupResizeOpacity(context)) }
    var stickerShadowStrength by remember { mutableFloatStateOf(OverlayPreferences.stickerShadowStrength(context)) }
    var startFilterMode by remember { mutableStateOf(OverlayPreferences.startFilterMode(context)) }
    var startCustomCategory by remember { mutableStateOf(OverlayPreferences.startCustomCategory(context)) }
    var afterCopyAction by remember { mutableStateOf(OverlayPreferences.afterCopyAction(context)) }
    var showQuickStickersTitle by remember { mutableStateOf(OverlayPreferences.showTitle(context)) }
    var showQuickStickersSearch by remember { mutableStateOf(OverlayPreferences.showSearch(context)) }
    var showQuickStickersCategories by remember { mutableStateOf(OverlayPreferences.showCategories(context)) }

    var showOnboarding by remember {
        mutableStateOf(
            QuickStickersOnboardingPolicy.isOnboardingEligible(
                context = context,
                isOverlayRunning = OverlayService.isRunning,
                hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true,
                hasActiveModalOrFlow = false
            )
        )
    }

    fun sendLightweightOverlayUpdate() {
        if (OverlayService.isRunning) {
            context.startService(
                Intent(context, OverlayService::class.java).setAction(OverlayService.ACTION_UPDATE_APPEARANCE)
            )
        }
    }

    // Throttled variant for slider drag ticks: the service path is now cheap
    // (alpha-only, no grid rebuild), but binder + prefs writes every frame
    // still jank. Live ticks update at most every 120ms; release always forces.
    var lastOverlayUpdateMs by remember { mutableLongStateOf(0L) }
    fun sendThrottledOverlayUpdate(force: Boolean = false) {
        val now = SystemClock.elapsedRealtime()
        if (force || now - lastOverlayUpdateMs >= 120L) {
            lastOverlayUpdateMs = now
            sendLightweightOverlayUpdate()
        }
    }

    fun sendShadowOverlayUpdate() {
        if (OverlayService.isRunning) {
            context.startService(
                Intent(context, OverlayService::class.java).setAction(OverlayService.ACTION_UPDATE_SHADOW)
            )
        }
    }

    /** Shows a snackbar, dismissing any currently-visible one first so rapid
     * actions never pile up a queue. */
    suspend fun flashSnackbar(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message = message, duration = duration)
    }

    fun revealOverlayControls() {
        if (OverlayService.isRunning) {
            context.startService(
                Intent(context, OverlayService::class.java).setAction(OverlayService.ACTION_REVEAL_CONTROLS)
            )
        }
    }
    var clipboardImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var lastClipboardStamp by remember { mutableLongStateOf(0L) }
    var showClipboardPicker by remember { mutableStateOf(false) }
    var recentlyCopiedId by remember { mutableStateOf<Long?>(null) }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            activeCutoutUri = uri
        }
    }

    // Handle incoming shared image
    LaunchedEffect(incomingSharedUri) {
        incomingSharedUri?.let { uri ->
            activeCutoutUri = uri
            onClearSharedUri()
        }
    }

    // Periodic check for clipboard image changes. The clip timestamp tells a
    // fresh copy apart from the one already handled — even when the URI is
    // identical — so a saved/dismissed image never pops back up on its own.
    LaunchedEffect(Unit) {
        while (true) {
            val (uris, stamp) = withContext(Dispatchers.IO) {
                ClipboardHelper.getClipboardImagesStamped(context)
            }
            if (stamp != lastClipboardStamp) {
                lastClipboardStamp = stamp
                // Freeze the offer while the picker sheet is open.
                if (!showClipboardPicker) {
                    clipboardImageUris = uris
                }
            }
            delay(2000)
        }
    }

    /** Clears the current clipboard offer (after import or dismiss). */
    fun consumeClipboardOffer() {
        clipboardImageUris = emptyList()
        showClipboardPicker = false
    }

    fun importClipboardStickers(uris: List<Uri>) {
        if (uris.isEmpty()) return
        scope.launch {
            var saved = 0
            var duplicates = 0
            var failed = 0
            uris.forEach { uri ->
                when (repository.importClipboardSticker(uri)) {
                    is ClipboardImportResult.Saved -> saved++
                    is ClipboardImportResult.Duplicate -> duplicates++
                    is ClipboardImportResult.OwnSource -> { /* filtered upstream */ }
                    else -> failed++
                }
            }
            consumeClipboardOffer()
            when {
                saved > 0 -> {
                    haptics.performConfirm()
                    val extra = buildList {
                        if (duplicates > 0) add("$duplicates duplicate${if (duplicates > 1) "s" else ""} skipped")
                        if (failed > 0) add("$failed failed")
                    }.joinToString(" • ")
                    flashSnackbar(
                        (if (saved == 1) "Sticker saved to library!" else "$saved stickers saved to library!") +
                            (if (extra.isNotEmpty()) " ($extra)" else "")
                    )
                }
                duplicates > 0 -> {
                    haptics.performTap()
                    flashSnackbar("Already in your library.")
                }
                else -> {
                    haptics.performReject()
                    flashSnackbar("Couldn't import clipboard images.")
                }
            }
        }
    }

    // Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val success = BackupHelper.exportBackup(context, uri, allStickers, categories)
                if (success) {
                    haptics.performConfirm()
                    flashSnackbar("Backup exported successfully!")
                } else {
                    haptics.performReject()
                    flashSnackbar("Failed to export backup!")
                }
            }
        }
    }

    // Import Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val count = BackupHelper.importBackup(context, uri, repository)
                if (count > 0) {
                    haptics.performConfirm()
                    flashSnackbar("Merged $count stickers from backup!")
                } else {
                    haptics.performReject()
                    flashSnackbar("No valid stickers found in backup!")
                }
            }
        }
    }

    // Filter stickers based on selected category and search query
    val filteredStickers = remember(allStickers, selectedCategory, searchQuery) {
        allStickers.filter { sticker ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> sticker.isFavorite
                "Frequent" -> sticker.usageCount > 0
                else -> sticker.category.equals(selectedCategory, ignoreCase = true)
            }
            val query = searchQuery.trim().lowercase()
            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                sticker.title.lowercase().contains(query) ||
                    sticker.tags.lowercase().contains(query) ||
                    sticker.category.lowercase().contains(query)
            }
            matchesCategory && matchesQuery
        }.let { list ->
            if (selectedCategory == "Frequent") {
                list.sortedByDescending { it.usageCount }
            } else {
                list
            }
        }
    }

    val canReorderStickers = !isSelectionMode && selectedCategory == "All" && searchQuery.isBlank()
    val libraryGridState = rememberLazyGridState()
    val autoScrollEdgePx = with(LocalDensity.current) { 64.dp.toPx() }
    val gridFilterAlpha = remember { Animatable(1f) }
    var reorderPreview by remember { mutableStateOf<List<StickerItem>?>(null) }
    var draggedStickerId by remember { mutableStateOf<Long?>(null) }
    var draggedIndex by remember { mutableStateOf(-1) }
    var draggedOffset by remember { mutableStateOf(Offset.Zero) }
    var didReorderStickers by remember { mutableStateOf(false) }

    LaunchedEffect(allStickers, canReorderStickers) {
        if (!canReorderStickers || draggedStickerId == null) {
            reorderPreview = null
        }
    }
    LaunchedEffect(selectedCategory, searchQuery) {
        gridFilterAlpha.snapTo(0.90f)
        gridFilterAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(StickHubMotion.DurationShort, easing = StickHubMotion.EasingEmphasizedDecelerate)
        )
    }
    val displayedStickers = if (canReorderStickers) reorderPreview ?: filteredStickers else filteredStickers

    fun toggleOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            scope.launch {
                flashSnackbar("Please grant 'Display over other apps' permission")
            }
        } else {
            val serviceIntent = Intent(context, OverlayService::class.java)
            if (OverlayService.isRunning) {
                context.stopService(serviceIntent)
                isOverlayRunning = false
                haptics.performCopyAck()
                scope.launch { flashSnackbar("Floating overlay disabled") }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                isOverlayRunning = true
                haptics.performCopyAck()
                scope.launch { flashSnackbar("Floating overlay active") }
            }
        }
    }

    val onItemClick: (StickerItem) -> Unit = { item ->
        if (isSelectionMode) {
            val isSelected = selectedStickerIds.contains(item.id)
            selectedStickerIds = if (isSelected) {
                selectedStickerIds - item.id
            } else {
                selectedStickerIds + item.id
            }
        } else {
            val copied = ClipboardHelper.copyStickerToClipboard(context, item)
            if (copied) {
                haptics.performConfirm()
                recentlyCopiedId = item.id
                scope.launch {
                    repository.recordUsage(item.id)
                    if (ClipboardHelper.shouldShowCopiedConfirmation()) {
                        flashSnackbar(
                            message = "Copied to clipboard!",
                            duration = SnackbarDuration.Short
                        )
                    }
                    delay(650)
                    if (recentlyCopiedId == item.id) {
                        recentlyCopiedId = null
                    }
                }
            } else {
                haptics.performReject()
            }
        }
    }

    val onItemLongClick: (StickerItem) -> Unit = { item ->
        if (isSelectionMode) {
            selectedStickerForDetail = item
        } else {
            haptics.performLongPress()
            isSelectionMode = true
            selectedStickerIds = setOf(item.id)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Base stationary Library route (zIndex = 0f)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
                .specimenDecor(
                    visualTheme = visualTheme,
                    isDark = ThemePreferences.resolveIsDark(context, themeMode)
                ),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                        floatingActionButton = {
                            if (!isSelectionMode) {
                                Row(
                                    modifier = Modifier.navigationBarsPadding(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            isSelectionMode = true
                                        },
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ) {
                                        Icon(
                                            painter = painterResource(LucideR.drawable.lucide_ic_list_checks),
                                            contentDescription = "Select Mode",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    FloatingActionButton(
                                        onClick = {
                                            haptics.performTap()
                                            scope.launch {
                                                val (uris, stamp) = withContext(Dispatchers.IO) {
                                                    ClipboardHelper.getClipboardImagesStamped(context)
                                                }
                                                lastClipboardStamp = stamp
                                                clipboardImageUris = uris
                                                showCreateSourceDialog = true
                                            }
                                        },
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Icon(
                                            painter = painterResource(LucideR.drawable.lucide_ic_plus),
                                            contentDescription = "Create sticker",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                        val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        val listBottomPadding = navBarBottomPadding + 96.dp

                        AnimatedContent(
                            targetState = libraryViewMode,
                            transitionSpec = {
                                fadeIn(tween(StickHubMotion.DurationShort)) togetherWith fadeOut(tween(StickHubMotion.DurationShort))
                            },
                            label = "library_view_mode"
                        ) { viewMode ->
                            when (viewMode) {
                                StickerLibraryViewMode.COMPACT_GRID -> {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = listBottomPadding),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { alpha = gridFilterAlpha.value }
                                    ) {
                                        item(key = "library_headers", span = { GridItemSpan(maxLineSpan) }) {
                                            LibraryHeadersContent(
                                                isSelectionMode = isSelectionMode,
                                                selectedStickerCount = selectedStickerIds.size,
                                                totalFilteredCount = filteredStickers.size,
                                                onCancelSelection = {
                                                    isSelectionMode = false
                                                    selectedStickerIds = emptySet()
                                                },
                                                onSelectAll = {
                                                    selectedStickerIds = if (selectedStickerIds.size == filteredStickers.size) {
                                                        emptySet()
                                                    } else {
                                                        filteredStickers.map { it.id }.toSet()
                                                    }
                                                },
                                                onFavoriteBatch = {
                                                    scope.launch {
                                                        repository.batchToggleFavorite(selectedStickerIds.toList(), true)
                                                        haptics.performConfirm()
                                                        flashSnackbar("Added to favorites")
                                                        isSelectionMode = false
                                                        selectedStickerIds = emptySet()
                                                    }
                                                },
                                                showBatchMoveMenu = showBatchMoveMenu,
                                                onToggleBatchMoveMenu = { showBatchMoveMenu = it },
                                                categories = categories,
                                                onBatchSetCategory = { catName ->
                                                    scope.launch {
                                                        repository.batchSetCategory(selectedStickerIds.toList(), catName)
                                                        haptics.performConfirm()
                                                        flashSnackbar("Moved to $catName")
                                                        isSelectionMode = false
                                                        selectedStickerIds = emptySet()
                                                    }
                                                },
                                                onBatchDelete = { showBatchDeleteConfirm = true },
                                                showLibrarySearch = showLibrarySearch,
                                                searchQuery = searchQuery,
                                                onSearchQueryChange = { searchQuery = it },
                                                isOverlayRunning = isOverlayRunning,
                                                onToggleOverlay = { toggleOverlay() },
                                                onOpenSettings = { if (navigator.requestPush(AppRoute.SETTINGS)) currentRoute = navigator.currentRoute },
                                                libraryViewMode = libraryViewMode,
                                                onOpenLayoutPicker = { showLibraryLayoutPicker = true },
                                                filteredStickersCount = filteredStickers.size,
                                                showLibraryCategoryFilters = showLibraryCategoryFilters,
                                                selectedCategory = selectedCategory,
                                                onSelectCategory = { selectedCategory = it },
                                                onAddCategoryClick = { showAddCategoryDialog = true },
                                                onCategoryLongClick = { cat -> categoryToDelete = cat },
                                                clipboardImageUris = clipboardImageUris,
                                                onImportClipboard = {
                                                    if (clipboardImageUris.size == 1) {
                                                        importClipboardStickers(clipboardImageUris)
                                                    } else {
                                                        showClipboardPicker = true
                                                    }
                                                },
                                                onDismissClipboard = { consumeClipboardOffer() },
                                                appFocusManager = appFocusManager,
                                                showQuickStickersOnboarding = showOnboarding,
                                                onEnableQuickStickers = {
                                                    QuickStickersOnboardingPolicy.markCompletedOrDismissed(context)
                                                    showOnboarding = false
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                                        val intent = Intent(
                                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                            Uri.parse("package:${context.packageName}")
                                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                                        context.startActivity(intent)
                                                        scope.launch {
                                                            flashSnackbar("Please grant 'Display over other apps' to activate Quick Stickers")
                                                        }
                                                    } else {
                                                        val serviceIntent = Intent(context, OverlayService::class.java)
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            context.startForegroundService(serviceIntent)
                                                        } else {
                                                            context.startService(serviceIntent)
                                                        }
                                                        isOverlayRunning = true
                                                        haptics.performConfirm()
                                                        scope.launch {
                                                            flashSnackbar("Quick Stickers activated!")
                                                        }
                                                    }
                                                },
                                                onDismissQuickStickers = {
                                                    QuickStickersOnboardingPolicy.markCompletedOrDismissed(context)
                                                    showOnboarding = false
                                                    haptics.performTap()
                                                }
                                            )
                                        }

                                        if (librarySnapshotState is LibrarySnapshotState.Loading) {
                                            item(key = "library_loading", span = { GridItemSpan(maxLineSpan) }) {
                                                LibraryLoadingView()
                                            }
                                        } else if (librarySnapshotState is LibrarySnapshotState.Failed) {
                                            item(key = "library_load_failed", span = { GridItemSpan(maxLineSpan) }) {
                                                LibraryLoadFailureView(
                                                    onRetry = { libraryRefreshAttempt += 1 }
                                                )
                                            }
                                        } else if (displayedStickers.isEmpty()) {
                                            item(key = "empty_library", span = { GridItemSpan(maxLineSpan) }) {
                                                EmptyLibraryView(searchQuery = searchQuery, visualTheme = visualTheme)
                                            }
                                        } else {
                                            items(displayedStickers, key = { it.id }) { sticker ->
                                                CompactStickerCard(
                                                    sticker = sticker,
                                                    isSelectionMode = isSelectionMode,
                                                    isSelected = selectedStickerIds.contains(sticker.id),
                                                    showCopiedBadge = recentlyCopiedId == sticker.id,
                                                    onClick = onItemClick,
                                                    onLongClick = onItemLongClick
                                                )
                                            }
                                        }
                                    }
                                }

                                StickerLibraryViewMode.STANDARD_GRID -> {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        state = libraryGridState,
                                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = listBottomPadding),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { alpha = gridFilterAlpha.value }
                                    ) {
                                        item(key = "library_headers", span = { GridItemSpan(maxLineSpan) }) {
                                            LibraryHeadersContent(
                                                isSelectionMode = isSelectionMode,
                                                selectedStickerCount = selectedStickerIds.size,
                                                totalFilteredCount = filteredStickers.size,
                                                onCancelSelection = {
                                                    isSelectionMode = false
                                                    selectedStickerIds = emptySet()
                                                },
                                                onSelectAll = {
                                                    selectedStickerIds = if (selectedStickerIds.size == filteredStickers.size) {
                                                        emptySet()
                                                    } else {
                                                        filteredStickers.map { it.id }.toSet()
                                                    }
                                                },
                                                onFavoriteBatch = {
                                                    scope.launch {
                                                        repository.batchToggleFavorite(selectedStickerIds.toList(), true)
                                                        haptics.performConfirm()
                                                        flashSnackbar("Added to favorites")
                                                        isSelectionMode = false
                                                        selectedStickerIds = emptySet()
                                                    }
                                                },
                                                showBatchMoveMenu = showBatchMoveMenu,
                                                onToggleBatchMoveMenu = { showBatchMoveMenu = it },
                                                categories = categories,
                                                onBatchSetCategory = { catName ->
                                                    scope.launch {
                                                        repository.batchSetCategory(selectedStickerIds.toList(), catName)
                                                        haptics.performConfirm()
                                                        flashSnackbar("Moved to $catName")
                                                        isSelectionMode = false
                                                        selectedStickerIds = emptySet()
                                                    }
                                                },
                                                onBatchDelete = { showBatchDeleteConfirm = true },
                                                showLibrarySearch = showLibrarySearch,
                                                searchQuery = searchQuery,
                                                onSearchQueryChange = { searchQuery = it },
                                                isOverlayRunning = isOverlayRunning,
                                                onToggleOverlay = { toggleOverlay() },
                                                onOpenSettings = { if (navigator.requestPush(AppRoute.SETTINGS)) currentRoute = navigator.currentRoute },
                                                libraryViewMode = libraryViewMode,
                                                onOpenLayoutPicker = { showLibraryLayoutPicker = true },
                                                filteredStickersCount = filteredStickers.size,
                                                showLibraryCategoryFilters = showLibraryCategoryFilters,
                                                selectedCategory = selectedCategory,
                                                onSelectCategory = { selectedCategory = it },
                                                onAddCategoryClick = { showAddCategoryDialog = true },
                                                onCategoryLongClick = { cat -> categoryToDelete = cat },
                                                clipboardImageUris = clipboardImageUris,
                                                onImportClipboard = {
                                                    if (clipboardImageUris.size == 1) {
                                                        importClipboardStickers(clipboardImageUris)
                                                    } else {
                                                        showClipboardPicker = true
                                                    }
                                                },
                                                onDismissClipboard = { consumeClipboardOffer() },
                                                appFocusManager = appFocusManager,
                                                showQuickStickersOnboarding = showOnboarding,
                                                onEnableQuickStickers = {
                                                    QuickStickersOnboardingPolicy.markCompletedOrDismissed(context)
                                                    showOnboarding = false
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                                        val intent = Intent(
                                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                            Uri.parse("package:${context.packageName}")
                                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                                        context.startActivity(intent)
                                                        scope.launch {
                                                            flashSnackbar("Please grant 'Display over other apps' to activate Quick Stickers")
                                                        }
                                                    } else {
                                                        val serviceIntent = Intent(context, OverlayService::class.java)
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            context.startForegroundService(serviceIntent)
                                                        } else {
                                                            context.startService(serviceIntent)
                                                        }
                                                        isOverlayRunning = true
                                                        haptics.performConfirm()
                                                        scope.launch {
                                                            flashSnackbar("Quick Stickers activated!")
                                                        }
                                                    }
                                                },
                                                onDismissQuickStickers = {
                                                    QuickStickersOnboardingPolicy.markCompletedOrDismissed(context)
                                                    showOnboarding = false
                                                    haptics.performTap()
                                                }
                                            )
                                        }

                                        if (librarySnapshotState is LibrarySnapshotState.Loading) {
                                            item(key = "library_loading", span = { GridItemSpan(maxLineSpan) }) {
                                                LibraryLoadingView()
                                            }
                                        } else if (librarySnapshotState is LibrarySnapshotState.Failed) {
                                            item(key = "library_load_failed", span = { GridItemSpan(maxLineSpan) }) {
                                                LibraryLoadFailureView(
                                                    onRetry = { libraryRefreshAttempt += 1 }
                                                )
                                            }
                                        } else if (displayedStickers.isEmpty()) {
                                            item(key = "empty_library", span = { GridItemSpan(maxLineSpan) }) {
                                                EmptyLibraryView(searchQuery = searchQuery, visualTheme = visualTheme)
                                            }
                                        } else {
                                            items(displayedStickers, key = { it.id }) { sticker ->
                                                StickerCard(
                                                    sticker = sticker,
                                                    onClick = onItemClick,
                                                    onLongClick = onItemLongClick,
                                                    onReorderStart = if (canReorderStickers) {
                                                        { heldSticker: StickerItem ->
                                                            val activeStickers = reorderPreview ?: allStickers
                                                            val sourceIndex = activeStickers.indexOfFirst { it.id == heldSticker.id }
                                                            if (sourceIndex >= 0) {
                                                                haptics.performTap()
                                                                reorderPreview = activeStickers
                                                                draggedStickerId = heldSticker.id
                                                                draggedIndex = sourceIndex
                                                                draggedOffset = Offset.Zero
                                                                didReorderStickers = false
                                                            }
                                                        }
                                                    } else null,
                                                    onReorderDrag = if (canReorderStickers) {
                                                        { dragAmount: Offset ->
                                                            if (draggedStickerId == sticker.id && draggedIndex in displayedStickers.indices) {
                                                                draggedOffset += dragAmount
                                                                val activeStickers = reorderPreview ?: allStickers
                                                                val currentInfo = libraryGridState.layoutInfo.visibleItemsInfo
                                                                    .firstOrNull { it.index == draggedIndex + 1 }

                                                                if (currentInfo != null) {
                                                                    val draggedCenterX = currentInfo.offset.x + (currentInfo.size.width / 2f) + draggedOffset.x
                                                                    val draggedCenterY = currentInfo.offset.y + (currentInfo.size.height / 2f) + draggedOffset.y
                                                                    val target = libraryGridState.layoutInfo.visibleItemsInfo.firstOrNull { itemInfo ->
                                                                        itemInfo.index >= 1 &&
                                                                            draggedCenterX >= itemInfo.offset.x &&
                                                                            draggedCenterX <= itemInfo.offset.x + itemInfo.size.width &&
                                                                            draggedCenterY >= itemInfo.offset.y &&
                                                                            draggedCenterY <= itemInfo.offset.y + itemInfo.size.height
                                                                    }

                                                                    if (target != null && target.index >= 1) {
                                                                        val targetStickerIndex = target.index - 1
                                                                        if (targetStickerIndex in activeStickers.indices && targetStickerIndex != draggedIndex) {
                                                                            draggedOffset += Offset(
                                                                                (currentInfo.offset.x - target.offset.x).toFloat(),
                                                                                (currentInfo.offset.y - target.offset.y).toFloat()
                                                                            )
                                                                            reorderPreview = StickerOrderPolicy.move(activeStickers, draggedIndex, targetStickerIndex)
                                                                            draggedIndex = targetStickerIndex
                                                                            didReorderStickers = true
                                                                        }
                                                                    }

                                                                    val viewportEnd = libraryGridState.layoutInfo.viewportEndOffset.toFloat()
                                                                    when {
                                                                        draggedCenterY < autoScrollEdgePx -> libraryGridState.dispatchRawDelta(-14f)
                                                                        draggedCenterY > viewportEnd - autoScrollEdgePx -> libraryGridState.dispatchRawDelta(14f)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else null,
                                                    onReorderCancel = {
                                                        reorderPreview = null
                                                        draggedStickerId = null
                                                        draggedIndex = -1
                                                        draggedOffset = Offset.Zero
                                                        didReorderStickers = false
                                                    },
                                                    onReorderEnd = {
                                                        val finalOrder = reorderPreview
                                                        val shouldPersist = didReorderStickers && finalOrder != null
                                                        draggedStickerId = null
                                                        draggedIndex = -1
                                                        draggedOffset = Offset.Zero
                                                        didReorderStickers = false
                                                        if (shouldPersist) {
                                                            scope.launch {
                                                                if (!repository.persistStickerOrder(finalOrder.map { it.id })) {
                                                                    reorderPreview = null
                                                                    flashSnackbar("Couldn’t save sticker order")
                                                                }
                                                            }
                                                        } else {
                                                            reorderPreview = null
                                                        }
                                                    },
                                                    isSelectionMode = isSelectionMode,
                                                    isSelected = selectedStickerIds.contains(sticker.id),
                                                    showCopiedBadge = recentlyCopiedId == sticker.id
                                                )
                                            }
                                        }
                                    }
                                }

                                StickerLibraryViewMode.LARGE_GRID -> {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = listBottomPadding),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { alpha = gridFilterAlpha.value }
                                    ) {
                                        item(key = "library_headers", span = { GridItemSpan(maxLineSpan) }) {
                                            LibraryHeadersContent(
                                                isSelectionMode = isSelectionMode,
                                                selectedStickerCount = selectedStickerIds.size,
                                                totalFilteredCount = filteredStickers.size,
                                                onCancelSelection = {
                                                    isSelectionMode = false
                                                    selectedStickerIds = emptySet()
                                                },
                                                onSelectAll = {
                                                    selectedStickerIds = if (selectedStickerIds.size == filteredStickers.size) {
                                                        emptySet()
                                                    } else {
                                                        filteredStickers.map { it.id }.toSet()
                                                    }
                                                },
                                                onFavoriteBatch = {
                                                    scope.launch {
                                                        repository.batchToggleFavorite(selectedStickerIds.toList(), true)
                                                        haptics.performConfirm()
                                                        flashSnackbar("Added to favorites")
                                                        isSelectionMode = false
                                                        selectedStickerIds = emptySet()
                                                    }
                                                },
                                                showBatchMoveMenu = showBatchMoveMenu,
                                                onToggleBatchMoveMenu = { showBatchMoveMenu = it },
                                                categories = categories,
                                                onBatchSetCategory = { catName ->
                                                    scope.launch {
                                                        repository.batchSetCategory(selectedStickerIds.toList(), catName)
                                                        haptics.performConfirm()
                                                        flashSnackbar("Moved to $catName")
                                                        isSelectionMode = false
                                                        selectedStickerIds = emptySet()
                                                    }
                                                },
                                                onBatchDelete = { showBatchDeleteConfirm = true },
                                                showLibrarySearch = showLibrarySearch,
                                                searchQuery = searchQuery,
                                                onSearchQueryChange = { searchQuery = it },
                                                isOverlayRunning = isOverlayRunning,
                                                onToggleOverlay = { toggleOverlay() },
                                                onOpenSettings = { if (navigator.requestPush(AppRoute.SETTINGS)) currentRoute = navigator.currentRoute },
                                                libraryViewMode = libraryViewMode,
                                                onOpenLayoutPicker = { showLibraryLayoutPicker = true },
                                                filteredStickersCount = filteredStickers.size,
                                                showLibraryCategoryFilters = showLibraryCategoryFilters,
                                                selectedCategory = selectedCategory,
                                                onSelectCategory = { selectedCategory = it },
                                                onAddCategoryClick = { showAddCategoryDialog = true },
                                                onCategoryLongClick = { cat -> categoryToDelete = cat },
                                                clipboardImageUris = clipboardImageUris,
                                                onImportClipboard = {
                                                    if (clipboardImageUris.size == 1) {
                                                        importClipboardStickers(clipboardImageUris)
                                                    } else {
                                                        showClipboardPicker = true
                                                    }
                                                },
                                                onDismissClipboard = { consumeClipboardOffer() },
                                                appFocusManager = appFocusManager,
                                                showQuickStickersOnboarding = showOnboarding,
                                                onEnableQuickStickers = {
                                                    QuickStickersOnboardingPolicy.markCompletedOrDismissed(context)
                                                    showOnboarding = false
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                                        val intent = Intent(
                                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                            Uri.parse("package:${context.packageName}")
                                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                                        context.startActivity(intent)
                                                        scope.launch {
                                                            flashSnackbar("Please grant 'Display over other apps' to activate Quick Stickers")
                                                        }
                                                    } else {
                                                        val serviceIntent = Intent(context, OverlayService::class.java)
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            context.startForegroundService(serviceIntent)
                                                        } else {
                                                            context.startService(serviceIntent)
                                                        }
                                                        isOverlayRunning = true
                                                        haptics.performConfirm()
                                                        scope.launch {
                                                            flashSnackbar("Quick Stickers activated!")
                                                        }
                                                    }
                                                },
                                                onDismissQuickStickers = {
                                                    QuickStickersOnboardingPolicy.markCompletedOrDismissed(context)
                                                    showOnboarding = false
                                                    haptics.performTap()
                                                }
                                            )
                                        }

                                        if (librarySnapshotState is LibrarySnapshotState.Loading) {
                                            item(key = "library_loading", span = { GridItemSpan(maxLineSpan) }) {
                                                LibraryLoadingView()
                                            }
                                        } else if (librarySnapshotState is LibrarySnapshotState.Failed) {
                                            item(key = "library_load_failed", span = { GridItemSpan(maxLineSpan) }) {
                                                LibraryLoadFailureView(
                                                    onRetry = { libraryRefreshAttempt += 1 }
                                                )
                                            }
                                        } else if (displayedStickers.isEmpty()) {
                                            item(key = "empty_library", span = { GridItemSpan(maxLineSpan) }) {
                                                EmptyLibraryView(searchQuery = searchQuery, visualTheme = visualTheme)
                                            }
                                        } else {
                                            items(displayedStickers, key = { it.id }) { sticker ->
                                                LargeStickerCard(
                                                    sticker = sticker,
                                                    isSelectionMode = isSelectionMode,
                                                    isSelected = selectedStickerIds.contains(sticker.id),
                                                    showCopiedBadge = recentlyCopiedId == sticker.id,
                                                    onClick = onItemClick,
                                                    onLongClick = onItemLongClick
                                                )
                                            }
                                        }
                                    }
                                }

                                StickerLibraryViewMode.LIST -> {
                                    LazyColumn(
                                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = listBottomPadding),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { alpha = gridFilterAlpha.value }
                                    ) {
                                        item(key = "library_headers") {
                                            LibraryHeadersContent(
                                                isSelectionMode = isSelectionMode,
                                                selectedStickerCount = selectedStickerIds.size,
                                                totalFilteredCount = filteredStickers.size,
                                                onCancelSelection = {
                                                    isSelectionMode = false
                                                    selectedStickerIds = emptySet()
                                                },
                                                onSelectAll = {
                                                    selectedStickerIds = if (selectedStickerIds.size == filteredStickers.size) {
                                                        emptySet()
                                                    } else {
                                                        filteredStickers.map { it.id }.toSet()
                                                    }
                                                },
                                                onFavoriteBatch = {
                                                    scope.launch {
                                                        repository.batchToggleFavorite(selectedStickerIds.toList(), true)
                                                        haptics.performConfirm()
                                                        flashSnackbar("Added to favorites")
                                                        isSelectionMode = false
                                                        selectedStickerIds = emptySet()
                                                    }
                                                },
                                                showBatchMoveMenu = showBatchMoveMenu,
                                                onToggleBatchMoveMenu = { showBatchMoveMenu = it },
                                                categories = categories,
                                                onBatchSetCategory = { catName ->
                                                    scope.launch {
                                                        repository.batchSetCategory(selectedStickerIds.toList(), catName)
                                                        haptics.performConfirm()
                                                        flashSnackbar("Moved to $catName")
                                                        isSelectionMode = false
                                                        selectedStickerIds = emptySet()
                                                    }
                                                },
                                                onBatchDelete = { showBatchDeleteConfirm = true },
                                                showLibrarySearch = showLibrarySearch,
                                                searchQuery = searchQuery,
                                                onSearchQueryChange = { searchQuery = it },
                                                isOverlayRunning = isOverlayRunning,
                                                onToggleOverlay = { toggleOverlay() },
                                                onOpenSettings = { if (navigator.requestPush(AppRoute.SETTINGS)) currentRoute = navigator.currentRoute },
                                                libraryViewMode = libraryViewMode,
                                                onOpenLayoutPicker = { showLibraryLayoutPicker = true },
                                                filteredStickersCount = filteredStickers.size,
                                                showLibraryCategoryFilters = showLibraryCategoryFilters,
                                                selectedCategory = selectedCategory,
                                                onSelectCategory = { selectedCategory = it },
                                                onAddCategoryClick = { showAddCategoryDialog = true },
                                                onCategoryLongClick = { cat -> categoryToDelete = cat },
                                                clipboardImageUris = clipboardImageUris,
                                                onImportClipboard = {
                                                    if (clipboardImageUris.size == 1) {
                                                        importClipboardStickers(clipboardImageUris)
                                                    } else {
                                                        showClipboardPicker = true
                                                    }
                                                },
                                                onDismissClipboard = { consumeClipboardOffer() },
                                                appFocusManager = appFocusManager,
                                                showQuickStickersOnboarding = showOnboarding,
                                                onEnableQuickStickers = {
                                                    QuickStickersOnboardingPolicy.markCompletedOrDismissed(context)
                                                    showOnboarding = false
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                                        val intent = Intent(
                                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                            Uri.parse("package:${context.packageName}")
                                                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                                        context.startActivity(intent)
                                                        scope.launch {
                                                            flashSnackbar("Please grant 'Display over other apps' to activate Quick Stickers")
                                                        }
                                                    } else {
                                                        val serviceIntent = Intent(context, OverlayService::class.java)
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            context.startForegroundService(serviceIntent)
                                                        } else {
                                                            context.startService(serviceIntent)
                                                        }
                                                        isOverlayRunning = true
                                                        haptics.performConfirm()
                                                        scope.launch {
                                                            flashSnackbar("Quick Stickers activated!")
                                                        }
                                                    }
                                                },
                                                onDismissQuickStickers = {
                                                    QuickStickersOnboardingPolicy.markCompletedOrDismissed(context)
                                                    showOnboarding = false
                                                    haptics.performTap()
                                                }
                                            )
                                        }

                                        if (librarySnapshotState is LibrarySnapshotState.Loading) {
                                            item(key = "library_loading") {
                                                LibraryLoadingView()
                                            }
                                        } else if (librarySnapshotState is LibrarySnapshotState.Failed) {
                                            item(key = "library_load_failed") {
                                                LibraryLoadFailureView(
                                                    onRetry = { libraryRefreshAttempt += 1 }
                                                )
                                            }
                                        } else if (displayedStickers.isEmpty()) {
                                            item(key = "empty_library") {
                                                EmptyLibraryView(searchQuery = searchQuery, visualTheme = visualTheme)
                                            }
                                        } else {
                                            items(displayedStickers, key = { it.id }) { sticker ->
                                                StickerListItem(
                                                    sticker = sticker,
                                                    isSelectionMode = isSelectionMode,
                                                    isSelected = selectedStickerIds.contains(sticker.id),
                                                    showCopiedBadge = recentlyCopiedId == sticker.id,
                                                    onClick = onItemClick,
                                                    onLongClick = onItemLongClick
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            if (showLibraryLayoutPicker) {
                StickerLibraryLayoutPickerSheet(
                    currentMode = libraryViewMode,
                    pendingMode = pendingLibraryLayoutMode,
                    sheetState = libraryLayoutPickerSheetState,
                    isDismissing = isDismissingLibraryPicker,
                    onSelectMode = { selectLibraryLayoutMode(it) },
                    onDismissRequest = { dismissLibraryLayoutPicker() }
                )
            }
        }

        // Layer 1: Settings Screen (zIndex = 1f)
        AnimatedVisibility(
            visible = currentRoute == AppRoute.SETTINGS || currentRoute == AppRoute.CATEGORY_MANAGEMENT,
            enter = slideInHorizontally(
                animationSpec = tween(durationMillis = 280, easing = StickHubMotion.EasingEmphasizedDecelerate),
                initialOffsetX = { fullWidth -> fullWidth }
            ),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = 250, easing = StickHubMotion.EasingEmphasizedAccelerate),
                targetOffsetX = { fullWidth -> fullWidth }
            ),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                visualTheme = visualTheme,
                onVisualThemeChange = onVisualThemeChange,
                libraryViewMode = libraryViewMode,
                onLibraryViewModeChange = { newMode ->
                    libraryViewMode = newMode
                    StickerLibraryPreferences.setViewMode(context, newMode)
                },
                showLibrarySearch = showLibrarySearch,
                onToggleShowSearch = onToggleShowSearch,
                showLibraryCategoryFilters = showLibraryCategoryFilters,
                onToggleShowCategoryFilters = onToggleShowCategoryFilters,
                isOverlayRunning = isOverlayRunning,
                onToggleOverlay = { toggleOverlay() },
                onRetryOverlayPermission = { toggleOverlay() },
                overlayBubbleSizeDp = overlayBubbleSizeDp,
                onOverlayBubbleSizeChange = { sizeDp ->
                    overlayBubbleSizeDp = sizeDp
                    OverlayPreferences.setBubbleSizeDp(context, sizeDp)
                    if (OverlayService.isRunning) {
                        context.startService(
                            Intent(context, OverlayService::class.java)
                                .setAction(OverlayService.ACTION_REFRESH_CONFIGURATION)
                        )
                    }
                },
                overlayBubbleOpacity = overlayBubbleOpacity,
                onOverlayBubbleOpacityChange = { opacity ->
                    overlayBubbleOpacity = opacity
                    OverlayPreferences.setBubbleOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewBubbleOpacity = { },
                popupMasterOpacity = popupMasterOpacity,
                onPopupMasterOpacityChange = { opacity ->
                    popupMasterOpacity = opacity
                    OverlayPreferences.setPopupMasterOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewMasterOpacity = { },
                popupSurfaceOpacity = popupSurfaceOpacity,
                onPopupSurfaceOpacityChange = { opacity ->
                    popupSurfaceOpacity = opacity
                    OverlayPreferences.setPopupSurfaceOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewSurfaceOpacity = { },
                popupStickersOpacity = popupStickersOpacity,
                onPopupStickersOpacityChange = { opacity ->
                    popupStickersOpacity = opacity
                    OverlayPreferences.setPopupStickersOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewStickersOpacity = { },
                popupChromeOpacity = popupChromeOpacity,
                onPopupChromeOpacityChange = { opacity ->
                    popupChromeOpacity = opacity
                    OverlayPreferences.setPopupChromeOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewChromeOpacity = { },
                popupCloseOpacity = popupCloseOpacity,
                onPopupCloseOpacityChange = { opacity ->
                    popupCloseOpacity = opacity
                    OverlayPreferences.setPopupCloseOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewCloseOpacity = { },
                popupResizeOpacity = popupResizeOpacity,
                onPopupResizeOpacityChange = { opacity ->
                    popupResizeOpacity = opacity
                    OverlayPreferences.setPopupResizeOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewResizeOpacity = { },
                stickerShadowStrength = stickerShadowStrength,
                onStickerShadowStrengthChange = { strength ->
                    // Heavy op: re-renders every cached thumbnail. Only on release.
                    stickerShadowStrength = strength
                    OverlayPreferences.setStickerShadowStrength(context, strength)
                    sendShadowOverlayUpdate()
                },
                onLivePreviewShadowStrength = { },
                onRevealOverlayControls = { revealOverlayControls() },
                onResetOverlayAppearance = {
                    OverlayPreferences.resetAppearance(context)
                    overlayBubbleOpacity = OverlayPreferences.bubbleOpacity(context)
                    popupMasterOpacity = OverlayPreferences.popupMasterOpacity(context)
                    popupSurfaceOpacity = OverlayPreferences.popupSurfaceOpacity(context)
                    popupStickersOpacity = OverlayPreferences.popupStickersOpacity(context)
                    popupChromeOpacity = OverlayPreferences.popupChromeOpacity(context)
                    popupCloseOpacity = OverlayPreferences.popupCloseOpacity(context)
                    popupResizeOpacity = OverlayPreferences.popupResizeOpacity(context)
                    stickerShadowStrength = OverlayPreferences.stickerShadowStrength(context)
                    overlayBubbleSizeDp = OverlayPreferences.bubbleSizeDp(context)
                    sendLightweightOverlayUpdate()
                    if (OverlayService.isRunning) {
                        context.startService(
                            Intent(context, OverlayService::class.java)
                                .setAction(OverlayService.ACTION_REFRESH_CONFIGURATION)
                        )
                    }
                },
                startFilterMode = startFilterMode,
                startCustomCategory = startCustomCategory,
                onStartFilterChange = { mode, customCat ->
                    startFilterMode = mode
                    startCustomCategory = customCat
                    OverlayPreferences.setStartFilterMode(context, mode)
                    OverlayPreferences.setStartCustomCategory(context, customCat)
                },
                afterCopyAction = afterCopyAction,
                onAfterCopyActionChange = { action ->
                    afterCopyAction = action
                    OverlayPreferences.setAfterCopyAction(context, action)
                },
                availableCategories = categories.map { it.name },
                showQuickStickersTitle = showQuickStickersTitle,
                onToggleShowTitle = { show ->
                    showQuickStickersTitle = show
                    OverlayPreferences.setShowTitle(context, show)
                    if (OverlayService.isRunning) {
                        context.startService(
                            Intent(context, OverlayService::class.java)
                                .setAction(OverlayService.ACTION_REFRESH_CONFIGURATION)
                        )
                    }
                },
                showQuickStickersSearch = showQuickStickersSearch,
                onToggleShowQuickSearch = { show ->
                    showQuickStickersSearch = show
                    OverlayPreferences.setShowSearch(context, show)
                    if (OverlayService.isRunning) {
                        context.startService(
                            Intent(context, OverlayService::class.java)
                                .setAction(OverlayService.ACTION_REFRESH_CONFIGURATION)
                        )
                    }
                },
                showQuickStickersCategories = showQuickStickersCategories,
                onToggleShowQuickCategories = { show ->
                    showQuickStickersCategories = show
                    OverlayPreferences.setShowCategories(context, show)
                    if (OverlayService.isRunning) {
                        context.startService(
                            Intent(context, OverlayService::class.java)
                                .setAction(OverlayService.ACTION_REFRESH_CONFIGURATION)
                        )
                    }
                },
                stickerCount = allStickers.size,
                categoryCount = categories.size,
                storageSizeBytes = repository.getStorageSize(),
                onNavigateToCategoryManagement = {
                    if (navigator.requestPush(AppRoute.CATEGORY_MANAGEMENT)) {
                        currentRoute = navigator.currentRoute
                    }
                },
                onExportBackup = {
                    val time = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                    exportLauncher.launch("StickHub_Backup_.stickhub")
                },
                onImportBackup = {
                    importLauncher.launch(arrayOf("*/*"))
                },
                onBack = {
                    if (navigator.requestPop()) {
                        currentRoute = navigator.currentRoute
                    }
                }
            )
        }

        // Layer 2: Category Management Screen (zIndex = 2f)
        AnimatedVisibility(
            visible = currentRoute == AppRoute.CATEGORY_MANAGEMENT,
            enter = slideInHorizontally(
                animationSpec = tween(durationMillis = 280, easing = StickHubMotion.EasingEmphasizedDecelerate),
                initialOffsetX = { fullWidth -> fullWidth }
            ),
            exit = slideOutHorizontally(
                animationSpec = tween(durationMillis = 250, easing = StickHubMotion.EasingEmphasizedAccelerate),
                targetOffsetX = { fullWidth -> fullWidth }
            ),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)
        ) {
            CategoryManagementScreen(
                categories = categories,
                stickers = allStickers,
                onAddCategory = { name ->
                    scope.launch {
                        if (repository.addCategory(name)) {
                            haptics.performConfirm()
                            flashSnackbar("Category '$name' created")
                        } else {
                            haptics.performReject()
                            flashSnackbar("Failed to create category")
                        }
                    }
                },
                onRenameCategory = { oldName, newName ->
                    scope.launch {
                        if (repository.renameCategory(oldName, newName)) {
                            haptics.performConfirm()
                            if (selectedCategory.equals(oldName, ignoreCase = true)) {
                                selectedCategory = newName
                            }
                            flashSnackbar("Renamed to '$newName'")
                        } else {
                            haptics.performReject()
                            flashSnackbar("Failed to rename category")
                        }
                    }
                },
                onDeleteCategory = { name ->
                    scope.launch {
                        if (repository.deleteCategory(name)) {
                            haptics.performConfirm()
                            if (selectedCategory.equals(name, ignoreCase = true)) {
                                selectedCategory = "All"
                            }
                            flashSnackbar("Category '$name' deleted")
                        } else {
                            haptics.performReject()
                            flashSnackbar("Failed to delete category")
                        }
                    }
                },
                onReorderCategories = { orderedNames ->
                    scope.launch {
                        repository.reorderCategories(orderedNames)
                    }
                },
                onBack = {
                    if (navigator.requestPop()) {
                        currentRoute = navigator.currentRoute
                    }
                }
            )
        }
    }

    // The Create FAB must expose the retained Google-Photos clipboard workflow explicitly.
    if (showCreateSourceDialog) {
        AlertDialog(
            onDismissRequest = { showCreateSourceDialog = false },
            title = { Text("Create sticker") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose how you want to create it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            if (clipboardImageUris.isEmpty()) return@Button
                            showCreateSourceDialog = false
                            if (clipboardImageUris.size == 1) {
                                importClipboardStickers(clipboardImageUris)
                            } else {
                                showClipboardPicker = true
                            }
                        },
                        enabled = clipboardImageUris.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_save),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (clipboardImageUris.size) {
                                0 -> "No image currently on clipboard"
                                1 -> "Import copied image directly"
                                else -> "Import ${clipboardImageUris.size} copied images"
                            }
                        )
                    }
                    Button(
                        onClick = {
                            showCreateSourceDialog = false
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_folder_open),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick photo from device")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCreateSourceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Multi-image clipboard review sheet
    if (showClipboardPicker && clipboardImageUris.isNotEmpty()) {
        ClipboardImportSheet(
            uris = clipboardImageUris,
            onImportSelected = { importClipboardStickers(it) },
            onDismiss = { consumeClipboardOffer() }
        )
    }

    // Subject Cutout Sheet
    activeCutoutUri?.let { uri ->        SubjectCutoutSheet(
            imageUri = uri,
            sheetState = cutoutSheetState,
            categories = categories,
            onDismiss = { activeCutoutUri = null },
            onSaveSticker = { bitmap, title, category, tags ->
                scope.launch {
                    val saved = repository.saveStickerBitmap(bitmap, title, category, tags)
                    activeCutoutUri = null
                    if (saved != null) {
                        haptics.performConfirm()
                        flashSnackbar("Sticker created successfully!")
                    } else {
                        haptics.performReject()
                        flashSnackbar("Failed to create sticker")
                    }
                }
            },
            onChangeImage = {
                activeCutoutUri = null
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )
    }

    // Sticker Detail BottomSheet
    selectedStickerForDetail?.let { sticker ->
        StickerDetailBottomSheet(
            sticker = sticker,
            sheetState = detailSheetState,
            categories = categories,
            onDismiss = { selectedStickerForDetail = null },
            onCopy = {
                ClipboardHelper.copyStickerToClipboard(context, it)
                haptics.performConfirm()
                scope.launch {
                    repository.recordUsage(it.id)
                    flashSnackbar("Copied to clipboard!")
                }
            },
            onToggleFavorite = { id ->
                scope.launch {
                    repository.toggleFavorite(id)
                    haptics.performConfirm()
                }
            },
            onDelete = { id ->
                scope.launch {
                    repository.deleteSticker(id)
                    selectedStickerForDetail = null
                    haptics.performConfirm()
                    flashSnackbar("Sticker deleted")
                }
            },
            onUpdateDetails = { id, title, category, tags ->
                scope.launch {
                    repository.updateSticker(id, title, category, tags)
                    haptics.performConfirm()
                    flashSnackbar("Updated successfully")
                }
            },
            onOpenStudio = {
                val targetSticker = sticker
                selectedStickerForDetail = null
                selectedStickerForStudio = targetSticker
            }
        )
    }

    // Sticker Studio BottomSheet
    selectedStickerForStudio?.let { sticker ->
        StickerStudioBottomSheet(
            sticker = sticker,
            sheetState = studioSheetState,
            onDismiss = { selectedStickerForStudio = null },
            onSaveNew = { editedBitmap ->
                scope.launch {
                    val saved = repository.saveStickerBitmap(
                        bitmap = editedBitmap,
                        title = "${sticker.title} (Edited)",
                        category = sticker.category,
                        tags = sticker.tags
                    )
                    selectedStickerForStudio = null
                    if (saved != null) {
                        flashSnackbar("Sticker saved to studio copy!")
                    }
                }
            },
            onOverwrite = { editedBitmap ->
                scope.launch {
                    val ok = repository.overwriteStickerBitmap(sticker.id, editedBitmap)
                    selectedStickerForStudio = null
                    if (ok) {
                        flashSnackbar("Sticker updated successfully!")
                    }
                }
            }
        )
    }



    // Add Category Dialog (accessible from CategoryChips quick add)
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            categories = categories,
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { catName ->
                scope.launch {
                    if (repository.addCategory(catName)) {
                        selectedCategory = catName
                        haptics.performConfirm()
                        flashSnackbar("Category '$catName' created")
                    } else {
                        haptics.performReject()
                        flashSnackbar("Failed to create category")
                    }
                }
            }
        )
    }

    // Delete Category Confirmation Dialog (from long click on CategoryChips)
    categoryToDelete?.let { cat ->
        val fallbackHome = categories
            .filter { !it.name.equals(cat.name, ignoreCase = true) }
            .sortedBy { it.displayOrder }
            .let { remaining ->
                remaining.firstOrNull { it.name.equals("General", ignoreCase = true) }?.name
                    ?: remaining.firstOrNull()?.name
            }
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category") },
            text = {
                Text(
                    if (fallbackHome != null) {
                        "Are you sure you want to delete '${cat.name}'? Stickers in this category will be moved to '$fallbackHome'."
                    } else {
                        "Are you sure you want to delete '${cat.name}'? This is your last category, so a fresh empty 'General' will be created."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteCategory(cat.name)
                            if (selectedCategory == cat.name) {
                                selectedCategory = "All"
                            }
                            categoryToDelete = null
                            haptics.performConfirm()
                            flashSnackbar(
                                if (fallbackHome != null) "Category deleted, stickers moved to $fallbackHome"
                                else "Category deleted"
                            )
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Batch Delete Confirmation Dialog
    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("Delete Selected Stickers") },
            text = { Text("Are you sure you want to delete ${selectedStickerIds.size} selected stickers? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val count = repository.batchDelete(selectedStickerIds.toList())
                            isSelectionMode = false
                            selectedStickerIds = emptySet()
                            showBatchDeleteConfirm = false
                            haptics.performConfirm()
                            flashSnackbar("Deleted $count stickers")
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LibraryHeadersContent(
    isSelectionMode: Boolean,
    selectedStickerCount: Int,
    totalFilteredCount: Int,
    onCancelSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onFavoriteBatch: () -> Unit,
    showBatchMoveMenu: Boolean,
    onToggleBatchMoveMenu: (Boolean) -> Unit,
    categories: List<CategoryItem>,
    onBatchSetCategory: (String) -> Unit,
    onBatchDelete: () -> Unit,
    showLibrarySearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isOverlayRunning: Boolean,
    onToggleOverlay: () -> Unit,
    onOpenSettings: () -> Unit,
    libraryViewMode: StickerLibraryViewMode,
    onOpenLayoutPicker: () -> Unit,
    filteredStickersCount: Int,
    showLibraryCategoryFilters: Boolean,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onCategoryLongClick: (CategoryItem) -> Unit,
    clipboardImageUris: List<Uri>,
    onImportClipboard: () -> Unit,
    onDismissClipboard: () -> Unit,
    appFocusManager: androidx.compose.ui.focus.FocusManager,
    showQuickStickersOnboarding: Boolean,
    onEnableQuickStickers: () -> Unit,
    onDismissQuickStickers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberStickHubHaptics()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(appFocusManager) {
                detectTapGestures(onTap = { appFocusManager.clearFocus() })
            }
    ) {
        // 1. Top Action Row / Search Bar
        if (isSelectionMode) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCancelSelection) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_x),
                            contentDescription = "Cancel selection",
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Text(
                        text = "$selectedStickerCount selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )

                    IconButton(onClick = onSelectAll) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_check_check),
                            contentDescription = "Select All",
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(onClick = onFavoriteBatch) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_heart),
                            contentDescription = "Favorite selected",
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { onToggleBatchMoveMenu(true) }) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_folder_input),
                                contentDescription = "Move category",
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showBatchMoveMenu,
                            onDismissRequest = { onToggleBatchMoveMenu(false) }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        onToggleBatchMoveMenu(false)
                                        onBatchSetCategory(cat.name)
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = onBatchDelete) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_trash_2),
                            contentDescription = "Delete selected",
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        } else if (showLibrarySearch) {
            TopSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                isOverlayRunning = isOverlayRunning,
                onToggleOverlay = onToggleOverlay,
                onOpenSettings = onOpenSettings,
                currentViewMode = libraryViewMode,
                onOpenLayoutPicker = onOpenLayoutPicker,
                resultCount = filteredStickersCount,
                applyDefaultPadding = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 6.dp)
            )
        } else {
            // Ultra-compact action row when search is hidden
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "StickHub",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleOverlay,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isOverlayRunning) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_layers_2),
                                contentDescription = "Floating Overlay",
                                tint = if (isOverlayRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptics.performNavigationTap()
                            onOpenLayoutPicker()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(libraryViewMode.iconRes),
                                contentDescription = "Switch layout: " + libraryViewMode.title,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptics.performNavigationTap()
                            onOpenSettings()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_settings_2),
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Category Chips Rail
        if (!isSelectionMode && showLibraryCategoryFilters) {
            CategoryChips(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelectCategory = onSelectCategory,
                onAddCategoryClick = onAddCategoryClick,
                onCategoryLongClick = onCategoryLongClick,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp)
            )
        }

        // 3. Detected Clipboard Banner
        AnimatedVisibility(
            visible = clipboardImageUris.isNotEmpty() && !isSelectionMode,
            enter = StickHubMotion.BannerEnter,
            exit = StickHubMotion.BannerExit
        ) {
            if (clipboardImageUris.isNotEmpty()) {
                val context = LocalContext.current
                val previewUri = clipboardImageUris.first()
                val extraCount = clipboardImageUris.size - 1
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = 2.dp,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CheckerboardBackground(modifier = Modifier.fillMaxSize())

                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(previewUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Clipboard preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(3.dp)
                                )

                                if (extraCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.45f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$extraCount",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (extraCount > 0) "${clipboardImageUris.size} images ready" else "Ready to import",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (extraCount > 0) "Review and import together" else "Save sticker to your library",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onImportClipboard,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_save),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (extraCount > 0) "Review" else "Save",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = onDismissClipboard,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_x),
                                    contentDescription = "Dismiss",
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryLoadingView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp, bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading your stickers…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LibraryLoadFailureView(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_database_zap),
                contentDescription = "Library load failed",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Your stickers are safe, but the library could not load.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Try loading the on-device library again.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(onClick = onRetry) {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_refresh_cw),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyLibraryView(
    searchQuery: String,
    visualTheme: AppVisualTheme = AppVisualTheme.DEFAULT
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 64.dp, start = 32.dp, end = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (searchQuery.isEmpty()) {
                when (visualTheme) {
                    AppVisualTheme.AURORA -> {
                        AuroraRibbonMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            bandA = MaterialTheme.colorScheme.primary,
                            bandB = MaterialTheme.colorScheme.secondary,
                            bandC = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    AppVisualTheme.SYNTHWAVE -> {
                        SynthSunMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            sun = MaterialTheme.colorScheme.secondary,
                            sunLow = MaterialTheme.colorScheme.tertiary,
                            grid = MaterialTheme.colorScheme.primary
                        )
                    }
                    AppVisualTheme.GATSBY -> {
                        DecoSunMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            gold = MaterialTheme.colorScheme.primary,
                            ink = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    AppVisualTheme.UKIYO -> {
                        WaveMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            sun = MaterialTheme.colorScheme.secondary,
                            wave = MaterialTheme.colorScheme.primary,
                            foam = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    AppVisualTheme.PIXEL -> {
                        PixelInvaderMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            pixel = MaterialTheme.colorScheme.primary,
                            accent = MaterialTheme.colorScheme.secondary
                        )
                    }
                    AppVisualTheme.KAWAII -> {
                        KawaiiCloudMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            cloud = MaterialTheme.colorScheme.surface,
                            outline = MaterialTheme.colorScheme.primary,
                            blush = MaterialTheme.colorScheme.secondary
                        )
                    }
                    AppVisualTheme.SOLARPUNK -> {
                        SolarLeafMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            sun = MaterialTheme.colorScheme.secondary,
                            leaf = MaterialTheme.colorScheme.primary,
                            stem = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    AppVisualTheme.NOIR -> {
                        NoirLampMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            lamp = MaterialTheme.colorScheme.secondary,
                            ink = MaterialTheme.colorScheme.primary,
                            rain = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AppVisualTheme.GLASS -> {
                        GlassDropletMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            glass = MaterialTheme.colorScheme.primary,
                            shine = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    AppVisualTheme.NOUVEAU -> {
                        NouveauBloomMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            gold = MaterialTheme.colorScheme.secondary,
                            teal = MaterialTheme.colorScheme.primary,
                            wine = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    AppVisualTheme.COTTAGE -> {
                        CottageRoseMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            bloom = MaterialTheme.colorScheme.primaryContainer,
                            ink = MaterialTheme.colorScheme.primary,
                            leaf = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                    AppVisualTheme.STARBASE -> {
                        StarbasePlanetMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            planet = MaterialTheme.colorScheme.primaryContainer,
                            ring = MaterialTheme.colorScheme.primary,
                            signal = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    AppVisualTheme.ATELIER -> {
                        AtelierFrameMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            frame = MaterialTheme.colorScheme.primary,
                            sun = MaterialTheme.colorScheme.primaryContainer,
                            paper = MaterialTheme.colorScheme.surface
                        )
                    }
                    AppVisualTheme.PRESSROOM -> {
                        PressFrontPageMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            paper = MaterialTheme.colorScheme.surface,
                            ink = MaterialTheme.colorScheme.primary,
                            accent = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                    AppVisualTheme.OLD_MONEY -> {
                        OldMoneySealMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            ring = MaterialTheme.colorScheme.outline,
                            initial = MaterialTheme.colorScheme.primary
                        )
                    }
                    AppVisualTheme.NEUBRUTALISM -> {
                        NeoStickerMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            fill = MaterialTheme.colorScheme.primaryContainer,
                            ink = MaterialTheme.colorScheme.outline,
                            accent = MaterialTheme.colorScheme.secondary
                        )
                    }
                    AppVisualTheme.HERBARIUM -> {
                        BotanicalLeafMotif(
                            modifier = Modifier
                                .size(72.dp)
                                .padding(bottom = 12.dp),
                            tint = MaterialTheme.colorScheme.primary,
                            alpha = 0.25f
                        )
                    }
                    AppVisualTheme.SKETCHBOOK -> {
                        SketchDoodleMotif(
                            modifier = Modifier
                                .size(96.dp)
                                .padding(bottom = 12.dp),
                            tint = MaterialTheme.colorScheme.primary,
                            accentTint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    AppVisualTheme.DEFAULT -> {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_folder_open),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                Icon(
                    painter = painterResource(LucideR.drawable.lucide_ic_folder_open),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (searchQuery.isNotEmpty()) "No matching stickers found" else "Your library is empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap '+' to create a sticker from a photo on your device",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun QuickStickersOnboardingCard(
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_layers),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Copy stickers while you chat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Quick Stickers floats over messaging apps so you can search and copy stickers with a single tap without switching apps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Not now",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onEnable,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "Enable Quick Stickers")
                }
            }
        }
    }
}
