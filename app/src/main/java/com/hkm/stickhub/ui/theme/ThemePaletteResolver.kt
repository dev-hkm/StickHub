package com.hkm.stickhub.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Curated color tokens for the Herbarium ("Vintage Botanical Scientific Illustration") theme.
 */
object BotanicalColors {
    // Light Palette
    val LightParchmentBackground = Color(0xFFF0E5D3)
    val LightWarmPaperSurface = Color(0xFFFAF4EA)
    val LightPaperSurfaceVariant = Color(0xFFEAE0CE)
    val LightLeafGreenPrimary = Color(0xFF4A6741)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightSagePrimaryContainer = Color(0xFFDDE6D7)
    val LightOnPrimaryContainer = Color(0xFF132B0F)
    val LightEarthBrownSecondary = Color(0xFF5D4037)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightSecondaryContainer = Color(0xFFE7DDD3)
    val LightOnSecondaryContainer = Color(0xFF1D140F)
    val LightMutedTerracottaTertiary = Color(0xFFBF6B63)
    val LightOnTertiary = Color(0xFFFFFFFF)
    val LightTertiaryContainer = Color(0xFFF6DCD9)
    val LightOnTertiaryContainer = Color(0xFF3C100E)
    val LightBotanicalInkText = Color(0xFF1A1512)
    val LightMutedInkText = Color(0xFF62564B)
    val LightOutline = Color(0xFFA89480)
    val LightOutlineVariant = Color(0xFFC8B8A6)
    val LightMutedRedError = Color(0xFF9C413D)
    val LightOnError = Color(0xFFFFFFFF)
    val LightErrorContainer = Color(0xFFFFDAD6)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette
    val DarkEspressoBackground = Color(0xFF171511)
    val DarkPaperSurface = Color(0xFF211C17)
    val DarkPaperSurfaceVariant = Color(0xFF2E2720)
    val DarkSagePrimary = Color(0xFFB7C9AA)
    val DarkOnPrimary = Color(0xFF23351E)
    val DarkPrimaryContainer = Color(0xFF354535)
    val DarkOnPrimaryContainer = Color(0xFFCCE1C0)
    val DarkParchmentBrownSecondary = Color(0xFFD5BA9F)
    val DarkOnSecondary = Color(0xFF3B2B1B)
    val DarkSecondaryContainer = Color(0xFF4D3A29)
    val DarkOnSecondaryContainer = Color(0xFFF2D8BD)
    val DarkMutedRoseTertiary = Color(0xFFE0A097)
    val DarkOnTertiary = Color(0xFF431B16)
    val DarkTertiaryContainer = Color(0xFF5A2C26)
    val DarkOnTertiaryContainer = Color(0xFFFFDAD5)
    val DarkParchmentText = Color(0xFFF0E5D3)
    val DarkMutedText = Color(0xFFD2C3B1)
    val DarkOutline = Color(0xFF9B8874)
    val DarkOutlineVariant = Color(0xFF50453A)
    val DarkMutedRedError = Color(0xFFFFB4AB)
    val DarkOnError = Color(0xFF690005)
    val DarkErrorContainer = Color(0xFF93000A)
    val DarkOnErrorContainer = Color(0xFFFFDAD6)
}

/**
 * Curated color tokens for the Sketchbook ("Hand-Drawn Sketch-Note") theme.
 */
object SketchbookColors {
    // Light Palette
    val LightNotebookBackground = Color(0xFFF4F1EA)
    val LightPaperSurface = Color(0xFFFFFCF5)
    val LightPaperSurfaceVariant = Color(0xFFEBE6DC)
    val LightNavyInkPrimary = Color(0xFF0E2A5C)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightPrimaryContainer = Color(0xFFDCE7F7)
    val LightOnPrimaryContainer = Color(0xFF081B3C)
    val LightBallpointBlueSecondary = Color(0xFF3158A6)
    val LightOnSecondary = Color(0xFFFFFFFF)
    val LightSecondaryContainer = Color(0xFFD7E2F7)
    val LightOnSecondaryContainer = Color(0xFF0D1D3A)
    val LightPencilGraphite = Color(0xFF333333)
    val LightMutedGraphite = Color(0xFF67635E)
    val LightMutedRedAccent = Color(0xFFB84942)
    val LightOnTertiary = Color(0xFFFFFFFF)
    val LightTertiaryContainer = Color(0xFFF9DCDA)
    val LightOnTertiaryContainer = Color(0xFF3D100D)
    val LightHighlighterYellow = Color(0xFFF3E899)
    val LightOnHighlighterYellow = Color(0xFF2B2600)
    val LightOutline = Color(0xFF9B968D)
    val LightOutlineVariant = Color(0xFFCCC6BC)
    val LightMutedRedError = Color(0xFFB84942)
    val LightOnError = Color(0xFFFFFFFF)
    val LightErrorContainer = Color(0xFFFFDAD6)
    val LightOnErrorContainer = Color(0xFF410002)

