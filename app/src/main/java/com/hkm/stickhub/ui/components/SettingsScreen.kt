package com.hkm.stickhub.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.hkm.stickhub.ui.theme.AppVisualTheme
import com.hkm.stickhub.ui.theme.BotanicalColors
import com.hkm.stickhub.ui.theme.SketchbookColors
import com.hkm.stickhub.ui.theme.notebookPaperLines
import com.hkm.stickhub.ui.theme.ThemePreferences
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.hkm.stickhub.ui.haptics.rememberStickHubHaptics
import com.hkm.stickhub.ui.library.StickerLibraryLayoutPickerSheet
import com.hkm.stickhub.ui.library.StickerLibraryViewMode
import com.hkm.stickhub.ui.theme.AppThemeMode
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    visualTheme: AppVisualTheme = AppVisualTheme.DEFAULT,
    onVisualThemeChange: (AppVisualTheme) -> Unit = {},
    libraryViewMode: StickerLibraryViewMode,
    onLibraryViewModeChange: (StickerLibraryViewMode) -> Unit,
    showLibrarySearch: Boolean,
    onToggleShowSearch: (Boolean) -> Unit,
    showLibraryCategoryFilters: Boolean,
    onToggleShowCategoryFilters: (Boolean) -> Unit,
    isOverlayRunning: Boolean,
    onToggleOverlay: () -> Unit,
    overlayBubbleSizeDp: Float,
    onOverlayBubbleSizeChange: (Float) -> Unit,
    showQuickStickersTitle: Boolean,
    onToggleShowTitle: (Boolean) -> Unit,
    showQuickStickersSearch: Boolean,
    onToggleShowQuickSearch: (Boolean) -> Unit,
    showQuickStickersCategories: Boolean,
    onToggleShowQuickCategories: (Boolean) -> Unit,
    stickerCount: Int,
    categoryCount: Int,
    storageSizeBytes: Long,
    onNavigateToCategoryManagement: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberStickHubHaptics()
    var previewBubbleSizeDp by remember(overlayBubbleSizeDp) {
        mutableFloatStateOf(overlayBubbleSizeDp)
    }

    val sheetCoroutineScope = rememberCoroutineScope()
    val layoutPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showLayoutPicker by remember { mutableStateOf(false) }
    var pendingLayoutMode by remember { mutableStateOf<StickerLibraryViewMode?>(null) }
    var isDismissingPicker by remember { mutableStateOf(false) }

    fun dismissLayoutPicker() {
        if (isDismissingPicker) return
        isDismissingPicker = true
        sheetCoroutineScope.launch {
            try {
                layoutPickerSheetState.hide()
            } finally {
                showLayoutPicker = false
                pendingLayoutMode = null
                isDismissingPicker = false
            }
        }
    }

    fun selectLayoutMode(newMode: StickerLibraryViewMode) {
        if (isDismissingPicker) return
        if (newMode == libraryViewMode) {
            dismissLayoutPicker()
            return
        }
        pendingLayoutMode = newMode
        isDismissingPicker = true
        haptics.performCopyAck()
        sheetCoroutineScope.launch {
            try {
                layoutPickerSheetState.hide()
            } finally {
                showLayoutPicker = false
                pendingLayoutMode = null
                isDismissingPicker = false
                onLibraryViewModeChange(newMode)
            }
        }
    }

    BackHandler(enabled = showLayoutPicker) {
        dismissLayoutPicker()
    }

    val navBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = navBarsBottom + 32.dp
            )
        ) {
            item(key = "settings_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (showLayoutPicker) {
                                dismissLayoutPicker()
                            } else {
                                haptics.performNavigationTap()
                                onBack()
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_arrow_left),
                            contentDescription = "Back to Library",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Personalize your sticker studio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item(key = "section_appearance") {
                SectionHeader("APPEARANCE")

                Text(
                    text = "Color theme",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val context = LocalContext.current
                val isDark = ThemePreferences.resolveIsDark(context, themeMode)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ColorThemeCard(
                        theme = AppVisualTheme.DEFAULT,
                        selected = visualTheme == AppVisualTheme.DEFAULT,
                        swatches = if (isDark) {
                            listOf(Color(0xFFD4D8E2), Color(0xFF383E4B), Color(0xFF1A1C20), Color(0xFFC4C7D0))
                        } else {
                            listOf(Color(0xFF2B303A), Color(0xFFE2E6EE), Color(0xFFFFFFFF), Color(0xFF4A4E57))
                        },
                        onClick = {
                            if (visualTheme != AppVisualTheme.DEFAULT) {
                                haptics.performCopyAck()
                                onVisualThemeChange(AppVisualTheme.DEFAULT)
                            }
                        }
                    )

                    ColorThemeCard(
                        theme = AppVisualTheme.HERBARIUM,
                        selected = visualTheme == AppVisualTheme.HERBARIUM,
                        swatches = if (isDark) {
                            listOf(
                                BotanicalColors.DarkSagePrimary,
                                BotanicalColors.DarkPrimaryContainer,
                                BotanicalColors.DarkPaperSurface,
                                BotanicalColors.DarkMutedRoseTertiary
                            )
                        } else {
                            listOf(
                                BotanicalColors.LightLeafGreenPrimary,
                                BotanicalColors.LightSagePrimaryContainer,
                                BotanicalColors.LightWarmPaperSurface,
                                BotanicalColors.LightMutedTerracottaTertiary
                            )
                        },
                        onClick = {
                            if (visualTheme != AppVisualTheme.HERBARIUM) {
                                haptics.performCopyAck()
                                onVisualThemeChange(AppVisualTheme.HERBARIUM)
                            }
                        }
                    )

                    ColorThemeCard(
                        theme = AppVisualTheme.SKETCHBOOK,
                        selected = visualTheme == AppVisualTheme.SKETCHBOOK,
                        swatches = if (isDark) {
                            listOf(
                                SketchbookColors.DarkPaperSurface,
                                SketchbookColors.DarkPrimaryBlue,
                                SketchbookColors.DarkRedAccent,
                                SketchbookColors.DarkMutedHighlighter
                            )
                        } else {
                            listOf(
                                SketchbookColors.LightPaperSurface,
                                SketchbookColors.LightNavyInkPrimary,
                                SketchbookColors.LightMutedRedAccent,
                                SketchbookColors.LightHighlighterYellow
                            )
                        },
                        onClick = {
                            if (visualTheme != AppVisualTheme.SKETCHBOOK) {
                                haptics.performCopyAck()
                                onVisualThemeChange(AppVisualTheme.SKETCHBOOK)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Theme mode",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ThemeModeSelector(
                    themeMode = themeMode,
                    onThemeModeChange = { newMode ->
                        if (newMode != themeMode) {
                            haptics.performCopyAck()
                            onThemeModeChange(newMode)
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Live preview",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SpecimenPreviewSurface(visualTheme = visualTheme)

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            showLayoutPicker = true
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(libraryViewMode.iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Library layout",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = libraryViewMode.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_chevron_right),
                            contentDescription = "Change layout",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    title = "Show search",
                    subtitle = "Show search field at the top of library",
                    checked = showLibrarySearch,
                    onCheckedChange = {
                        haptics.performCopyAck()
                        onToggleShowSearch(it)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsToggleRow(
                    title = "Show category filters",
                    subtitle = "Show category tag rail below search",
                    checked = showLibraryCategoryFilters,
                    onCheckedChange = {
                        haptics.performCopyAck()
                        onToggleShowCategoryFilters(it)
                    }
                )

                SettingsDivider()
            }

            item(key = "section_quick_stickers") {
                SectionHeader("QUICK STICKERS")

                SettingsToggleRow(
                    title = "Floating overlay",
                    subtitle = "Floating bubble for quick sticker access anywhere",
                    checked = isOverlayRunning,
                    onCheckedChange = {
                        haptics.performCopyAck()
                        onToggleOverlay()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bubble size",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${previewBubbleSizeDp.toInt()} dp",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = previewBubbleSizeDp,
                        onValueChange = { previewBubbleSizeDp = it },
                        onValueChangeFinished = {
                            if (abs(previewBubbleSizeDp - overlayBubbleSizeDp) >= 0.5f) {
                                haptics.performCopyAck()
                                onOverlayBubbleSizeChange(previewBubbleSizeDp)
                            }
                        },
                        valueRange = 36f..84f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    title = "Show title in popup",
                    subtitle = "Display app name at the top of quick stickers",
                    checked = showQuickStickersTitle,
                    onCheckedChange = {
                        haptics.performCopyAck()
                        onToggleShowTitle(it)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsToggleRow(
                    title = "Show search in popup",
                    subtitle = "Display search input in quick stickers",
                    checked = showQuickStickersSearch,
                    onCheckedChange = {
                        haptics.performCopyAck()
                        onToggleShowQuickSearch(it)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsToggleRow(
                    title = "Show categories in popup",
                    subtitle = "Display category tabs in quick stickers",
                    checked = showQuickStickersCategories,
                    onCheckedChange = {
                        haptics.performCopyAck()
                        onToggleShowQuickCategories(it)
                    }
                )

                SettingsDivider()
            }

            item(key = "section_data") {
                SectionHeader("LIBRARY & DATA")

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            haptics.performNavigationTap()
                            onNavigateToCategoryManagement()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_tag),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Manage categories",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$categoryCount categories available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_chevron_right),
                            contentDescription = "Manage",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Stickers", value = "$stickerCount")
                        StatItem(label = "Categories", value = "$categoryCount")
                        StatItem(label = "Storage", value = formatStorageSize(storageSizeBytes))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            haptics.performNavigationTap()
                            onExportBackup()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_upload),
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export")
                    }

                    FilledTonalButton(
                        onClick = {
                            haptics.performNavigationTap()
                            onImportBackup()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_download),
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import")
                    }
                }

                SettingsDivider()
            }

            // 4. PRIVACY & STORAGE
            item(key = "section_privacy") {
                SectionHeader("PRIVACY")

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(LucideR.drawable.lucide_ic_shield_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "100% Offline & Private",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All stickers, tags, and preferences are stored exclusively on your device. StickHub has zero analytics, zero external network servers, and zero trackers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        if (showLayoutPicker) {
            StickerLibraryLayoutPickerSheet(
                currentMode = libraryViewMode,
                pendingMode = pendingLayoutMode,
                sheetState = layoutPickerSheetState,
                isDismissing = isDismissingPicker,
                onSelectMode = { selectLayoutMode(it) },
                onDismissRequest = { dismissLayoutPicker() }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatStorageSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.1f MB", bytes.toDouble() / (1024 * 1024))
    }
}

@Composable
private fun ColorThemeCard(
    theme: AppVisualTheme,
    selected: Boolean,
    swatches: List<Color>,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                role = Role.RadioButton,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = theme.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (swatch in swatches) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(swatch)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = theme.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_check),
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecimenPreviewSurface(visualTheme: AppVisualTheme) {
    val context = LocalContext.current
    val isDark = ThemePreferences.resolveIsDark(context, ThemePreferences.getThemeMode(context))

    val headerTitle = when (visualTheme) {
        AppVisualTheme.DEFAULT -> "SYSTEM PALETTE"
        AppVisualTheme.HERBARIUM -> "BOTANICAL SPECIMEN"
        AppVisualTheme.SKETCHBOOK -> "SKETCH-NOTE SPECIMEN"
    }

    val sampleCardText = when (visualTheme) {
        AppVisualTheme.DEFAULT -> "Material 3 card"
        AppVisualTheme.HERBARIUM -> "Warm parchment paper"
        AppVisualTheme.SKETCHBOOK -> "Ruled notebook paper"
    }

    val iconRes = when (visualTheme) {
        AppVisualTheme.DEFAULT -> LucideR.drawable.lucide_ic_sparkles
        AppVisualTheme.HERBARIUM -> LucideR.drawable.lucide_ic_leaf
        AppVisualTheme.SKETCHBOOK -> LucideR.drawable.lucide_ic_pen_tool
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (visualTheme == AppVisualTheme.SKETCHBOOK) {
                    Modifier.notebookPaperLines(enabled = true, isDark = isDark)
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = visualTheme.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = sampleCardText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }

                FilledTonalButton(
                    onClick = { /* Interactive preview */ },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sample",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
