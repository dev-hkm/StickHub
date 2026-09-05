package com.hkm.stickhub

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.view.WindowManager
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.service.OverlayPreferences
import com.hkm.stickhub.service.OverlayService
import com.hkm.stickhub.service.OverlayStickerFilter
import com.hkm.stickhub.ui.library.StickerLibraryPreferences
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
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

    @Test
    fun tappingNewCategoryChipKeepsBubblePanelOpen() = withService { service ->
        val repository = field<StickerRepository>(service, "repository")
        runBlocking {
            repository.addCategory("Anime")
            repository.addCategory("Food")
            repository.addCategory("Travel")
        }
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val bubble = field<View>(service, "bubbleView")
        assertTrue(bubble.performClick())
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val chipContainer = field<android.widget.LinearLayout>(service, "chipContainer")
        val chip = (0 until chipContainer.childCount)
            .map { chipContainer.getChildAt(it) }
            .firstOrNull { (it as? android.widget.TextView)?.text?.toString() == "Anime" }
        assertNotNull("New category must be rendered as a bubble chip", chip)
        assertTrue(chip!!.performClick())
        assertTrue("Selecting a category must not dismiss the popup", field<Boolean>(service, "isPanelOpen"))
    }

    @Test
    fun categoryChipDoesNotSitUnderCloseControlWhenTitleIsHidden() = withService { service ->
        OverlayPreferences.setShowTitle(service, false)
        OverlayPreferences.setShowSearch(service, false)
        OverlayPreferences.setShowCategories(service, true)
        invoke(service, "refreshOverlayConfiguration")
        val bubble = field<View>(service, "bubbleView")
        assertTrue(bubble.performClick())
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val chips = field<android.widget.LinearLayout>(service, "chipContainer")
        val chip = (0 until chips.childCount)
            .map { chips.getChildAt(it) }
            .first { (it as? android.widget.TextView)?.text?.toString() == "All" }
        val close = field<View>(service, "closeButton")
        val chipLocation = IntArray(2)
        val closeLocation = IntArray(2)
        chip.getLocationOnScreen(chipLocation)
        close.getLocationOnScreen(closeLocation)
        val chipRight = chipLocation[0] + chip.width
        val chipBottom = chipLocation[1] + chip.height
        val closeRight = closeLocation[0] + close.width
        val closeBottom = closeLocation[1] + close.height
        val overlaps = chipLocation[0] < closeRight && closeLocation[0] < chipRight &&
            chipLocation[1] < closeBottom && closeLocation[1] < chipBottom
        assertFalse("Category chips must not be covered by the close hit target", overlaps)
    }

    @Test
    fun addingManyCategoriesWhileBubbleIsOpenKeepsEveryChipActionSafe() = withService { service ->
        val bubble = field<View>(service, "bubbleView")
        assertTrue(bubble.performClick())
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val repository = field<StickerRepository>(service, "repository")
        runBlocking {
            (1..24).forEach { repository.addCategory("Category $it") }
        }
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        val chips = field<android.widget.LinearLayout>(service, "chipContainer")
        val chipViews = (0 until chips.childCount)
            .map { chips.getChildAt(it) }
            .filterIsInstance<android.widget.TextView>()
        assertTrue("All newly added categories should be rendered", chipViews.any { it.text == "Category 24" })
        chipViews.forEach { assertTrue("Chip click must remain handled", it.performClick()) }
        assertTrue("Bubble panel must remain open after category selection", field<Boolean>(service, "isPanelOpen"))
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

    private fun invoke(service: OverlayService, name: String) {
        OverlayService::class.java.getDeclaredMethod(name).apply { isAccessible = true }.invoke(service)
    }
}
