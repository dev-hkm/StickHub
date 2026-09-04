package com.hkm.stickhub

import android.content.Context
import com.hkm.stickhub.service.OverlayAppearancePreset
import com.hkm.stickhub.service.OverlayOpacityPolicy
import com.hkm.stickhub.service.OverlayPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OverlayAppearancePresetApplyTest {
    private lateinit var context: Context

    @Before fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test fun presetAppliesAllLayersAndLeavesGeometryAlone() {
        OverlayPreferences.setPanelWidthPx(context, 700)
        OverlayPreferences.setPanelPosition(context, 11, 22)
        OverlayPreferences.applyAppearancePreset(context, OverlayAppearancePreset.FLOATING)

        assertEquals(0.75f, OverlayPreferences.bubbleOpacity(context), 0f)
        assertEquals(1f, OverlayPreferences.popupMasterOpacity(context), 0f)
        assertEquals(0f, OverlayPreferences.popupSurfaceOpacity(context), 0f)
        assertEquals(1f, OverlayPreferences.popupStickersOpacity(context), 0f)
        assertEquals(0f, OverlayPreferences.popupChromeOpacity(context), 0f)
        assertEquals(0.65f, OverlayPreferences.popupCloseOpacity(context), 0f)
        assertEquals(0.55f, OverlayPreferences.popupResizeOpacity(context), 0f)
        assertEquals(0.65f, OverlayPreferences.stickerShadowStrength(context), 0f)

        // Geometry untouched.
        assertEquals(700, OverlayPreferences.panelWidthPx(context))
        assertEquals(11, OverlayPreferences.panelPositionX(context))
        assertEquals(22, OverlayPreferences.panelPositionY(context))
    }

    @Test fun everyPresetRoundTripsThroughCommittedReads() {
        for (preset in OverlayAppearancePreset.entries) {
            OverlayPreferences.applyAppearancePreset(context, preset)
            assertEquals(preset.master, OverlayPreferences.popupMasterOpacity(context), 0f)
            assertEquals(preset.surface, OverlayPreferences.popupSurfaceOpacity(context), 0f)
            assertEquals(preset.shadow, OverlayPreferences.stickerShadowStrength(context), 0f)
        }
        // Reset restores stock defaults (no silent auto-apply on upgrade).
        OverlayPreferences.resetAppearance(context)
        assertEquals(
            OverlayOpacityPolicy.DEFAULT_SURFACE_OPACITY,
            OverlayPreferences.popupSurfaceOpacity(context),
            0f
        )
        assertEquals(
            OverlayOpacityPolicy.DEFAULT_MASTER_OPACITY,
            OverlayPreferences.popupMasterOpacity(context),
            0f
        )
    }
}