    // Dark Palette
    val DarkDeepNotebookBackground = Color(0xFF121A29)
    val DarkPaperSurface = Color(0xFF1B2433)
    val DarkPaperSurfaceVariant = Color(0xFF243044)
    val DarkPrimaryBlue = Color(0xFFB8CCFF)
    val DarkOnPrimary = Color(0xFF002B73)
    val DarkPrimaryContainer = Color(0xFF2C4679)
    val DarkOnPrimaryContainer = Color(0xFFDCE7F7)
    val DarkSecondaryInkBlue = Color(0xFF9FC0FF)
    val DarkOnSecondary = Color(0xFF002A66)
    val DarkSecondaryContainer = Color(0xFF1C3A6E)
    val DarkOnSecondaryContainer = Color(0xFFD7E3FF)
    val DarkMainPaperText = Color(0xFFF4F1EA)
    val DarkMutedPaperText = Color(0xFFCDC7BD)
    val DarkRedAccent = Color(0xFFFFB4AE)
    val DarkOnTertiary = Color(0xFF561E1A)
    val DarkTertiaryContainer = Color(0xFF73332E)
    val DarkOnTertiaryContainer = Color(0xFFFFDAD6)
    val DarkMutedHighlighter = Color(0xFFD7C96B)
    val DarkOnHighlighter = Color(0xFF353000)
    val DarkOutline = Color(0xFFA29C93)
    val DarkOutlineVariant = Color(0xFF454D5D)
    val DarkMutedRedError = Color(0xFFFFB4AE)
    val DarkOnError = Color(0xFF690005)
    val DarkErrorContainer = Color(0xFF93000A)
    val DarkOnErrorContainer = Color(0xFFFFDAD6)
}

val HerbariumLightColorScheme: ColorScheme = lightColorScheme(
    primary = BotanicalColors.LightLeafGreenPrimary,
    onPrimary = BotanicalColors.LightOnPrimary,
    primaryContainer = BotanicalColors.LightSagePrimaryContainer,
    onPrimaryContainer = BotanicalColors.LightOnPrimaryContainer,
    secondary = BotanicalColors.LightEarthBrownSecondary,
    onSecondary = BotanicalColors.LightOnSecondary,
    secondaryContainer = BotanicalColors.LightSecondaryContainer,
    onSecondaryContainer = BotanicalColors.LightOnSecondaryContainer,
    tertiary = BotanicalColors.LightMutedTerracottaTertiary,
    onTertiary = BotanicalColors.LightOnTertiary,
    tertiaryContainer = BotanicalColors.LightTertiaryContainer,
    onTertiaryContainer = BotanicalColors.LightOnTertiaryContainer,
    background = BotanicalColors.LightParchmentBackground,
    onBackground = BotanicalColors.LightBotanicalInkText,
    surface = BotanicalColors.LightWarmPaperSurface,
    onSurface = BotanicalColors.LightBotanicalInkText,
    surfaceVariant = BotanicalColors.LightPaperSurfaceVariant,
    onSurfaceVariant = BotanicalColors.LightMutedInkText,
    outline = BotanicalColors.LightOutline,
    outlineVariant = BotanicalColors.LightOutlineVariant,
    error = BotanicalColors.LightMutedRedError,
    onError = BotanicalColors.LightOnError,
    errorContainer = BotanicalColors.LightErrorContainer,
    onErrorContainer = BotanicalColors.LightOnErrorContainer
)

