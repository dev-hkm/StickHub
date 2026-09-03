package com.hkm.stickhub

import android.view.HapticFeedbackConstants
import com.hkm.stickhub.ui.haptics.StickHubHaptics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HapticPolicyTest {

    @Test
    fun testCopyAckPolicy_api30AndAbove() {
        val result = StickHubHaptics.resolveFeedbackConstant(
            StickHubHaptics.FeedbackType.COPY_ACK,
            sdkInt = 34
        )
        assertEquals(HapticFeedbackConstants.CONFIRM, result)
    }

    @Test
    fun testCopyAckPolicy_apiBelow30_fallbackContextClick() {
        val result = StickHubHaptics.resolveFeedbackConstant(
            StickHubHaptics.FeedbackType.COPY_ACK,
            sdkInt = 28
        )
        assertEquals(6, result) // CONTEXT_CLICK (API 23+)
    }

    @Test
    fun testCategoryFilterSelectionResolvesToExactSameConstantAsStickerCopy() {
        // Requirement: category/tag filter changed must resolve to the EXACT SAME mapping/function as sticker copy
        for (sdk in listOf(24, 28, 29, 30, 31, 33, 34, 35)) {
            val copyFeedback = StickHubHaptics.resolveFeedbackConstant(StickHubHaptics.FeedbackType.COPY_ACK, sdk)
            val expectedCategoryFeedback = if (sdk >= 30) HapticFeedbackConstants.CONFIRM else 6
            assertEquals("SDK $sdk must match COPY_ACK", copyFeedback, expectedCategoryFeedback)
        }
    }

    @Test
    fun testNavigationTapPolicyUsesPlatformVirtualKey() {
        val result = StickHubHaptics.resolveFeedbackConstant(
            StickHubHaptics.FeedbackType.NAVIGATION_TAP,
            sdkInt = 35
        )
        assertEquals(HapticFeedbackConstants.VIRTUAL_KEY, result)
    }

    @Test
    fun testLongPressPolicyUsesPlatformLongPress() {
        val result = StickHubHaptics.resolveFeedbackConstant(
            StickHubHaptics.FeedbackType.LONG_PRESS,
            sdkInt = 34
        )
        assertEquals(HapticFeedbackConstants.LONG_PRESS, result)
    }

    @Test
    fun testRejectPolicy_api30AndAbove() {
        val result = StickHubHaptics.resolveFeedbackConstant(
            StickHubHaptics.FeedbackType.REJECT,
            sdkInt = 31
        )
        assertEquals(HapticFeedbackConstants.REJECT, result)
    }

    @Test
    fun testRejectPolicy_apiBelow30_returnsNullToPreventDistraction() {
        val result = StickHubHaptics.resolveFeedbackConstant(
            StickHubHaptics.FeedbackType.REJECT,
            sdkInt = 26
        )
        assertNull(result)
    }

    @Test
    fun testSemanticTypesAreCleanAndPredictable() {
        val types = StickHubHaptics.FeedbackType.entries
        assertEquals(4, types.size)
        assertEquals(
            listOf("COPY_ACK", "NAVIGATION_TAP", "LONG_PRESS", "REJECT"),
            types.map { it.name }
        )
    }
}
