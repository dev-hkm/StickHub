package com.hkm.stickhub

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.service.OverlayService
import com.hkm.stickhub.ui.StickHubApp
import com.hkm.stickhub.ui.theme.AppThemeMode
import com.hkm.stickhub.ui.theme.AppVisualTheme
import com.hkm.stickhub.ui.theme.StickHubTheme
import com.hkm.stickhub.ui.theme.ThemePreferences

import android.graphics.Color as AndroidColor
import androidx.activity.SystemBarStyle

class MainActivity : ComponentActivity() {

    private lateinit var repository: StickerRepository
    private var incomingSharedUri by mutableStateOf<Uri?>(null)
    private var themeMode by mutableStateOf(AppThemeMode.SYSTEM)
    private var visualTheme by mutableStateOf(AppVisualTheme.DEFAULT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT < 35) {
                window.isStatusBarContrastEnforced = false
            }
        }
        repository = StickerRepository.getInstance(this)
        themeMode = ThemePreferences.getThemeMode(this)
        visualTheme = ThemePreferences.getVisualTheme(this)

        handleIncomingIntent(intent)

        setContent {
            val resolvedIsDark = ThemePreferences.resolveIsDark(this, themeMode)
            StickHubTheme(
                visualTheme = visualTheme,
                darkTheme = resolvedIsDark
            ) {
                StickHubApp(
                    repository = repository,
                    incomingSharedUri = incomingSharedUri,
                    onClearSharedUri = { incomingSharedUri = null },
                    themeMode = themeMode,
                    onThemeModeChange = { newMode ->
                        themeMode = newMode
                        ThemePreferences.setThemeMode(this, newMode)
                        if (OverlayService.isRunning) {
                            startService(
                                Intent(this, OverlayService::class.java)
                                    .setAction(OverlayService.ACTION_REFRESH_CONFIGURATION)
                            )
                        }
                    },
                    visualTheme = visualTheme,
                    onVisualThemeChange = { newTheme ->
                        visualTheme = newTheme
                        ThemePreferences.setVisualTheme(this, newTheme)
                        if (OverlayService.isRunning) {
                            startService(
                                Intent(this, OverlayService::class.java)
                                    .setAction(OverlayService.ACTION_REFRESH_CONFIGURATION)
                            )
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            } ?: intent.data

            if (uri != null) {
                incomingSharedUri = uri
            }
        }
    }
}