val HerbariumDarkColorScheme: ColorScheme = darkColorScheme(
    primary = BotanicalColors.DarkSagePrimary,
    onPrimary = BotanicalColors.DarkOnPrimary,
    primaryContainer = BotanicalColors.DarkPrimaryContainer,
    onPrimaryContainer = BotanicalColors.DarkOnPrimaryContainer,
    secondary = BotanicalColors.DarkParchmentBrownSecondary,
    onSecondary = BotanicalColors.DarkOnSecondary,
    secondaryContainer = BotanicalColors.DarkSecondaryContainer,
    onSecondaryContainer = BotanicalColors.DarkOnSecondaryContainer,
    tertiary = BotanicalColors.DarkMutedRoseTertiary,
    onTertiary = BotanicalColors.DarkOnTertiary,
    tertiaryContainer = BotanicalColors.DarkTertiaryContainer,
    onTertiaryContainer = BotanicalColors.DarkOnTertiaryContainer,
    background = BotanicalColors.DarkEspressoBackground,
    onBackground = BotanicalColors.DarkParchmentText,
    surface = BotanicalColors.DarkPaperSurface,
    onSurface = BotanicalColors.DarkParchmentText,
    surfaceVariant = BotanicalColors.DarkPaperSurfaceVariant,
    onSurfaceVariant = BotanicalColors.DarkMutedText,
    outline = BotanicalColors.DarkOutline,
    outlineVariant = BotanicalColors.DarkOutlineVariant,
    error = BotanicalColors.DarkMutedRedError,
    onError = BotanicalColors.DarkOnError,
    errorContainer = BotanicalColors.DarkErrorContainer,
    onErrorContainer = BotanicalColors.DarkOnErrorContainer
)

val SketchbookLightColorScheme: ColorScheme = lightColorScheme(
    primary = SketchbookColors.LightNavyInkPrimary,
    onPrimary = SketchbookColors.LightOnPrimary,
    primaryContainer = SketchbookColors.LightPrimaryContainer,
    onPrimaryContainer = SketchbookColors.LightOnPrimaryContainer,
    secondary = SketchbookColors.LightBallpointBlueSecondary,
    onSecondary = SketchbookColors.LightOnSecondary,
    secondaryContainer = SketchbookColors.LightSecondaryContainer,
    onSecondaryContainer = SketchbookColors.LightOnSecondaryContainer,
    tertiary = SketchbookColors.LightMutedRedAccent,
    onTertiary = SketchbookColors.LightOnTertiary,
    tertiaryContainer = SketchbookColors.LightTertiaryContainer,
    onTertiaryContainer = SketchbookColors.LightOnTertiaryContainer,
    background = SketchbookColors.LightNotebookBackground,
    onBackground = SketchbookColors.LightPencilGraphite,
    surface = SketchbookColors.LightPaperSurface,
    onSurface = SketchbookColors.LightPencilGraphite,
    surfaceVariant = SketchbookColors.LightPaperSurfaceVariant,
    onSurfaceVariant = SketchbookColors.LightMutedGraphite,
    outline = SketchbookColors.LightOutline,
    outlineVariant = SketchbookColors.LightOutlineVariant,
    error = SketchbookColors.LightMutedRedError,
    onError = SketchbookColors.LightOnError,
    errorContainer = SketchbookColors.LightErrorContainer,
    onErrorContainer = SketchbookColors.LightOnErrorContainer
)

