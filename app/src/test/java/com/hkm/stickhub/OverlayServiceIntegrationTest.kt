package com.hkm.stickhub

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.view.WindowManager
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.service.OverlayPreferences
import com.hkm.stickhub.service.OverlayService
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OverlayServiceIntegrationTest {
    @After
    fun releaseSharedRepository() {
        // The service uses the process-wide repository singleton while each
        // Robolectric test gets a fresh sandbox: drop the shared instance so
        // no test inherits another sandbox's database handle.
        StickerRepository.resetSharedInstanceForTests()
    }

    @Test
    @Config(sdk = [25])
    fun supportedPreOreoDeviceCanStartAndStopOverlay() {
        ShadowSettings.setCanDrawOverlays(true)
        val controller = Robolectric.buildService(OverlayService::class.java).create()
        try {
            assertTrue(OverlayService.isRunning)
            assertNotNull(field<View>(controller.get(), "bubbleView"))
        } finally {
            controller.destroy()
        }
        assertFalse(OverlayService.isRunning)
    }

    @Test
    fun missingPermissionDoesNotRequestStickyRestarts() {
        ShadowSettings.setCanDrawOverlays(false)
        val controller = Robolectric.buildService(OverlayService::class.java).create()
        try {
            assertEquals(Service.START_NOT_STICKY, controller.get().onStartCommand(null, 0, 1))
            assertFalse(OverlayService.isRunning)
        } finally {
            controller.destroy()
        }
    }

    @Test
    fun closedPanelIsClampedAfterViewportShrinks() = withService { service ->
        val params = field<WindowManager.LayoutParams>(service, "panelParams")
        params.width = 1400
        params.height = 2000
        params.x = 1000
        params.y = 1500
        service.onConfigurationChanged(Configuration(service.resources.configuration))
        val metrics = service.resources.displayMetrics
        assertTrue(params.x + params.width <= metrics.widthPixels)
        assertTrue(params.y + params.height <= metrics.heightPixels)
    }

    @Test
    fun previewChangesAlphaWithoutPersistingAndCommitRestoresSavedValue() = withService { service ->
        OverlayPreferences.setBubbleOpacity(service, 0.8f)
        service.onStartCommand(Intent(OverlayService.ACTION_UPDATE_APPEARANCE), 0, 1)
        val bubble = field<View>(service, "bubbleView")
        val preview = Intent("com.hkm.stickhub.PREVIEW_APPEARANCE")
            .putExtra("appearance_layer", "bubble")
            .putExtra("appearance_value", 0.25f)
        service.onStartCommand(preview, 0, 2)
        assertEquals(0.25f, bubble.alpha, 0.001f)
        assertEquals(0.8f, OverlayPreferences.bubbleOpacity(service), 0.001f)
        service.onStartCommand(Intent(OverlayService.ACTION_UPDATE_APPEARANCE), 0, 3)
        assertEquals(0.8f, bubble.alpha, 0.001f)
    }

    @Test
    fun accessibleBubbleClickOpensPanel() = withService { service ->
        val bubble = field<View>(service, "bubbleView")
        assertTrue(bubble.performClick())
        assertTrue(field<Boolean>(service, "isPanelOpen"))
    }

    private fun withService(block: (OverlayService) -> Unit) {
        ShadowSettings.setCanDrawOverlays(true)
        val controller = Robolectric.buildService(OverlayService::class.java).create()
        try {
            block(controller.get())
        } finally {
            controller.destroy()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> field(service: OverlayService, name: String): T =
        OverlayService::class.java.getDeclaredField(name).apply { isAccessible = true }.get(service) as T
}
