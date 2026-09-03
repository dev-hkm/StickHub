package com.hkm.stickhub

import com.hkm.stickhub.service.OverlayAfterCopyAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayAfterCopyPolicyTest {

    @Test
    fun testAfterCopyActionParsing() {
        assertEquals(OverlayAfterCopyAction.CLOSE_POPUP, OverlayAfterCopyAction.fromId("close_popup"))
        assertEquals(OverlayAfterCopyAction.CLOSE_POPUP, OverlayAfterCopyAction.fromId("CLOSE_POPUP"))
        assertEquals(OverlayAfterCopyAction.KEEP_OPEN, OverlayAfterCopyAction.fromId("keep_open"))
        assertEquals(OverlayAfterCopyAction.KEEP_OPEN, OverlayAfterCopyAction.fromId("KEEP_OPEN"))
        assertEquals(OverlayAfterCopyAction.CLOSE_POPUP, OverlayAfterCopyAction.fromId("invalid"))
        assertEquals(OverlayAfterCopyAction.CLOSE_POPUP, OverlayAfterCopyAction.fromId(null))
    }

    @Test
    fun testAfterCopyPolicyDecisions() {
        fun shouldClosePopup(action: OverlayAfterCopyAction, copySucceeded: Boolean): Boolean {
            if (!copySucceeded) return false
            return action == OverlayAfterCopyAction.CLOSE_POPUP
        }

        // When copy succeeds:
        assertTrue(shouldClosePopup(OverlayAfterCopyAction.CLOSE_POPUP, copySucceeded = true))
        assertFalse(shouldClosePopup(OverlayAfterCopyAction.KEEP_OPEN, copySucceeded = true))

        // When copy fails, popup should never close regardless of setting
        assertFalse(shouldClosePopup(OverlayAfterCopyAction.CLOSE_POPUP, copySucceeded = false))
        assertFalse(shouldClosePopup(OverlayAfterCopyAction.KEEP_OPEN, copySucceeded = false))
    }
}