val SketchbookDarkColorScheme: ColorScheme = darkColorScheme(
    primary = SketchbookColors.DarkPrimaryBlue,
    onPrimary = SketchbookColors.DarkOnPrimary,
    primaryContainer = SketchbookColors.DarkPrimaryContainer,
    onPrimaryContainer = SketchbookColors.DarkOnPrimaryContainer,
    secondary = SketchbookColors.DarkSecondaryInkBlue,
    onSecondary = SketchbookColors.DarkOnSecondary,
    secondaryContainer = SketchbookColors.DarkSecondaryContainer,
    onSecondaryContainer = SketchbookColors.DarkOnSecondaryContainer,
    tertiary = SketchbookColors.DarkRedAccent,
    onTertiary = SketchbookColors.DarkOnTertiary,
    tertiaryContainer = SketchbookColors.DarkTertiaryContainer,
    onTertiaryContainer = SketchbookColors.DarkOnTertiaryContainer,
    background = SketchbookColors.DarkDeepNotebookBackground,
    onBackground = SketchbookColors.DarkMainPaperText,
    surface = SketchbookColors.DarkPaperSurface,
    onSurface = SketchbookColors.DarkMainPaperText,
    surfaceVariant = SketchbookColors.DarkPaperSurfaceVariant,
    onSurfaceVariant = SketchbookColors.DarkMutedPaperText,
    outline = SketchbookColors.DarkOutline,
    outlineVariant = SketchbookColors.DarkOutlineVariant,
    error = SketchbookColors.DarkMutedRedError,
    onError = SketchbookColors.DarkOnError,
    errorContainer = SketchbookColors.DarkErrorContainer,
    onErrorContainer = SketchbookColors.DarkOnErrorContainer
)

/**
 * Shared palette definition for Android View system (OverlayService).
 */
data class OverlayPalette(
    val surfaceColor: Int,
    val surfaceVariantColor: Int,
    val primaryColor: Int,
    val primaryContainerColor: Int,
    val onPrimaryContainerColor: Int,
    val textColor: Int,
    val mutedTextColor: Int,
    val outlineColor: Int,
    val accentColor: Int,
    val selectedChipContainerColor: Int,
    val selectedChipContentColor: Int,
    val selectedChipStrokeColor: Int?,
    val isDark: Boolean,
    val visualTheme: AppVisualTheme
)

object ThemePaletteResolver {

