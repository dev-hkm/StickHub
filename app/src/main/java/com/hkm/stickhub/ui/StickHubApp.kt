package com.hkm.stickhub.ui

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
    onThemeModeChange: (AppThemeMode) -> Unit = {}
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Navigation Controller and Route
    val navigator = remember { AppNavigator(initialRoute = AppRoute.LIBRARY) }
    var currentRoute by rememberSaveable { mutableStateOf(AppRoute.LIBRARY) }

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
    var showQuickStickersTitle by remember { mutableStateOf(OverlayPreferences.showTitle(context)) }
    var showQuickStickersSearch by remember { mutableStateOf(OverlayPreferences.showSearch(context)) }
    var showQuickStickersCategories by remember { mutableStateOf(OverlayPreferences.showCategories(context)) }
    var clipboardImageUri by remember { mutableStateOf<Uri?>(null) }
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

    // Periodic check for clipboard image changes
    LaunchedEffect(Unit) {
        while (true) {
            val uri = withContext(Dispatchers.IO) {
                ClipboardHelper.getClipboardImageUri(context)
            }
            if (uri != clipboardImageUri) {
                clipboardImageUri = uri
            }
            delay(2000)
        }
    }

    fun importClipboardSticker(uri: Uri) {
        scope.launch {
            val importResult = repository.importClipboardSticker(uri)
            clipboardImageUri = null
            when (importResult) {
                is ClipboardImportResult.Saved -> {
                    haptics.performConfirm()
                    snackbarHostState.showSnackbar("Sticker saved to library!")
                }
                is ClipboardImportResult.Duplicate -> {
                    haptics.performTap()
                    snackbarHostState.showSnackbar("This sticker is already in your library.")
                }
                is ClipboardImportResult.InvalidSource -> {
                    haptics.performReject()
                    snackbarHostState.showSnackbar(importResult.reason)
                }
                is ClipboardImportResult.Failed -> {
                    haptics.performReject()
                    snackbarHostState.showSnackbar(importResult.reason)
                }
                is ClipboardImportResult.OwnSource -> {
                    // Ignore own source
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
                    snackbarHostState.showSnackbar("Backup exported successfully!")
                } else {
                    haptics.performReject()
                    snackbarHostState.showSnackbar("Failed to export backup!")
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
                    snackbarHostState.showSnackbar("Merged $count stickers from backup!")
                } else {
                    haptics.performReject()
                    snackbarHostState.showSnackbar("No valid stickers found in backup!")
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
    LaunchedEffect(selectedCategory) {
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
                snackbarHostState.showSnackbar("Please grant 'Display over other apps' permission")
            }
        } else {
            val serviceIntent = Intent(context, OverlayService::class.java)
            if (OverlayService.isRunning) {
                context.stopService(serviceIntent)
                isOverlayRunning = false
                haptics.performCopyAck()
                scope.launch { snackbarHostState.showSnackbar("Floating overlay disabled") }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                isOverlayRunning = true
                haptics.performCopyAck()
                scope.launch { snackbarHostState.showSnackbar("Floating overlay active") }
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
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
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
            haptics.performSelection()
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
                .zIndex(0f),
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
                                                clipboardImageUri = withContext(Dispatchers.IO) {
                                                    ClipboardHelper.getClipboardImageUri(context)
                                                }
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
                                                        snackbarHostState.showSnackbar("Added to favorites")
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
                                                        snackbarHostState.showSnackbar("Moved to $catName")
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
                                                clipboardImageUri = clipboardImageUri,
                                                onSaveClipboardDirectly = { uri -> importClipboardSticker(uri) },
                                                onDismissClipboard = { clipboardImageUri = null },
                                                onLaunchCutout = { uri ->
                                                    activeCutoutUri = uri
                                                    clipboardImageUri = null
                                                },
                                                appFocusManager = appFocusManager
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
                                                EmptyLibraryView(searchQuery = searchQuery)
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
                                                        snackbarHostState.showSnackbar("Added to favorites")
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
                                                        snackbarHostState.showSnackbar("Moved to $catName")
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
                                                clipboardImageUri = clipboardImageUri,
                                                onSaveClipboardDirectly = { uri -> importClipboardSticker(uri) },
                                                onDismissClipboard = { clipboardImageUri = null },
                                                onLaunchCutout = { uri ->
                                                    activeCutoutUri = uri
                                                    clipboardImageUri = null
                                                },
                                                appFocusManager = appFocusManager
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
                                                EmptyLibraryView(searchQuery = searchQuery)
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
                                                                    snackbarHostState.showSnackbar("Couldn’t save sticker order")
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
                                                        snackbarHostState.showSnackbar("Added to favorites")
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
                                                        snackbarHostState.showSnackbar("Moved to $catName")
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
                                                clipboardImageUri = clipboardImageUri,
                                                onSaveClipboardDirectly = { uri -> importClipboardSticker(uri) },
                                                onDismissClipboard = { clipboardImageUri = null },
                                                onLaunchCutout = { uri ->
                                                    activeCutoutUri = uri
                                                    clipboardImageUri = null
                                                },
                                                appFocusManager = appFocusManager
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
                                                EmptyLibraryView(searchQuery = searchQuery)
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
                                                        snackbarHostState.showSnackbar("Added to favorites")
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
                                                        snackbarHostState.showSnackbar("Moved to $catName")
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
                                                clipboardImageUri = clipboardImageUri,
                                                onSaveClipboardDirectly = { uri -> importClipboardSticker(uri) },
                                                onDismissClipboard = { clipboardImageUri = null },
                                                onLaunchCutout = { uri ->
                                                    activeCutoutUri = uri
                                                    clipboardImageUri = null
                                                },
                                                appFocusManager = appFocusManager
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
                                                EmptyLibraryView(searchQuery = searchQuery)
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
                            snackbarHostState.showSnackbar("Category '' created")
                        } else {
                            haptics.performReject()
                            snackbarHostState.showSnackbar("Failed to create category")
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
                            snackbarHostState.showSnackbar("Renamed to ''")
                        } else {
                            haptics.performReject()
                            snackbarHostState.showSnackbar("Failed to rename category")
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
                            snackbarHostState.showSnackbar("Category '' deleted")
                        } else {
                            haptics.performReject()
                            snackbarHostState.showSnackbar("Failed to delete category")
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
                            val uri = clipboardImageUri ?: return@Button
                            showCreateSourceDialog = false
                            importClipboardSticker(uri)
                        },
                        enabled = clipboardImageUri != null,
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
                            text = if (clipboardImageUri != null) "Import copied image directly" else "No image currently on clipboard"
                        )
                    }
                    Button(
                        onClick = {
                            val uri = clipboardImageUri ?: return@Button
                            showCreateSourceDialog = false
                            activeCutoutUri = uri
                            clipboardImageUri = null
                        },
                        enabled = clipboardImageUri != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_scissors),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cut out copied image (Google Photos)")
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

    // Subject Cutout Sheet
    activeCutoutUri?.let { uri ->
        SubjectCutoutSheet(
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
                        snackbarHostState.showSnackbar("Sticker created successfully!")
                    } else {
                        haptics.performReject()
                        snackbarHostState.showSnackbar("Failed to create sticker")
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
                    snackbarHostState.showSnackbar("Copied to clipboard!")
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
                    snackbarHostState.showSnackbar("Sticker deleted")
                }
            },
            onUpdateDetails = { id, title, category, tags ->
                scope.launch {
                    repository.updateSticker(id, title, category, tags)
                    haptics.performConfirm()
                    snackbarHostState.showSnackbar("Updated successfully")
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
                        snackbarHostState.showSnackbar("Sticker saved to studio copy!")
                    }
                }
            },
            onOverwrite = { editedBitmap ->
                scope.launch {
                    val ok = repository.overwriteStickerBitmap(sticker.id, editedBitmap)
                    selectedStickerForStudio = null
                    if (ok) {
                        snackbarHostState.showSnackbar("Sticker updated successfully!")
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
                        snackbarHostState.showSnackbar("Category '$catName' created")
                    } else {
                        haptics.performReject()
                        snackbarHostState.showSnackbar("Failed to create category")
                    }
                }
            }
        )
    }

    // Delete Category Confirmation Dialog (from long click on CategoryChips)
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete '${cat.name}'? Stickers in this category will be moved to 'General'.") },
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
                            snackbarHostState.showSnackbar("Category deleted, stickers moved to General")
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
                            snackbarHostState.showSnackbar("Deleted $count stickers")
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
    clipboardImageUri: Uri?,
    onSaveClipboardDirectly: (Uri) -> Unit,
    onDismissClipboard: () -> Unit,
    onLaunchCutout: (Uri) -> Unit,
    appFocusManager: androidx.compose.ui.focus.FocusManager,
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
            visible = clipboardImageUri != null && !isSelectionMode,
            enter = StickHubMotion.BannerEnter,
            exit = StickHubMotion.BannerExit
        ) {
            clipboardImageUri?.let { uri ->
                val context = LocalContext.current
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
                                        .data(uri)
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
                                    text = "Save sticker or isolate subject",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onLaunchCutout(uri) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_scissors),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cut out", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }

                            Button(
                                onClick = { onSaveClipboardDirectly(uri) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(LucideR.drawable.lucide_ic_save),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                contentDescription = null,
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
private fun EmptyLibraryView(searchQuery: String) {
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
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_folder_open),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.outline
            )
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
