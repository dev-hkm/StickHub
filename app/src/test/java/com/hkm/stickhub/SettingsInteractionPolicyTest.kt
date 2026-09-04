package com.hkm.stickhub

import com.hkm.stickhub.service.OverlayAppearancePreset
import com.hkm.stickhub.ui.settings.PreviewRateLimiter
import com.hkm.stickhub.ui.settings.SliderInteractionState
import org.junit.Assert.*
import org.junit.Test

class SettingsInteractionPolicyTest {
    @Test fun finalSliderValueWinsOverAStaleParentEcho() {
        val state = SliderInteractionState(1f, 0f..1f)
        state.change(.2f)
        state.synchronize(.8f)
        assertEquals(.2f, state.finish(), 0f)
        state.synchronize(.6f)
        assertEquals(.6f, state.value, 0f)
    }

    @Test fun cancelledSliderRestoresCommittedValue() {
        val state = SliderInteractionState(.8f, 0f..1f)
        state.change(.1f)
        assertEquals(.8f, state.cancel(), 0f)
        assertEquals(.8f, state.value, 0f)
    }

    @Test fun directManipulationClampsInvalidValues() {
        val state = SliderInteractionState(.5f, 0f..1f)
        state.change(Float.NaN)
        assertEquals(.5f, state.finish(), 0f)
        state.change(4f)
        assertEquals(1f, state.finish(), 0f)
    }

    @Test fun previewLimitsBinderTrafficButNeverDropsANewLayer() {
        val gate = PreviewRateLimiter(32L)
        assertTrue(gate.shouldDispatch("surface", 0L))
        assertFalse(gate.shouldDispatch("surface", 16L))
        assertTrue(gate.shouldDispatch("surface", 32L))
        assertTrue(gate.shouldDispatch("stickers", 33L))
        gate.reset()
        assertTrue(gate.shouldDispatch("surface", 34L))
    }

    @Test fun floatingPresetKeepsStickersAndRecoveryControlsVisible() {
        val preset = OverlayAppearancePreset.FLOATING
        assertEquals(0f, preset.surface, 0f)
        assertEquals(1f, preset.master * preset.stickers, 0f)
        assertTrue(preset.close > 0f && preset.resize > 0f)
        assertTrue(preset.shadow > 0f)
    }

    @Test fun allPresetsAreFiniteAndInRange() {
        OverlayAppearancePreset.entries.forEach {
            listOf(it.bubble, it.master, it.surface, it.stickers, it.chrome, it.close, it.resize, it.shadow)
                .forEach { value -> assertTrue(value.isFinite() && value in 0f..1f) }
        }
    }
}
