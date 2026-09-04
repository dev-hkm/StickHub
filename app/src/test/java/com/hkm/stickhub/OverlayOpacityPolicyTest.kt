package com.hkm.stickhub

import com.hkm.stickhub.service.OverlayOpacityPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OverlayOpacityPolicyTest {

    @Test
    fun corruptedNonFiniteOpacityRecoversToVisibleValue() {
        assertEquals(1f, OverlayOpacityPolicy.clamp(Float.NaN), 0f)
        assertEquals(1f, OverlayOpacityPolicy.clamp(Float.POSITIVE_INFINITY), 0f)
        assertEquals(1f, OverlayOpacityPolicy.clamp(Float.NEGATIVE_INFINITY), 0f)
    }

    data class MockOverlayConfig(
        val bubbleSizeDp: Float = 40f,
        val bubbleOpacity: Float = 1.0f,
        val masterOpacity: Float = 1.0f,
        val surfaceOpacity: Float = 0.96f,
        val stickersOpacity: Float = 1.0f,
        val chromeOpacity: Float = 1.0f,
        val closeOpacity: Float = 1.0f,
        val resizeOpacity: Float = 0.85f,
        val shadowStrength: Float = 0.45f,
        val posXFraction: Float = 0.92f,
        val posYFraction: Float = 0.33f,
        val panelWidthPx: Int = 600,
        val panelHeightPx: Int = 800,
        val defaultFilter: String = "Favorites"
    )

    private fun resetAppearance(current: MockOverlayConfig): MockOverlayConfig {
        return current.copy(
            bubbleOpacity = OverlayOpacityPolicy.DEFAULT_BUBBLE_OPACITY,
            masterOpacity = OverlayOpacityPolicy.DEFAULT_MASTER_OPACITY,
            surfaceOpacity = OverlayOpacityPolicy.DEFAULT_SURFACE_OPACITY,
            stickersOpacity = OverlayOpacityPolicy.DEFAULT_STICKERS_OPACITY,
            chromeOpacity = OverlayOpacityPolicy.DEFAULT_CHROME_OPACITY,
            closeOpacity = OverlayOpacityPolicy.DEFAULT_CLOSE_OPACITY,
            resizeOpacity = OverlayOpacityPolicy.DEFAULT_RESIZE_OPACITY,
            shadowStrength = OverlayOpacityPolicy.DEFAULT_SHADOW_STRENGTH,
            bubbleSizeDp = 40f
        )
    }

    @Test
    fun testClamping() {
        assertEquals(0f, OverlayOpacityPolicy.clamp(-0.2f), 0.001f)
        assertEquals(0f, OverlayOpacityPolicy.clamp(0f), 0.001f)
        assertEquals(0.73f, OverlayOpacityPolicy.clamp(0.73f), 0.001f)
        assertEquals(1.0f, OverlayOpacityPolicy.clamp(1.0f), 0.001f)
        assertEquals(1.0f, OverlayOpacityPolicy.clamp(1.5f), 0.001f)
    }

    @Test
    fun testMasterAndLayerOpacityIndependence() {
        // Master 1, surface 0, stickers 1 -> surface 0, stickers 1
        val effSurface = OverlayOpacityPolicy.effectiveSurfaceOpacity(master = 1.0f, surface = 0.0f)
        val effStickers = OverlayOpacityPolicy.effectiveStickersOpacity(master = 1.0f, stickers = 1.0f)
        val effChrome = OverlayOpacityPolicy.effectiveChromeOpacity(master = 1.0f, chrome = 0.0f)
        val effClose = OverlayOpacityPolicy.effectiveCloseOpacity(master = 1.0f, close = 0.55f)
        val effResize = OverlayOpacityPolicy.effectiveResizeOpacity(master = 1.0f, resize = 0.55f)

        assertEquals(0.0f, effSurface, 0.001f)
        assertEquals(1.0f, effStickers, 0.001f)
        assertEquals(0.0f, effChrome, 0.001f)
        assertEquals(0.55f, effClose, 0.001f)
        assertEquals(0.55f, effResize, 0.001f)
    }

    @Test
    fun testMasterZeroMakesAllLayersInvisible() {
        assertEquals(0f, OverlayOpacityPolicy.effectiveSurfaceOpacity(0.0f, 1.0f), 0.001f)
        assertEquals(0f, OverlayOpacityPolicy.effectiveStickersOpacity(0.0f, 1.0f), 0.001f)
        assertEquals(0f, OverlayOpacityPolicy.effectiveChromeOpacity(0.0f, 1.0f), 0.001f)
        assertEquals(0f, OverlayOpacityPolicy.effectiveCloseOpacity(0.0f, 1.0f), 0.001f)
        assertEquals(0f, OverlayOpacityPolicy.effectiveResizeOpacity(0.0f, 1.0f), 0.001f)
    }

    @Test
    fun testUpdatingSingleOpacityPreservesOthers() {
        var config = MockOverlayConfig()
        config = config.copy(surfaceOpacity = 0.2f)
        assertEquals(0.2f, config.surfaceOpacity, 0.001f)
        assertEquals(1.0f, config.masterOpacity, 0.001f)
        assertEquals(1.0f, config.stickersOpacity, 0.001f)
        assertEquals(1.0f, config.chromeOpacity, 0.001f)

        config = config.copy(stickersOpacity = 0.8f)
        assertEquals(0.2f, config.surfaceOpacity, 0.001f)
        assertEquals(0.8f, config.stickersOpacity, 0.001f)
    }

    @Test
    fun testResetAppearanceDoesNotResetSizePositionOrDefaultFilter() {
        val customized = MockOverlayConfig(
            bubbleSizeDp = 64f,
            bubbleOpacity = 0.1f,
            masterOpacity = 0.7f,
            surfaceOpacity = 0.0f,
            stickersOpacity = 0.5f,
            chromeOpacity = 0.2f,
            closeOpacity = 0.3f,
            resizeOpacity = 0.4f,
            shadowStrength = 0.9f,
            posXFraction = 0.12f,
            posYFraction = 0.78f,
            panelWidthPx = 720,
            panelHeightPx = 960,
            defaultFilter = "Anime"
        )

        val reset = resetAppearance(customized)

        // Visual opacities and bubble size are reset to defaults
        assertEquals(OverlayOpacityPolicy.DEFAULT_BUBBLE_OPACITY, reset.bubbleOpacity, 0.001f)
        assertEquals(OverlayOpacityPolicy.DEFAULT_MASTER_OPACITY, reset.masterOpacity, 0.001f)
        assertEquals(OverlayOpacityPolicy.DEFAULT_SURFACE_OPACITY, reset.surfaceOpacity, 0.001f)
        assertEquals(OverlayOpacityPolicy.DEFAULT_STICKERS_OPACITY, reset.stickersOpacity, 0.001f)
        assertEquals(OverlayOpacityPolicy.DEFAULT_CHROME_OPACITY, reset.chromeOpacity, 0.001f)
        assertEquals(OverlayOpacityPolicy.DEFAULT_CLOSE_OPACITY, reset.closeOpacity, 0.001f)
        assertEquals(OverlayOpacityPolicy.DEFAULT_RESIZE_OPACITY, reset.resizeOpacity, 0.001f)
        assertEquals(OverlayOpacityPolicy.DEFAULT_SHADOW_STRENGTH, reset.shadowStrength, 0.001f)
        assertEquals(40f, reset.bubbleSizeDp, 0.001f)

        // Non-visual settings MUST be preserved!
        assertEquals(0.12f, reset.posXFraction, 0.001f)
        assertEquals(0.78f, reset.posYFraction, 0.001f)
        assertEquals(720, reset.panelWidthPx)
        assertEquals(960, reset.panelHeightPx)
        assertEquals("Anime", reset.defaultFilter)
    }
}
