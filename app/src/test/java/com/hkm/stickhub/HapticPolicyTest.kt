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
    fun testSelectionTickIsLighterThanCopyAck() {
        // Selections/chips/releases must NOT reuse the heavyweight CONFIRM —
        // they resolve to the lightest tick on every API level.
        for (sdk in listOf(24, 28, 29, 30, 31, 33, 34, 35)) {
            val tick = StickHubHaptics.resolveFeedbackConstant(StickHubHaptics.FeedbackType.TICK, sdk)
            val expected = if (sdk >= 27) HapticFeedbackConstants.TEXT_HANDLE_MOVE
            else HapticFeedbackConstants.VIRTUAL_KEY
            assertEquals("SDK $sdk tick", expected, tick)
        }
        // And CONFIRM stays reserved for real successes.
        assertEquals(
            HapticFeedbackConstants.CONFIRM,
            StickHubHaptics.resolveFeedbackConstant(StickHubHaptics.FeedbackType.COPY_ACK, 34)
        )
    }

    @Test
    fun testNavigationTapPolicy_prefersKeyboardTap() {
        assertEquals(
            HapticFeedbackConstants.KEYBOARD_TAP,
            StickHubHaptics.resolveFeedbackConstant(
                StickHubHaptics.FeedbackType.NAVIGATION_TAP,
                sdkInt = 35
            )
        )
        assertEquals(
            HapticFeedbackConstants.VIRTUAL_KEY,
            StickHubHaptics.resolveFeedbackConstant(
                StickHubHaptics.FeedbackType.NAVIGATION_TAP,
                sdkInt = 28
            )
        )
    }

    @Test
    fun testTogglePolicy_directionAwareOn34_fallsBackToTick() {
        assertEquals(
            HapticFeedbackConstants.TOGGLE_ON,
            StickHubHaptics.resolveFeedbackConstant(
                StickHubHaptics.FeedbackType.TOGGLE_ON,
                sdkInt = 34
            )
        )
        assertEquals(
            HapticFeedbackConstants.TOGGLE_OFF,
            StickHubHaptics.resolveFeedbackConstant(
                StickHubHaptics.FeedbackType.TOGGLE_OFF,
                sdkInt = 35
            )
        )
        // Below API 34 a toggle degrades to the light tick, never CONFIRM.
        assertEquals(
            HapticFeedbackConstants.TEXT_HANDLE_MOVE,
            StickHubHaptics.resolveFeedbackConstant(
                StickHubHaptics.FeedbackType.TOGGLE_ON,
                sdkInt = 30
            )
        )
        assertEquals(
            HapticFeedbackConstants.VIRTUAL_KEY,
            StickHubHaptics.resolveFeedbackConstant(
                StickHubHaptics.FeedbackType.TOGGLE_OFF,
                sdkInt = 24
            )
        )
    }

    @Test
    fun testLongPressPolicyIsASinglePulseNeverTheOemDoubleBeat() {
        assertEquals(
            HapticFeedbackConstants.TEXT_HANDLE_MOVE,
            StickHubHaptics.resolveFeedbackConstant(
                StickHubHaptics.FeedbackType.LONG_PRESS,
                sdkInt = 34
            )
        )
        assertEquals(
            HapticFeedbackConstants.VIRTUAL_KEY,
            StickHubHaptics.resolveFeedbackConstant(
                StickHubHaptics.FeedbackType.LONG_PRESS,
                sdkInt = 24
            )
        )
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
        assertEquals(7, types.size)
        assertEquals(
            listOf("TICK", "NAVIGATION_TAP", "TOGGLE_ON", "TOGGLE_OFF", "LONG_PRESS", "COPY_ACK", "REJECT"),
            types.map { it.name }
        )
    }

    @Test
    fun testThrottleIntervalIsSane() {
        // Machine-gun guard: 50–120ms keeps rapid taps distinct but calm.
        assertEquals(true, StickHubHaptics.MIN_INTERVAL_MS in 50L..120L)
    }
}
