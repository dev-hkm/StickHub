package com.hkm.stickhub.ui

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import android.os.SystemClock
import androidx.compose.material3.ButtonDefaults
import com.hkm.stickhub.service.OverlayStartFilterMode
import com.hkm.stickhub.service.OverlayAfterCopyAction
import com.hkm.stickhub.service.QuickStickersOnboardingPolicy

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.hkm.stickhub.ui.settings.PreviewRateLimiter
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
import com.hkm.stickhub.util.BackupOperations
import com.hkm.stickhub.util.BackupWorkState
import com.hkm.stickhub.util.ClipboardHelper
import com.hkm.stickhub.util.ClipboardBatchSnapshot
import com.hkm.stickhub.util.ClipboardOfferReducer
import com.hkm.stickhub.util.ClipboardStager
import com.hkm.stickhub.util.IncomingShareBatch
import com.hkm.stickhub.util.StagedClipboardItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed interface ModalRoute {
    object None : ModalRoute
    object SourceChooser : ModalRoute
    object ClipboardReview : ModalRoute
    data class SubjectCutout(val uri: Uri) : ModalRoute
    data class StickerDetail(val stickerId: Long) : ModalRoute
    data class StickerStudio(val sticker: StickerItem) : ModalRoute
    object LibraryLayout : ModalRoute
}

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
    incomingSharedBatch: IncomingShareBatch? = null,
    onClearSharedBatch: () -> Unit = {},
    foregroundTick: Int = 0,
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
    val categoryOrder by repository.categoryOrderFlow.collectAsState()
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

    // Modal Coordinator
    var activeModalRoute by remember { mutableStateOf<ModalRoute>(ModalRoute.None) }

    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val studioSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cutoutSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val libraryLayoutPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun dismissCutoutSheet(afterHidden: (() -> Unit)? = null) {
        scope.launch {
            try {
                cutoutSheetState.hide()
            } finally {
                if (activeModalRoute is ModalRoute.SubjectCutout) {
                    activeModalRoute = ModalRoute.None
                }
                afterHidden?.invoke()
            }
        }
    }

    // Library View Mode
    var libraryViewMode by remember {
        mutableStateOf(StickerLibraryPreferences.getViewMode(context))
    }
    var pendingLibraryLayoutMode by remember { mutableStateOf<StickerLibraryViewMode?>(null) }
    var isDismissingLibraryPicker by remember { mutableStateOf(false) }

    fun dismissLibraryLayoutPicker() {
        if (isDismissingLibraryPicker) return
        isDismissingLibraryPicker = true
        scope.launch {
            try {
                libraryLayoutPickerSheetState.hide()
            } finally {
                if (activeModalRoute is ModalRoute.LibraryLayout) {
                    activeModalRoute = ModalRoute.None
                }
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
        haptics.performTick()
        scope.launch {
            try {
                libraryLayoutPickerSheetState.hide()
            } finally {
                if (activeModalRoute is ModalRoute.LibraryLayout) {
                    activeModalRoute = ModalRoute.None
                }
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

    // A stuck transient preview must never survive leaving Settings:
    // restoring committed state is a single cheap intent.
    DisposableEffect(currentRoute) {
        onDispose {
            if (OverlayService.isRunning) {
                context.startService(
                    Intent(context, OverlayService::class.java)
                        .setAction(OverlayService.ACTION_UPDATE_APPEARANCE)
                )
            }
        }
    }

    // Dialogs
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryItem?>(null) }

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

    // Transient drag previews: layer + throttled value straight to the
    // running service. Nothing is persisted until release, and the service
    // clears previews on every committed update.
    val previewLimiter = remember { PreviewRateLimiter() }
    fun sendAppearancePreview(layer: String, value: Float) {
        if (!OverlayService.isRunning) return
        if (!previewLimiter.shouldDispatch(layer, SystemClock.uptimeMillis())) return
        context.startService(
            Intent(context, OverlayService::class.java)
                .setAction(OverlayService.ACTION_PREVIEW_APPEARANCE)
                .putExtra(OverlayService.EXTRA_APPEARANCE_LAYER, layer)
                .putExtra(OverlayService.EXTRA_APPEARANCE_VALUE, value)
        )
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
    var clipboardSkippedCount by remember { mutableIntStateOf(0) }
    var clipboardOffer by remember { mutableStateOf<ClipboardBatchSnapshot?>(null) }
    var clipboardOfferState by remember { mutableStateOf(ClipboardOfferReducer.State()) }
    var clipboardGeneration by remember { mutableLongStateOf(0L) }
    var stagedClipboardItems by remember { mutableStateOf<List<StagedClipboardItem>>(emptyList()) }
    var isStagingClipboard by remember { mutableStateOf(false) }
    var isImportingClipboard by remember { mutableStateOf(false) }
    var clipboardStageProgress by remember { mutableStateOf(0 to 0) }
    var recentlyCopiedId by remember { mutableStateOf<Long?>(null) }
    val clipboardOfferReducer = remember { ClipboardOfferReducer() }
    val clipboardStager = remember(context) { ClipboardStager(context.applicationContext) }

    val backupOps = remember(context) { BackupOperations.getInstance(context) }
    val backupWorkState by backupOps.state.collectAsState()

    fun showClipboardOffer(snapshot: ClipboardBatchSnapshot) {
        clipboardOffer = snapshot
        clipboardImageUris = snapshot.uris
        clipboardSkippedCount = snapshot.rejected.size
    }

    fun reduceClipboard(event: ClipboardOfferReducer.Event) {
        val (next, effect) = clipboardOfferReducer.reduce(clipboardOfferState, event)
        clipboardOfferState = next
        if (effect is ClipboardOfferReducer.Effect.Show) {
            showClipboardOffer(effect.snapshot)
        }
    }

    // Capture only ClipData metadata on the main thread. The stream is never
    // touched until Review, so an old scan cannot overwrite a newer batch.
    fun checkClipboardOffer() {
        val snapshot = ClipboardHelper.captureClipboardBatch(context, ++clipboardGeneration)
        if (snapshot == null) {
            if (activeModalRoute !is ModalRoute.ClipboardReview) {
                clipboardOffer = null
                clipboardImageUris = emptyList()
                clipboardSkippedCount = 0
            }
        } else {
            reduceClipboard(ClipboardOfferReducer.Event.ScanArrived(snapshot))
        }
    }

    fun openClipboardReview() {
        val offer = clipboardOffer ?: return
        if (offer.candidates.isEmpty() || isStagingClipboard || isImportingClipboard) return
        reduceClipboard(ClipboardOfferReducer.Event.ReviewOpened)
        activeModalRoute = ModalRoute.ClipboardReview
        stagedClipboardItems = emptyList()
        clipboardStageProgress = 0 to offer.candidates.size
        isStagingClipboard = true
        scope.launch {
            val staged = clipboardStager.stage(offer) { done, total ->
                scope.launch { clipboardStageProgress = done to total }
            }
            // The sheet is intentionally frozen to its opening snapshot.
            if (activeModalRoute is ModalRoute.ClipboardReview && clipboardOffer?.generation == offer.generation) {
                stagedClipboardItems = staged
            } else {
                staged.filterIsInstance<StagedClipboardItem.Ready>().forEach { it.file.delete() }
            }
            isStagingClipboard = false
        }
    }

    fun closeClipboardReview(consumeCurrent: Boolean) {
        val (next, effect) = clipboardOfferReducer.reduce(
            clipboardOfferState,
            ClipboardOfferReducer.Event.ReviewClosed
        )
        clipboardOfferState = next
        if (activeModalRoute is ModalRoute.ClipboardReview) {
            activeModalRoute = ModalRoute.None
        }
        isStagingClipboard = false
        isImportingClipboard = false
        clipboardStageProgress = 0 to 0
        stagedClipboardItems.filterIsInstance<StagedClipboardItem.Ready>().forEach { it.file.delete() }
        stagedClipboardItems = emptyList()
        if (effect is ClipboardOfferReducer.Effect.Show) {
            showClipboardOffer(effect.snapshot)
        } else if (consumeCurrent) {
            clipboardOfferState = next.copy(current = null)
            clipboardOffer = null
            clipboardImageUris = emptyList()
            clipboardSkippedCount = 0
        }
    }

    BackHandler(enabled = activeModalRoute !is ModalRoute.None) {
        when (activeModalRoute) {
            ModalRoute.None -> {}
            ModalRoute.LibraryLayout -> dismissLibraryLayoutPicker()
            ModalRoute.SourceChooser -> activeModalRoute = ModalRoute.None
            ModalRoute.ClipboardReview -> closeClipboardReview(consumeCurrent = false)
            is ModalRoute.SubjectCutout -> dismissCutoutSheet()
            is ModalRoute.StickerDetail -> {
                scope.launch {
                    try {
                        detailSheetState.hide()
                    } finally {
                        activeModalRoute = ModalRoute.None
                    }
                }
            }
            is ModalRoute.StickerStudio -> {
                scope.launch {
                    try {
                        studioSheetState.hide()
                    } finally {
                        activeModalRoute = ModalRoute.None
                    }
                }
            }
        }
    }

    /**
     * The regular clipboard affordance intentionally keeps the original one-tap
     * behavior: a clipboard primary clip is treated as one ready-made sticker.
     * Multi-item review remains reserved for an explicit ACTION_SEND_MULTIPLE
     * share, where the sender actually supplied a batch.
     */
    fun importSingleClipboardSticker() {
        val offer = clipboardOffer ?: return
        val candidate = offer.candidates.firstOrNull() ?: return
        if (isStagingClipboard || isImportingClipboard) return

        // Freeze this observation while the provider grant is staged. If the user
        // copies something else during the read, the reducer holds it as pending
        // and surfaces it after this operation completes.
        reduceClipboard(ClipboardOfferReducer.Event.ReviewOpened)
        isStagingClipboard = true
        clipboardStageProgress = 0 to 1
        val singleOffer = offer.copy(
            sourceItemCount = 1,
            candidates = listOf(candidate),
            rejected = emptyList()
        )

        scope.launch {
            try {
                val staged = clipboardStager.stage(singleOffer) { done, total ->
                    scope.launch { clipboardStageProgress = done to total }
                }
                isStagingClipboard = false
                val ready = staged.filterIsInstance<StagedClipboardItem.Ready>()
                if (ready.isEmpty()) {
                    haptics.performReject()
                    flashSnackbar("Couldn't read the copied image.")
                    closeClipboardReview(consumeCurrent = false)
                    return@launch
                }

                isImportingClipboard = true
                val result = repository.importStagedClipboardBatch(ready)
                isImportingClipboard = false
                // There is no review sheet in this path, so a failed provider
                // read cannot be retried from a hidden temporary file.
                ready.forEach { if (it.file.exists()) it.file.delete() }

                when {
                    result.saved.isNotEmpty() -> {
                        haptics.performConfirm()
                        flashSnackbar("Sticker saved to your library.")
                    }
                    result.duplicates.isNotEmpty() -> {
                        haptics.performTick()
                        flashSnackbar("Already in your library.")
                    }
                    else -> {
                        haptics.performReject()
                        flashSnackbar("Couldn't import the copied image.")
                    }
                }
                closeClipboardReview(consumeCurrent = result.failed.isEmpty())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                isStagingClipboard = false
                isImportingClipboard = false
                haptics.performReject()
                flashSnackbar("Couldn't import the copied image.")
                closeClipboardReview(consumeCurrent = false)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        var listenerRegistered = false
        val clipListener = ClipboardManager.OnPrimaryClipChangedListener { checkClipboardOffer() }
        fun registerListener() {
            if (listenerRegistered) return
            try {
                clipboard?.addPrimaryClipChangedListener(clipListener)
                listenerRegistered = true
            } catch (_: Exception) {
            }
        }
        fun unregisterListener() {
            if (!listenerRegistered) return
            try {
                clipboard?.removePrimaryClipChangedListener(clipListener)
            } catch (_: Exception) {
            }
            listenerRegistered = false
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    registerListener()
                    checkClipboardOffer()
                }
                Lifecycle.Event.ON_PAUSE -> unregisterListener()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            registerListener()
            checkClipboardOffer()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            unregisterListener()
        }
    }
    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            activeModalRoute = ModalRoute.SubjectCutout(uri)
        }
    }

    // Handle incoming shared image
    LaunchedEffect(incomingSharedUri) {
        incomingSharedUri?.let { uri ->
            activeModalRoute = ModalRoute.SubjectCutout(uri)
            onClearSharedUri()
        }
    }

    // Multi-share is a ready-made-sticker batch. It deliberately does not run
    // ML subject cutout N times; Review stages/imports the original images.
    LaunchedEffect(incomingSharedBatch?.id) {
        incomingSharedBatch?.let { event ->
            val snapshot = event.snapshot
            showClipboardOffer(snapshot)
            clipboardOfferState = ClipboardOfferReducer.State(
                lastGeneration = snapshot.generation,
                current = snapshot
            )
            openClipboardReview()
            onClearSharedBatch()
        }
    }

    fun importClipboardStickers(items: List<StagedClipboardItem.Ready>) {
        if (items.isEmpty() || isImportingClipboard) return
        scope.launch {
            isImportingClipboard = true
            val result = repository.importStagedClipboardBatch(items)
            isImportingClipboard = false
            val saved = result.saved.size
            val duplicates = result.duplicates.size
            val failed = result.failed.size
            // Retain only failed staged items in the open sheet for Retry.
            stagedClipboardItems = result.failed.map { it.item }
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
                    haptics.performTick()
                    flashSnackbar("Already in your library.")
                }
                else -> {
                    haptics.performReject()
                    flashSnackbar("Couldn't import clipboard images.")
                }
            }
            if (failed == 0) {
                closeClipboardReview(consumeCurrent = true)
            }
        }
    }

    // Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            haptics.performTick()
            scope.launch { flashSnackbar("Export started…") }
            backupOps.startExport(uri, allStickers, categories)
        }
    }

    // Import Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            haptics.performTick()
            scope.launch { flashSnackbar("Import started…") }
            backupOps.startImport(uri)
        }
    }

    // WhatsApp native packs: summaries are pure grouping (no I/O); encoding
    // happens per tap on IO. Pack files persist so WhatsApp can re-read them.
    val whatsappPackItems = remember(allStickers) {
        allStickers.groupBy { it.category.ifBlank { "General" } }
            .mapKeys { com.hkm.stickhub.util.WhatsAppPackBuilder.packIdentifier(it.key) }
    }
    val whatsappPacks = remember(allStickers) {
        com.hkm.stickhub.util.WhatsAppPackBuilder.summarize(allStickers)
    }
    var preparingWhatsAppPackId by remember { mutableStateOf<String?>(null) }
    val whatsappAddLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // WhatsApp shows its own confirm dialog; on return, report only a
        // confirmed add and stay silent on cancel.
        scope.launch(Dispatchers.IO) {
            val added = whatsappPacks.any { pack ->
                com.hkm.stickhub.util.WhatsAppPackIntents.isPackAdded(context, pack.packId) == true
            }
            if (added) {
                haptics.performConfirm()
                flashSnackbar("Added to WhatsApp")
            }
        }
    }

    // Single consumer for rotation-safe backup outcomes.
    LaunchedEffect(backupWorkState) {
        when (val work = backupWorkState) {
            is BackupWorkState.Idle, is BackupWorkState.Running -> {}
            is BackupWorkState.ImportFinished -> {
                if (work.imported > 0) {
                    haptics.performConfirm()
                    val extra = buildList {
                        if (work.alreadyPresent > 0) {
                            add("${work.alreadyPresent} already present")
                        }
                    }.joinToString(" • ")
                    flashSnackbar(
                        (if (work.imported == 1) "Sticker merged into library!" else "${work.imported} stickers merged into library!") +
                            (if (extra.isNotEmpty()) " ($extra)" else "")
                    )
                } else if (work.alreadyPresent > 0) {
                    haptics.performTick()
                    flashSnackbar("Already in your library.")
                } else {
                    haptics.performReject()
                    flashSnackbar("No valid stickers found in backup!")
                }
                backupOps.acknowledge()
            }
            is BackupWorkState.ExportFinished -> {
                if (work.ok) {
                    haptics.performConfirm()
                    flashSnackbar("Backup exported successfully!")
                } else {
                    haptics.performReject()
                    flashSnackbar("Failed to export backup!")
                }
                backupOps.acknowledge()
            }
            is BackupWorkState.Failed -> {
                haptics.performReject()
                flashSnackbar(work.message)
                backupOps.acknowledge()
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

    // Reorder is a layout capability (standard grid, All tab, no search), deliberately
    // not tied to selection mode: the card detector must survive the normal-to-selection
    // flip so one continuous hold can select at timeout and still become a reorder
    // drag if the finger keeps moving.
    val canReorderStickers = selectedCategory == "All" && searchQuery.isBlank()
    val libraryGridState = rememberLazyGridState()
    val autoScrollEdgePx = with(LocalDensity.current) { 64.dp.toPx() }
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
    val displayedStickers = if (canReorderStickers) reorderPreview ?: filteredStickers else filteredStickers

    // Reconcile the toggle with the real world on every foreground return:
    // a revoked permission stops the service instead of showing a stale ON.
    LaunchedEffect(foregroundTick) {
        if (foregroundTick == 0) return@LaunchedEffect
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            Settings.canDrawOverlays(context)
        if (OverlayService.isRunning && !hasPermission) {
            try {
                context.stopService(Intent(context, OverlayService::class.java))
            } catch (_: Exception) {
            }
            isOverlayRunning = false
        } else {
            isOverlayRunning = OverlayService.isRunning
        }
    }

    /**
     * Starts the overlay service and reports the verified outcome. Never
     * celebrates before [OverlayService.isRunning] actually flips.
     */
    fun requestOverlayStart(onDone: (Boolean) -> Unit = {}) {
        try {
            val serviceIntent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (_: Exception) {
            onDone(false)
            return
        }
        scope.launch {
            kotlinx.coroutines.delay(800)
            val actuallyRunning = OverlayService.isRunning
            isOverlayRunning = actuallyRunning
            onDone(actuallyRunning)
        }
    }

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
                haptics.performToggle(false)
                scope.launch { flashSnackbar("Floating overlay disabled") }
            } else {
                requestOverlayStart { actuallyRunning ->
                    haptics.performToggle(actuallyRunning)
                    scope.launch {
                        flashSnackbar(
                            if (actuallyRunning) "Floating overlay active"
                            else "Couldn't start overlay"
                        )
                    }
                }
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

    // Long-press haptics live in the cards (single buzz guaranteed no matter
    // which detector fires); this callback only flips state.
    val onItemLongClick: (StickerItem) -> Unit = { item ->
        if (isSelectionMode) {
            activeModalRoute = ModalRoute.StickerDetail(item.id)
        } else {
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
                                    // Selection mode is entered by long-pressing a sticker,
                                    // so no dedicated toggle button lives here.
                                    FloatingActionButton(
                                        onClick = {
                                            haptics.performTap()
                                            checkClipboardOffer()
                                            activeModalRoute = ModalRoute.SourceChooser
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
                        // Scaffold insets are zeroed above and the grids carry their own
                        // navigation-bar + FAB padding; the (empty) scaffold padding is
                        // still applied so the Scaffold contract holds by construction.
                        val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        val listBottomPadding = navBarBottomPadding + 96.dp

                        AnimatedContent(
                            targetState = libraryViewMode,
                            transitionSpec = {
                                fadeIn(tween(StickHubMotion.DurationShort)) togetherWith fadeOut(tween(StickHubMotion.DurationShort))
                            },
                            label = "library_view_mode",
                            modifier = Modifier.padding(innerPadding)
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
                                                        haptics.performTick()
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
                                                        haptics.performTick()
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
                                                onOpenLayoutPicker = { activeModalRoute = ModalRoute.LibraryLayout },
                                                filteredStickersCount = filteredStickers.size,
                                                showLibraryCategoryFilters = showLibraryCategoryFilters,
                                                selectedCategory = selectedCategory,
                                                categoryOrder = categoryOrder,
                                                onSelectCategory = { selectedCategory = it },
                                                onAddCategoryClick = { showAddCategoryDialog = true },
                                                onCategoryLongClick = { cat -> categoryToDelete = cat },
                                                onReorderCategories = { newOrder ->
                                                    scope.launch {
                                                        repository.reorderCategories(newOrder)
                                                    }
                                                },
                                                clipboardImageUris = clipboardImageUris,
                                                onImportClipboard = { importSingleClipboardSticker() },
                                                onDismissClipboard = { closeClipboardReview(consumeCurrent = true) },
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
                                                        requestOverlayStart { actuallyRunning ->
                                                            haptics.performToggle(actuallyRunning)
                                                            scope.launch {
                                                                flashSnackbar(
                                                                    if (actuallyRunning) "Quick Stickers activated!"
                                                                    else "Couldn't start overlay"
                                                                )
                                                            }
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
                                                Box(modifier = Modifier.animateItem()) {
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
                                                        haptics.performTick()
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
                                                        haptics.performTick()
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
                                                onOpenLayoutPicker = { activeModalRoute = ModalRoute.LibraryLayout },
                                                filteredStickersCount = filteredStickers.size,
                                                showLibraryCategoryFilters = showLibraryCategoryFilters,
                                                selectedCategory = selectedCategory,
                                                categoryOrder = categoryOrder,
                                                onSelectCategory = { selectedCategory = it },
                                                onAddCategoryClick = { showAddCategoryDialog = true },
                                                onCategoryLongClick = { cat -> categoryToDelete = cat },
                                                onReorderCategories = { newOrder ->
                                                    scope.launch {
                                                        repository.reorderCategories(newOrder)
                                                    }
                                                },
                                                clipboardImageUris = clipboardImageUris,
                                                onImportClipboard = { importSingleClipboardSticker() },
                                                onDismissClipboard = { closeClipboardReview(consumeCurrent = true) },
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
                                                        requestOverlayStart { actuallyRunning ->
                                                            haptics.performToggle(actuallyRunning)
                                                            scope.launch {
                                                                flashSnackbar(
                                                                    if (actuallyRunning) "Quick Stickers activated!"
                                                                    else "Couldn't start overlay"
                                                                )
                                                            }
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
                                                Box(modifier = Modifier.animateItem()) {
                                                    StickerCard(
                                                        sticker = sticker,
                                                        onClick = onItemClick,
                                                        onLongClick = onItemLongClick,
                                                        onReorderStart = if (canReorderStickers) {
                                                            { heldSticker: StickerItem ->
                                                                val activeStickers = reorderPreview ?: allStickers
                                                                val sourceIndex = activeStickers.indexOfFirst { it.id == heldSticker.id }
                                                                if (sourceIndex >= 0) {
                                                                    // Grab buzz is owned by the card detector.
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
                                }

                                StickerLibraryViewMode.LARGE_GRID -> {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = listBottomPadding),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
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
                                                        haptics.performTick()
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
                                                        haptics.performTick()
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
                                                onOpenLayoutPicker = { activeModalRoute = ModalRoute.LibraryLayout },
                                                filteredStickersCount = filteredStickers.size,
                                                showLibraryCategoryFilters = showLibraryCategoryFilters,
                                                selectedCategory = selectedCategory,
                                                categoryOrder = categoryOrder,
                                                onSelectCategory = { selectedCategory = it },
                                                onAddCategoryClick = { showAddCategoryDialog = true },
                                                onCategoryLongClick = { cat -> categoryToDelete = cat },
                                                onReorderCategories = { newOrder ->
                                                    scope.launch {
                                                        repository.reorderCategories(newOrder)
                                                    }
                                                },
                                                clipboardImageUris = clipboardImageUris,
                                                onImportClipboard = { importSingleClipboardSticker() },
                                                onDismissClipboard = { closeClipboardReview(consumeCurrent = true) },
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
                                                        requestOverlayStart { actuallyRunning ->
                                                            haptics.performToggle(actuallyRunning)
                                                            scope.launch {
                                                                flashSnackbar(
                                                                    if (actuallyRunning) "Quick Stickers activated!"
                                                                    else "Couldn't start overlay"
                                                                )
                                                            }
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
                                                Box(modifier = Modifier.animateItem()) {
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
                                }

                                StickerLibraryViewMode.LIST -> {
                                    LazyColumn(
                                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 0.dp, bottom = listBottomPadding),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
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
                                                        haptics.performTick()
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
                                                        haptics.performTick()
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
                                                onOpenLayoutPicker = { activeModalRoute = ModalRoute.LibraryLayout },
                                                filteredStickersCount = filteredStickers.size,
                                                showLibraryCategoryFilters = showLibraryCategoryFilters,
                                                selectedCategory = selectedCategory,
                                                categoryOrder = categoryOrder,
                                                onSelectCategory = { selectedCategory = it },
                                                onAddCategoryClick = { showAddCategoryDialog = true },
                                                onCategoryLongClick = { cat -> categoryToDelete = cat },
                                                onReorderCategories = { newOrder ->
                                                    scope.launch {
                                                        repository.reorderCategories(newOrder)
                                                    }
                                                },
                                                clipboardImageUris = clipboardImageUris,
                                                onImportClipboard = { importSingleClipboardSticker() },
                                                onDismissClipboard = { closeClipboardReview(consumeCurrent = true) },
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
                                                        requestOverlayStart { actuallyRunning ->
                                                            haptics.performToggle(actuallyRunning)
                                                            scope.launch {
                                                                flashSnackbar(
                                                                    if (actuallyRunning) "Quick Stickers activated!"
                                                                    else "Couldn't start overlay"
                                                                )
                                                            }
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
                                                Box(modifier = Modifier.animateItem()) {
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
                onLivePreviewBubbleOpacity = { sendAppearancePreview("bubble", it) },
                popupMasterOpacity = popupMasterOpacity,
                onPopupMasterOpacityChange = { opacity ->
                    popupMasterOpacity = opacity
                    OverlayPreferences.setPopupMasterOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewMasterOpacity = { sendAppearancePreview("master", it) },
                popupSurfaceOpacity = popupSurfaceOpacity,
                onPopupSurfaceOpacityChange = { opacity ->
                    popupSurfaceOpacity = opacity
                    OverlayPreferences.setPopupSurfaceOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewSurfaceOpacity = { sendAppearancePreview("surface", it) },
                popupStickersOpacity = popupStickersOpacity,
                onPopupStickersOpacityChange = { opacity ->
                    popupStickersOpacity = opacity
                    OverlayPreferences.setPopupStickersOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewStickersOpacity = { sendAppearancePreview("stickers", it) },
                popupChromeOpacity = popupChromeOpacity,
                onPopupChromeOpacityChange = { opacity ->
                    popupChromeOpacity = opacity
                    OverlayPreferences.setPopupChromeOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewChromeOpacity = { sendAppearancePreview("chrome", it) },
                popupCloseOpacity = popupCloseOpacity,
                onPopupCloseOpacityChange = { opacity ->
                    popupCloseOpacity = opacity
                    OverlayPreferences.setPopupCloseOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewCloseOpacity = { sendAppearancePreview("close", it) },
                popupResizeOpacity = popupResizeOpacity,
                onPopupResizeOpacityChange = { opacity ->
                    popupResizeOpacity = opacity
                    OverlayPreferences.setPopupResizeOpacity(context, opacity)
                    sendThrottledOverlayUpdate(force = true)
                },
                onLivePreviewResizeOpacity = { sendAppearancePreview("resize", it) },
                stickerShadowStrength = stickerShadowStrength,
                onStickerShadowStrengthChange = { strength ->
                    // Heavy op: re-renders every cached thumbnail. Only on release.
                    stickerShadowStrength = strength
                    OverlayPreferences.setStickerShadowStrength(context, strength)
                    sendShadowOverlayUpdate()
                },
                onLivePreviewShadowStrength = { },
                onApplyAppearancePreset = { preset ->
                    OverlayPreferences.applyAppearancePreset(context, preset)
                    overlayBubbleOpacity = OverlayPreferences.bubbleOpacity(context)
                    popupMasterOpacity = OverlayPreferences.popupMasterOpacity(context)
                    popupSurfaceOpacity = OverlayPreferences.popupSurfaceOpacity(context)
                    popupStickersOpacity = OverlayPreferences.popupStickersOpacity(context)
                    popupChromeOpacity = OverlayPreferences.popupChromeOpacity(context)
                    popupCloseOpacity = OverlayPreferences.popupCloseOpacity(context)
                    popupResizeOpacity = OverlayPreferences.popupResizeOpacity(context)
                    stickerShadowStrength = OverlayPreferences.stickerShadowStrength(context)
                    // Committed update clears any transient preview, then the
                    // heavy shadow re-render runs exactly once.
                    sendLightweightOverlayUpdate()
                    sendShadowOverlayUpdate()
                },
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
                whatsappPacks = whatsappPacks,
                preparingWhatsAppPackId = preparingWhatsAppPackId,
                onAddWhatsAppPack = { packId ->
                    if (preparingWhatsAppPackId != null) return@SettingsScreen
                    preparingWhatsAppPackId = packId
                    scope.launch {
                        val items = whatsappPackItems[packId].orEmpty()
                        val built = withContext(Dispatchers.IO) {
                            val appContext = context.applicationContext
                            com.hkm.stickhub.util.WhatsAppPackBuilder.pruneExcept(
                                appContext,
                                whatsappPackItems.keys
                            )
                            com.hkm.stickhub.util.WhatsAppPackBuilder.buildPack(
                                appContext,
                                packId,
                                whatsappPacks.firstOrNull { it.packId == packId }?.displayName ?: packId,
                                items
                            )
                        }
                        preparingWhatsAppPackId = null
                        if (built == null) {
                            haptics.performReject()
                            flashSnackbar("Couldn't prepare this pack")
                            return@launch
                        }
                        try {
                            whatsappAddLauncher.launch(
                                com.hkm.stickhub.util.WhatsAppPackIntents.enableIntent(built.pack)
                            )
                        } catch (_: android.content.ActivityNotFoundException) {
                            haptics.performReject()
                            flashSnackbar("WhatsApp is not installed")
                        }
                    }
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
                categoryOrder = categoryOrder,
                stickers = allStickers,
                onAddCategory = { name ->
                    scope.launch {
                        if (repository.addCategory(name)) {
                            haptics.performTick()
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
                            haptics.performTick()
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
                            haptics.performTick()
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

    // Unified Modal Coordinator
    when (val route = activeModalRoute) {
        ModalRoute.None -> {}

        ModalRoute.SourceChooser -> {
            AlertDialog(
                onDismissRequest = { activeModalRoute = ModalRoute.None },
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
                                activeModalRoute = ModalRoute.None
                                importSingleClipboardSticker()
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
                                text = if (clipboardImageUris.isEmpty()) {
                                    "No image currently on clipboard"
                                } else {
                                    "Import copied image"
                                }
                            )
                        }
                        Button(
                            onClick = {
                                activeModalRoute = ModalRoute.None
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
                    TextButton(onClick = { activeModalRoute = ModalRoute.None }) {
                        Text("Cancel")
                    }
                }
            )
        }

        ModalRoute.ClipboardReview -> {
            ClipboardImportSheet(
                stagedItems = stagedClipboardItems,
                skippedCount = clipboardSkippedCount,
                isStaging = isStagingClipboard,
                stageProgress = clipboardStageProgress,
                isImporting = isImportingClipboard,
                onImportSelected = { importClipboardStickers(it) },
                onDismiss = { closeClipboardReview(consumeCurrent = false) }
            )
        }

        is ModalRoute.SubjectCutout -> {
            SubjectCutoutSheet(
                imageUri = route.uri,
                sheetState = cutoutSheetState,
                categories = categories,
                onDismiss = { dismissCutoutSheet() },
                onSaveSticker = { bitmap, title, category, tags ->
                    val saved = repository.saveStickerBitmap(bitmap, title, category, tags)
                    if (saved != null) {
                        flashSnackbar("Sticker created successfully!")
                        true
                    } else {
                        flashSnackbar("Failed to create sticker")
                        false
                    }
                },
                onCopySticker = { bitmap ->
                    val copied = withContext(Dispatchers.IO) {
                        ClipboardHelper.copyBitmapToClipboard(context, bitmap)
                    }
                    if (copied) {
                        haptics.performConfirm()
                        flashSnackbar("Sticker copied to clipboard.")
                    } else {
                        haptics.performReject()
                        flashSnackbar("Couldn't copy sticker.")
                    }
                    copied
                },
                onChangeImage = {
                    dismissCutoutSheet {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                }
            )
        }

        is ModalRoute.StickerDetail -> {
            allStickers.find { it.id == route.stickerId }?.let { sticker ->
                StickerDetailBottomSheet(
                    sticker = sticker,
                    sheetState = detailSheetState,
                    categories = categories,
                    onDismiss = {
                        scope.launch {
                            try {
                                detailSheetState.hide()
                            } finally {
                                activeModalRoute = ModalRoute.None
                            }
                        }
                    },
                    onCopy = { stickerToCopy ->
                        val copied = withContext(Dispatchers.IO) {
                            ClipboardHelper.copyStickerToClipboard(context, stickerToCopy)
                        }
                        if (copied) {
                            haptics.performConfirm()
                            repository.recordUsage(stickerToCopy.id)
                            flashSnackbar("Copied to clipboard!")
                        } else {
                            haptics.performReject()
                            flashSnackbar("Couldn't copy sticker.")
                        }
                        copied
                    },
                    onToggleFavorite = { id ->
                        scope.launch {
                            repository.toggleFavorite(id)
                            haptics.performTick()
                        }
                    },
                    onDelete = { id ->
                        scope.launch {
                            try {
                                detailSheetState.hide()
                            } finally {
                                activeModalRoute = ModalRoute.None
                            }
                            repository.deleteSticker(id)
                            haptics.performTick()
                            flashSnackbar("Sticker deleted")
                        }
                    },
                    onUpdateDetails = { id, title, category, tags ->
                        scope.launch {
                            repository.updateSticker(id, title, category, tags)
                            haptics.performTick()
                            flashSnackbar("Updated successfully")
                        }
                    },
                    onOpenStudio = {
                        val targetSticker = sticker
                        scope.launch {
                            try {
                                detailSheetState.hide()
                            } finally {
                                activeModalRoute = ModalRoute.StickerStudio(targetSticker)
                            }
                        }
                    }
                )
            }
        }

        is ModalRoute.StickerStudio -> {
            StickerStudioBottomSheet(
                sticker = route.sticker,
                sheetState = studioSheetState,
                onDismiss = {
                    scope.launch {
                        try {
                            studioSheetState.hide()
                        } finally {
                            activeModalRoute = ModalRoute.None
                        }
                    }
                },
                onSaveNew = { editedBitmap ->
                    scope.launch {
                        try {
                            studioSheetState.hide()
                        } finally {
                            activeModalRoute = ModalRoute.None
                        }
                        val saved = repository.saveStickerBitmap(
                            bitmap = editedBitmap,
                            title = "${route.sticker.title} (Edited)",
                            category = route.sticker.category,
                            tags = route.sticker.tags
                        )
                        if (saved != null) {
                            flashSnackbar("Sticker saved to studio copy!")
                        }
                    }
                },
                onOverwrite = { editedBitmap ->
                    scope.launch {
                        try {
                            studioSheetState.hide()
                        } finally {
                            activeModalRoute = ModalRoute.None
                        }
                        val ok = repository.overwriteStickerBitmap(route.sticker.id, editedBitmap)
                        if (ok) {
                            flashSnackbar("Sticker updated successfully!")
                        }
                    }
                }
            )
        }

        ModalRoute.LibraryLayout -> {
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



    // Add Category Dialog (accessible from CategoryChips quick add)
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            categories = categories,
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { catName ->
                scope.launch {
                    if (repository.addCategory(catName)) {
                        selectedCategory = catName
                        haptics.performTick()
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
        val fallbackHome = CategoryItem.pickDeleteFallback(categories, cat.name)
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
                            haptics.performTick()
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
                            haptics.performTick()
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
    categoryOrder: List<String> = emptyList(),
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
    onReorderCategories: ((List<String>) -> Unit)? = null,
    onReorderCustomCategories: (List<String>) -> Unit = {},
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
                categoryOrder = categoryOrder,
                selectedCategory = selectedCategory,
                onSelectCategory = onSelectCategory,
                onAddCategoryClick = onAddCategoryClick,
                onCategoryLongClick = onCategoryLongClick,
                onReorderCategories = onReorderCategories,
                onReorderCustomCategories = onReorderCustomCategories,
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

                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Ready to import",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Save one copied sticker to your library",
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
                                    "Save",
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
