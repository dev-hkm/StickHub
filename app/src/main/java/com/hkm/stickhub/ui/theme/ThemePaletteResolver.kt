package com.hkm.stickhub.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Curated color tokens for the Herbarium (\"Vintage Botanical Scientific Illustration\") theme.
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
            AppVisualTheme.HERBARIUM -> {
                if (isDark) {
                    OverlayPalette(
                        surfaceColor = BotanicalColors.DarkPaperSurface.toArgb(),
                        surfaceVariantColor = BotanicalColors.DarkPaperSurfaceVariant.toArgb(),
                        primaryColor = BotanicalColors.DarkSagePrimary.toArgb(),
                        primaryContainerColor = BotanicalColors.DarkPrimaryContainer.toArgb(),
                        onPrimaryContainerColor = BotanicalColors.DarkOnPrimaryContainer.toArgb(),
                        textColor = BotanicalColors.DarkParchmentText.toArgb(),
                        mutedTextColor = BotanicalColors.DarkMutedText.toArgb(),
                        outlineColor = BotanicalColors.DarkOutline.toArgb(),
                        accentColor = BotanicalColors.DarkMutedRoseTertiary.toArgb(),
                        isDark = true,
                        visualTheme = visualTheme
                    )
                } else {
                    OverlayPalette(
                        surfaceColor = BotanicalColors.LightWarmPaperSurface.toArgb(),
                        surfaceVariantColor = BotanicalColors.LightPaperSurfaceVariant.toArgb(),
                        primaryColor = BotanicalColors.LightLeafGreenPrimary.toArgb(),
                        primaryContainerColor = BotanicalColors.LightSagePrimaryContainer.toArgb(),
                        onPrimaryContainerColor = BotanicalColors.LightOnPrimaryContainer.toArgb(),
                        textColor = BotanicalColors.LightBotanicalInkText.toArgb(),
                        mutedTextColor = BotanicalColors.LightMutedInkText.toArgb(),
                        outlineColor = BotanicalColors.LightOutline.toArgb(),
                        accentColor = BotanicalColors.LightMutedTerracottaTertiary.toArgb(),
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
                            isDark = false,
                            visualTheme = visualTheme
                        )
                    }
                }
            }
        }
    }
}
