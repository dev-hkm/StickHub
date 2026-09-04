package com.hkm.stickhub

import com.hkm.stickhub.data.cutout.CutoutGesturePolicy
import com.hkm.stickhub.data.cutout.CutoutInteractionMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CutoutGesturePolicyTest {

    @Test
    fun manualModeRequiresLongPressInsteadOfTap() {
        assertFalse(CutoutGesturePolicy.shouldSelectOnTap(CutoutInteractionMode.Manual))
        assertTrue(CutoutGesturePolicy.shouldSelectOnLongPress(CutoutInteractionMode.Manual))
    }

    @Test
    fun autoModeRetainsTapAndLongPressSelection() {
        assertTrue(CutoutGesturePolicy.shouldSelectOnTap(CutoutInteractionMode.Auto))
        assertTrue(CutoutGesturePolicy.shouldSelectOnLongPress(CutoutInteractionMode.Auto))
    }
}
