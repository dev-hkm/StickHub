package com.hkm.stickhub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeutralPrimaryDark,
    onPrimary = NeutralOnPrimaryDark,
    primaryContainer = NeutralPrimaryContainerDark,
    onPrimaryContainer = NeutralOnPrimaryContainerDark,
    surface = NeutralSurfaceDark,
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF262930),
    onSurfaceVariant = Color(0xFFC4C7D0),
    background = NeutralBackgroundDark,
    onBackground = Color(0xFFEDEDED)
)

private val LightColorScheme = lightColorScheme(
    primary = NeutralPrimaryLight,
    onPrimary = NeutralOnPrimaryLight,
    primaryContainer = NeutralPrimaryContainerLight,
    onPrimaryContainer = NeutralOnPrimaryContainerLight,
    surface = NeutralSurfaceLight,
    onSurface = Color(0xFF1A1C20),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF4A4E57),
    background = NeutralBackgroundLight,
    onBackground = Color(0xFF1A1C20)
)

/**
 * Animates all color scheme tokens over 200ms when theme or dark mode changes.
 * Avoids duplicate composition layers and preserves layout/scroll stability.
 */
@Composable
fun animateColorScheme(
    target: ColorScheme,
    animationSpec: AnimationSpec<Color> = tween(durationMillis = 200, easing = FastOutSlowInEasing)
): ColorScheme {
    return ColorScheme(
        primary = animateColorAsState(target.primary, animationSpec, label = "theme_primary").value,
        onPrimary = animateColorAsState(target.onPrimary, animationSpec, label = "theme_onPrimary").value,
        primaryContainer = animateColorAsState(target.primaryContainer, animationSpec, label = "theme_primaryContainer").value,
        onPrimaryContainer = animateColorAsState(target.onPrimaryContainer, animationSpec, label = "theme_onPrimaryContainer").value,
        inversePrimary = animateColorAsState(target.inversePrimary, animationSpec, label = "theme_inversePrimary").value,
        secondary = animateColorAsState(target.secondary, animationSpec, label = "theme_secondary").value,
        onSecondary = animateColorAsState(target.onSecondary, animationSpec, label = "theme_onSecondary").value,
        secondaryContainer = animateColorAsState(target.secondaryContainer, animationSpec, label = "theme_secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(target.onSecondaryContainer, animationSpec, label = "theme_onSecondaryContainer").value,
        tertiary = animateColorAsState(target.tertiary, animationSpec, label = "theme_tertiary").value,
        onTertiary = animateColorAsState(target.onTertiary, animationSpec, label = "theme_onTertiary").value,
        tertiaryContainer = animateColorAsState(target.tertiaryContainer, animationSpec, label = "theme_tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(target.onTertiaryContainer, animationSpec, label = "theme_onTertiaryContainer").value,
        background = animateColorAsState(target.background, animationSpec, label = "theme_background").value,
        onBackground = animateColorAsState(target.onBackground, animationSpec, label = "theme_onBackground").value,
        surface = animateColorAsState(target.surface, animationSpec, label = "theme_surface").value,
        onSurface = animateColorAsState(target.onSurface, animationSpec, label = "theme_onSurface").value,
        surfaceVariant = animateColorAsState(target.surfaceVariant, animationSpec, label = "theme_surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(target.onSurfaceVariant, animationSpec, label = "theme_onSurfaceVariant").value,
        surfaceTint = animateColorAsState(target.surfaceTint, animationSpec, label = "theme_surfaceTint").value,
        inverseSurface = animateColorAsState(target.inverseSurface, animationSpec, label = "theme_inverseSurface").value,
        inverseOnSurface = animateColorAsState(target.inverseOnSurface, animationSpec, label = "theme_inverseOnSurface").value,
        error = animateColorAsState(target.error, animationSpec, label = "theme_error").value,
        onError = animateColorAsState(target.onError, animationSpec, label = "theme_onError").value,
        errorContainer = animateColorAsState(target.errorContainer, animationSpec, label = "theme_errorContainer").value,
        onErrorContainer = animateColorAsState(target.onErrorContainer, animationSpec, label = "theme_onErrorContainer").value,
        outline = animateColorAsState(target.outline, animationSpec, label = "theme_outline").value,
        outlineVariant = animateColorAsState(target.outlineVariant, animationSpec, label = "theme_outlineVariant").value,
        scrim = animateColorAsState(target.scrim, animationSpec, label = "theme_scrim").value
    )
}

@Composable
fun StickHubTheme(
    visualTheme: AppVisualTheme = AppVisualTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ (only for DEFAULT theme)
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val rawColorScheme = when (visualTheme) {
        AppVisualTheme.SKETCHBOOK -> {
            if (darkTheme) SketchbookDarkColorScheme else SketchbookLightColorScheme
        }
        AppVisualTheme.HERBARIUM -> {
            if (darkTheme) HerbariumDarkColorScheme else HerbariumLightColorScheme
        }
        AppVisualTheme.DEFAULT -> {
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }
        }
    }

    val colorScheme = animateColorScheme(target = rawColorScheme)

    val typography = when (visualTheme) {
        AppVisualTheme.SKETCHBOOK -> SketchbookTypography
        AppVisualTheme.HERBARIUM -> HerbariumTypography
        AppVisualTheme.DEFAULT -> Typography
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
                @Suppress("DEPRECATION")
                if (Build.VERSION.SDK_INT < 35) {
                    window.isStatusBarContrastEnforced = false
                }
            }
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}