package com.hkm.stickhub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OverlayPreferencesPolicyTest {

    data class OverlayPreferencesState(
        val bubbleSizeDp: Float = 40f,
        val bubbleOpacity: Float = 1.0f,
        val panelOpacity: Float = 1.0f,
        val bubblePosXFraction: Float = 0.92f,
        val bubblePosYFraction: Float = 0.33f,
        val panelWidthPx: Int = 500,
        val panelHeightPx: Int = 700,
        val panelPosX: Int = 100,
        val panelPosY: Int = 200
    )

    private fun clampOpacity(value: Float): Float = value.coerceIn(0f, 1f)

    private fun resetAppearance(current: OverlayPreferencesState): OverlayPreferencesState {
        return current.copy(
            bubbleSizeDp = 40f,
            bubbleOpacity = 1.0f,
            panelOpacity = 1.0f
        )
    }

    @Test
    fun testOpacityClamping() {
        assertEquals(0f, clampOpacity(-0.5f), 0.001f)
        assertEquals(0f, clampOpacity(0f), 0.001f)
        assertEquals(0.42f, clampOpacity(0.42f), 0.001f)
        assertEquals(1.0f, clampOpacity(1.0f), 0.001f)
        assertEquals(1.0f, clampOpacity(1.5f), 0.001f)
    }

    @Test
    fun testBubbleAndPanelOpacityIndependence() {
        var state = OverlayPreferencesState()
        state = state.copy(bubbleOpacity = 0.25f)
        assertEquals(0.25f, state.bubbleOpacity, 0.001f)
        assertEquals(1.0f, state.panelOpacity, 0.001f)

        state = state.copy(panelOpacity = 0.65f)
        assertEquals(0.25f, state.bubbleOpacity, 0.001f)
        assertEquals(0.65f, state.panelOpacity, 0.001f)
    }

    @Test
    fun testResetAppearancePreservesGeometryAndPosition() {
        val customized = OverlayPreferencesState(
            bubbleSizeDp = 64f,
            bubbleOpacity = 0.20f,
            panelOpacity = 0.50f,
            bubblePosXFraction = 0.15f,
            bubblePosYFraction = 0.85f,
            panelWidthPx = 800,
            panelHeightPx = 1000,
            panelPosX = 40,
            panelPosY = 60
        )

        val reset = resetAppearance(customized)

        // Opacity and bubble size reset to defaults
        assertEquals(40f, reset.bubbleSizeDp, 0.001f)
        assertEquals(1.0f, reset.bubbleOpacity, 0.001f)
        assertEquals(1.0f, reset.panelOpacity, 0.001f)

        // Geometry and positions MUST be preserved
        assertEquals(0.15f, reset.bubblePosXFraction, 0.001f)
        assertEquals(0.85f, reset.bubblePosYFraction, 0.001f)
        assertEquals(800, reset.panelWidthPx)
        assertEquals(1000, reset.panelHeightPx)
        assertEquals(40, reset.panelPosX)
        assertEquals(60, reset.panelPosY)
    }
}
