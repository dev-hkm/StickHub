package com.hkm.stickhub

import com.hkm.stickhub.service.OverlayAppearanceState
import org.junit.Assert.*
import org.junit.Test

class OverlayAppearanceStateTest {
    @Test
    fun previewIsTransientAndCommitRestoresPersistedValue() {
        val state = OverlayAppearanceState()
        assertTrue(state.preview("surface", 0.3f))
        assertEquals(0.3f, state.opacity("surface", 0.9f, 0), 0.001f)
        assertEquals(0.7f, state.opacity("stickers", 0.7f, 0), 0.001f)
        state.clearPreviews()
        assertEquals(0.9f, state.opacity("surface", 0.9f, 0), 0.001f)
    }

    @Test
    fun revealWinsOverPreviewAndCommittedTransparencyForFiveSeconds() {
        val state = OverlayAppearanceState()
        state.preview("master", 0f)
        state.reveal(100)
        state.preview("close", 0f)
        assertEquals(1f, state.opacity("master", 0f, 5099), 0f)
        assertEquals(1f, state.opacity("close", 0f, 5099), 0f)
        state.clearPreviews()
        assertEquals(1f, state.opacity("bubble", 0f, 5099), 0f)
        assertEquals(0f, state.opacity("bubble", 0f, 5100), 0f)
    }

    @Test
    fun invalidPreviewCannotPoisonWindowAlpha() {
        val state = OverlayAppearanceState()
        assertFalse(state.preview("unknown", 0.5f))
        assertFalse(state.preview("bubble", Float.NaN))
        assertFalse(state.preview("bubble", Float.POSITIVE_INFINITY))
        assertEquals(0.6f, state.opacity("bubble", 0.6f, 0), 0f)
    }
}
