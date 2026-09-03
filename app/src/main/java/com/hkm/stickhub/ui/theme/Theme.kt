package com.hkm.stickhub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
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

@Composable
fun StickHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
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
            // isAppearanceLightStatusBars = true means dark status bar icons (appropriate for light theme)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}