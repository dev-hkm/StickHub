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
import com.hkm.stickhub.util.ClipboardHelper
import com.hkm.stickhub.util.ClipboardStager
import com.hkm.stickhub.util.IncomingShareBatch
import com.hkm.stickhub.util.StickerTransport
import com.hkm.stickhub.ui.theme.AppThemeMode
import com.hkm.stickhub.ui.theme.AppVisualTheme
import com.hkm.stickhub.ui.theme.StickHubTheme
import com.hkm.stickhub.ui.theme.ThemePreferences

import android.graphics.Color as AndroidColor
import androidx.activity.SystemBarStyle

class MainActivity : ComponentActivity() {

    private lateinit var repository: StickerRepository
    private var incomingSharedUri by mutableStateOf<Uri?>(null)
    private var incomingSharedBatch by mutableStateOf<IncomingShareBatch?>(null)
    private var incomingShareGeneration = 0L
    private var themeMode by mutableStateOf(AppThemeMode.SYSTEM)
    private var visualTheme by mutableStateOf(AppVisualTheme.DEFAULT)
    /** Bumped on every resume so the UI reconciles real permission/service state. */
    private var foregroundTick by mutableStateOf(0)

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
        ClipboardStager.cleanupStale(applicationContext)
        StickerTransport.cleanup(applicationContext)
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
                    incomingSharedBatch = incomingSharedBatch,
                    onClearSharedBatch = { incomingSharedBatch = null },
                    foregroundTick = foregroundTick,
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

    override fun onResume() {
        super.onResume()
        // Re-read everything another surface could have changed while we were
        // away so the UI reflects reality, not first-launch memory.
        themeMode = ThemePreferences.getThemeMode(this)
        visualTheme = ThemePreferences.getVisualTheme(this)
        foregroundTick++
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val isShare = intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE
        if (!isShare) return

        // Harvest both framework-standard multi-share shapes: EXTRA_STREAM(S)
        // and Intent.clipData. A repeated share with the same URIs still gets a
        // fresh generation so Compose never swallows the event.
        val batch = ClipboardHelper.captureShareBatch(intent, ++incomingShareGeneration)
        if (batch.candidates.isEmpty()) return
        if (intent.action == Intent.ACTION_SEND_MULTIPLE || batch.candidates.size > 1) {
            incomingSharedBatch = IncomingShareBatch(incomingShareGeneration, batch)
        } else {
            // Preserve the existing one-image Share -> cut subject flow.
            incomingSharedUri = batch.uris.firstOrNull()
        }
    }
}
