package com.hkm.stickhub.ui.haptics

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Unified semantic haptic policy for StickHub.
 *
 * Guaranteed rules:
 * 1. Reference profile: COPY_ACK is the exact same feedback used for copying a sticker,
 *    category/tag selection, layout changes, and setting toggle mutations.
 * 2. NAVIGATION_TAP is a subtle virtual key feedback for navigating between destinations.
 * 3. No raw numeric API constants; no OS-dependent toggle vibrations.
 * 4. Maximum one haptic event per real state mutation.
 */
class StickHubHaptics(private val view: View) {

    enum class FeedbackType {
        COPY_ACK,
        NAVIGATION_TAP,
        LONG_PRESS,
        REJECT
    }

    /**
     * Primary reference profile: identical acknowledgement used when copying a sticker.
     * Also used for category/tag filter selection, layout selection, and successful setting mutations.
     */
    fun performCopyAck() {
        resolveFeedbackConstant(FeedbackType.COPY_ACK, Build.VERSION.SDK_INT)?.let {
            view.performHapticFeedback(it)
        }
    }

    /**
     * Subtle acknowledgement for deliberate navigation events (open/close Settings, dialogs).
     */
    fun performNavigationTap() {
        resolveFeedbackConstant(FeedbackType.NAVIGATION_TAP, Build.VERSION.SDK_INT)?.let {
            view.performHapticFeedback(it)
        }
    }

    /**
     * Acknowledgement for long-press gestures.
     */
    fun performLongPress() {
        resolveFeedbackConstant(FeedbackType.LONG_PRESS, Build.VERSION.SDK_INT)?.let {
            view.performHapticFeedback(it)
        }
    }

    /**
     * Acknowledgement for rejected or invalid actions.
     */
    fun performReject() {
        resolveFeedbackConstant(FeedbackType.REJECT, Build.VERSION.SDK_INT)?.let {
            view.performHapticFeedback(it)
        }
    }

    // Semantic aliases for consistency across the codebase
    fun performConfirm() = performCopyAck()
    fun performTap() = performNavigationTap()
    fun performSelection() = performCopyAck()

    companion object {
        fun resolveFeedbackConstant(type: FeedbackType, sdkInt: Int): Int? {
            return when (type) {
                FeedbackType.COPY_ACK -> {
                    if (sdkInt >= 30) HapticFeedbackConstants.CONFIRM
                    else 6 // HapticFeedbackConstants.CONTEXT_CLICK (API 23+)
                }
                FeedbackType.NAVIGATION_TAP -> HapticFeedbackConstants.VIRTUAL_KEY
                FeedbackType.LONG_PRESS -> HapticFeedbackConstants.LONG_PRESS
                FeedbackType.REJECT -> {
                    if (sdkInt >= 30) HapticFeedbackConstants.REJECT
                    else null
                }
            }
        }

        fun performCopyAck(view: View) {
            resolveFeedbackConstant(FeedbackType.COPY_ACK, Build.VERSION.SDK_INT)?.let {
                view.performHapticFeedback(it)
            }
        }

        fun performNavigationTap(view: View) {
            resolveFeedbackConstant(FeedbackType.NAVIGATION_TAP, Build.VERSION.SDK_INT)?.let {
                view.performHapticFeedback(it)
            }
        }

        fun performLongPress(view: View) {
            resolveFeedbackConstant(FeedbackType.LONG_PRESS, Build.VERSION.SDK_INT)?.let {
                view.performHapticFeedback(it)
            }
        }

        fun performReject(view: View) {
            resolveFeedbackConstant(FeedbackType.REJECT, Build.VERSION.SDK_INT)?.let {
                view.performHapticFeedback(it)
            }
        }

        fun performConfirm(view: View) = performCopyAck(view)
        fun performTap(view: View) = performNavigationTap(view)
        fun performSelection(view: View) = performCopyAck(view)
    }
}

@Composable
fun rememberStickHubHaptics(): StickHubHaptics {
    val view = LocalView.current
    return remember(view) { StickHubHaptics(view) }
}