    /**
     * Resolves an [OverlayPalette] for the given visual theme and dark mode flag.
     * Compatible with Android Views in [OverlayService].
     */
    fun resolveOverlayPalette(
        context: Context,
        visualTheme: AppVisualTheme,
        isDark: Boolean
    ): OverlayPalette {
        return when (visualTheme) {
            AppVisualTheme.SKETCHBOOK -> {
                if (isDark) {
                    val primary = SketchbookColors.DarkPrimaryBlue.toArgb()
                    val container = SketchbookColors.DarkPrimaryContainer.toArgb()
                    val onContainer = SketchbookColors.DarkOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = SketchbookColors.DarkPaperSurface.toArgb(),
                        surfaceVariantColor = SketchbookColors.DarkPaperSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = SketchbookColors.DarkMainPaperText.toArgb(),
                        mutedTextColor = SketchbookColors.DarkMutedPaperText.toArgb(),
                        outlineColor = SketchbookColors.DarkOutline.toArgb(),
                        accentColor = SketchbookColors.DarkRedAccent.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = SketchbookColors.LightNavyInkPrimary.toArgb()
                    val container = SketchbookColors.LightPrimaryContainer.toArgb()
                    val onContainer = SketchbookColors.LightOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = SketchbookColors.LightPaperSurface.toArgb(),
                        surfaceVariantColor = SketchbookColors.LightPaperSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = SketchbookColors.LightNavyInkPrimary.toArgb(),
                        mutedTextColor = SketchbookColors.LightMutedGraphite.toArgb(),
                        outlineColor = SketchbookColors.LightOutline.toArgb(),
                        accentColor = SketchbookColors.LightMutedRedAccent.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.HERBARIUM -> {
                if (isDark) {
                    val primary = BotanicalColors.DarkSagePrimary.toArgb()
                    val container = BotanicalColors.DarkPrimaryContainer.toArgb()
                    val onContainer = BotanicalColors.DarkOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = BotanicalColors.DarkPaperSurface.toArgb(),
                        surfaceVariantColor = BotanicalColors.DarkPaperSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = BotanicalColors.DarkParchmentText.toArgb(),
                        mutedTextColor = BotanicalColors.DarkMutedText.toArgb(),
                        outlineColor = BotanicalColors.DarkOutline.toArgb(),
                        accentColor = BotanicalColors.DarkMutedRoseTertiary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    val primary = BotanicalColors.LightLeafGreenPrimary.toArgb()
                    val container = BotanicalColors.LightSagePrimaryContainer.toArgb()
                    val onContainer = BotanicalColors.LightOnPrimaryContainer.toArgb()
                    OverlayPalette(
                        surfaceColor = BotanicalColors.LightWarmPaperSurface.toArgb(),
                        surfaceVariantColor = BotanicalColors.LightPaperSurfaceVariant.toArgb(),
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = BotanicalColors.LightBotanicalInkText.toArgb(),
                        mutedTextColor = BotanicalColors.LightMutedInkText.toArgb(),
                        outlineColor = BotanicalColors.LightOutline.toArgb(),
                        accentColor = BotanicalColors.LightMutedTerracottaTertiary.toArgb(),
                        selectedChipContainerColor = container,
                        selectedChipContentColor = onContainer,
                        selectedChipStrokeColor = primary,
                        isDark = false,
                        visualTheme = visualTheme
                    )
                }
            }
            AppVisualTheme.DEFAULT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val primary = context.getColor(
                        if (isDark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600
                    )
                    val surface = context.getColor(
                        if (isDark) android.R.color.system_neutral1_900 else android.R.color.system_neutral1_50
                    )
                    val surfaceVariant = context.getColor(
                        if (isDark) android.R.color.system_neutral2_800 else android.R.color.system_neutral2_100
                    )
                    val container = context.getColor(
                        if (isDark) android.R.color.system_accent1_700 else android.R.color.system_accent1_100
                    )
                    val onContainer = context.getColor(
                        if (isDark) android.R.color.system_accent1_100 else android.R.color.system_accent1_900
                    )
                    OverlayPalette(
                        surfaceColor = surface,
                        surfaceVariantColor = surfaceVariant,
                        primaryColor = primary,
                        primaryContainerColor = container,
                        onPrimaryContainerColor = onContainer,
                        textColor = if (isDark) 0xFFEDEDED.toInt() else 0xFF1A1C20.toInt(),
                        mutedTextColor = if (isDark) 0xFFC4C7D0.toInt() else 0xFF4A4E57.toInt(),
                        outlineColor = if (isDark) 0xFF4A4E57.toInt() else 0xFFC4C7D0.toInt(),
                        accentColor = primary,
                        selectedChipContainerColor = primary,
                        selectedChipContentColor = android.graphics.Color.WHITE,
                        selectedChipStrokeColor = null,
                        isDark = isDark,
                        visualTheme = visualTheme
                    )
                } else {
                    if (isDark) {
                        OverlayPalette(
                            surfaceColor = NeutralSurfaceDark.toArgb(),
                            surfaceVariantColor = 0xFF262930.toInt(),
                            primaryColor = NeutralPrimaryDark.toArgb(),
                            primaryContainerColor = NeutralPrimaryContainerDark.toArgb(),
                            onPrimaryContainerColor = NeutralOnPrimaryContainerDark.toArgb(),
                            textColor = 0xFFEDEDED.toInt(),
                            mutedTextColor = 0xFFC4C7D0.toInt(),
                            outlineColor = 0xFF383E4B.toInt(),
                            accentColor = NeutralPrimaryDark.toArgb(),
                            selectedChipContainerColor = NeutralPrimaryDark.toArgb(),
                            selectedChipContentColor = android.graphics.Color.WHITE,
                            selectedChipStrokeColor = null,
                            isDark = true,
                            visualTheme = visualTheme
                        )
                    } else {
                        OverlayPalette(
                            surfaceColor = NeutralSurfaceLight.toArgb(),
                            surfaceVariantColor = 0xFFF0F2F5.toInt(),
                            primaryColor = NeutralPrimaryLight.toArgb(),
                            primaryContainerColor = NeutralPrimaryContainerLight.toArgb(),
                            onPrimaryContainerColor = NeutralOnPrimaryContainerLight.toArgb(),
                            textColor = 0xFF1A1C20.toInt(),
                            mutedTextColor = 0xFF4A4E57.toInt(),
                            outlineColor = 0xFFE2E6EE.toInt(),
                            accentColor = NeutralPrimaryLight.toArgb(),
                            selectedChipContainerColor = NeutralPrimaryLight.toArgb(),
                            selectedChipContentColor = android.graphics.Color.WHITE,
                            selectedChipStrokeColor = null,
                            isDark = false,
                            visualTheme = visualTheme
                        )
                    }
                }
            }
        }
    }
}
