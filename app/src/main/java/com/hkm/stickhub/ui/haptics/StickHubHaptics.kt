package com.hkm.stickhub.ui.haptics

import android.os.Build
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Unified semantic haptic policy for StickHub.
 *
 * Design goal: feel like a stock-quality app. Strength ladder (weak → strong):
 * TICK (selections, chips, releases) < NAVIGATION_TAP (moves between screens)
 * < TOGGLE (switch flips, direction-aware on API 34+) < HOLD_ACK (a single
 * crisp beat for holds — deliberately not the platform LONG_PRESS constant,
 * which several OEMs render as a double pulse)
 * < COPY_ACK/CONFIRM (real successes only: copy, save, export, import)
 * < REJECT (errors).
 *
 * Guaranteed rules:
 * 1. COPY_ACK/CONFIRM fires only after a confirmed success — never on a blind
 *    button press, never for toggles, chips or slider releases.
 * 2. At most one haptic event per 80ms per source (machine-gun guard).
 * 3. No raw Vibrator API; only OEM-tuned platform constants.
 */
class StickHubHaptics(private val view: View) {

    enum class FeedbackType {
        TICK,
        NAVIGATION_TAP,
        TOGGLE_ON,
        TOGGLE_OFF,
        LONG_PRESS,
        COPY_ACK,
        REJECT
    }

    private var lastFireUptimeMs: Long = 0L

    private fun fire(type: FeedbackType): Boolean {
        val now = SystemClock.uptimeMillis()
        if (now - lastFireUptimeMs < MIN_INTERVAL_MS) return false
        val constant = resolveFeedbackConstant(type, Build.VERSION.SDK_INT) ?: return false
        lastFireUptimeMs = now
        return view.performHapticFeedback(constant)
    }

    /** Lightest tick: selections, chips, slider releases, dialog buttons. */
    fun performTick() {
        fire(FeedbackType.TICK)
    }

    /** Deliberate navigation events (open/close screens, sheets, dialogs). */
    fun performNavigationTap() {
        fire(FeedbackType.NAVIGATION_TAP)
    }

    /** Direction-aware switch flip (falls back to a tick below API 34). */
    fun performToggle(enabled: Boolean) {
        fire(if (enabled) FeedbackType.TOGGLE_ON else FeedbackType.TOGGLE_OFF)
    }

    /** Acknowledgement for long-press gestures. */
    fun performLongPress() {
        fire(FeedbackType.LONG_PRESS)
    }

    /**
     * Primary reference profile: fires ONLY after a confirmed success
     * (sticker copied, sticker saved, backup exported/imported).
     */
    fun performCopyAck() {
        fire(FeedbackType.COPY_ACK)
    }

    /** Acknowledgement for rejected or invalid actions. */
    fun performReject() {
        fire(FeedbackType.REJECT)
    }

    // Semantic aliases for consistency across the codebase
    fun performConfirm() = performCopyAck()
    fun performTap() = performNavigationTap()
    fun performSelection() = performTick()

    companion object {
        const val MIN_INTERVAL_MS: Long = 80L

        @Volatile
        private var lastStaticFireUptimeMs: Long = 0L

        fun resolveFeedbackConstant(type: FeedbackType, sdkInt: Int): Int? {
            return when (type) {
                FeedbackType.TICK -> {
                    if (sdkInt >= 27) HapticFeedbackConstants.TEXT_HANDLE_MOVE
                    else HapticFeedbackConstants.VIRTUAL_KEY
                }
                FeedbackType.NAVIGATION_TAP -> {
                    if (sdkInt >= 30) HapticFeedbackConstants.KEYBOARD_TAP
                    else HapticFeedbackConstants.VIRTUAL_KEY
                }
                FeedbackType.TOGGLE_ON -> {
                    if (sdkInt >= 34) HapticFeedbackConstants.TOGGLE_ON
                    else resolveFeedbackConstant(FeedbackType.TICK, sdkInt)
                }
                FeedbackType.TOGGLE_OFF -> {
                    if (sdkInt >= 34) HapticFeedbackConstants.TOGGLE_OFF
                    else resolveFeedbackConstant(FeedbackType.TICK, sdkInt)
                }
                FeedbackType.COPY_ACK -> {
                    if (sdkInt >= 30) HapticFeedbackConstants.CONFIRM
                    else 6 // HapticFeedbackConstants.CONTEXT_CLICK (API 23+)
                }
                FeedbackType.LONG_PRESS -> {
                    // Exactly one beat, always: HapticFeedbackConstants.LONG_PRESS
                    // is a double pulse on several OEM skins and users read it
                    // as two separate events.
                    if (sdkInt >= 27) HapticFeedbackConstants.TEXT_HANDLE_MOVE
                    else HapticFeedbackConstants.VIRTUAL_KEY
                }
                FeedbackType.REJECT -> {
                    if (sdkInt >= 30) HapticFeedbackConstants.REJECT
                    else null
                }
            }
        }

        private fun fireStatic(view: View, type: FeedbackType): Boolean {
            val now = SystemClock.uptimeMillis()
            if (now - lastStaticFireUptimeMs < MIN_INTERVAL_MS) return false
            val constant = resolveFeedbackConstant(type, Build.VERSION.SDK_INT) ?: return false
            lastStaticFireUptimeMs = now
            return view.performHapticFeedback(constant)
        }

        fun performTick(view: View) {
            fireStatic(view, FeedbackType.TICK)
        }

        fun performNavigationTap(view: View) {
            fireStatic(view, FeedbackType.NAVIGATION_TAP)
        }

        fun performToggle(view: View, enabled: Boolean) {
            fireStatic(view, if (enabled) FeedbackType.TOGGLE_ON else FeedbackType.TOGGLE_OFF)
        }

        fun performLongPress(view: View) {
            fireStatic(view, FeedbackType.LONG_PRESS)
        }

        fun performCopyAck(view: View) {
            fireStatic(view, FeedbackType.COPY_ACK)
        }

        fun performReject(view: View) {
            fireStatic(view, FeedbackType.REJECT)
        }

        fun performConfirm(view: View) = performCopyAck(view)
        fun performTap(view: View) = performNavigationTap(view)
        fun performSelection(view: View) = performTick(view)
    }
}

@Composable
fun rememberStickHubHaptics(): StickHubHaptics {
    val view = LocalView.current
    return remember(view) { StickHubHaptics(view) }
}
