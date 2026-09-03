package com.hkm.stickhub.service

import android.content.Context

/**
 * Policy managing eligibility and local persistence for the first-run Quick Stickers onboarding.
 */
object QuickStickersOnboardingPolicy {
    private const val PREFS_NAME = "stickhub_overlay_onboarding"
    private const val KEY_COMPLETED_OR_DISMISSED = "onboarding_completed_or_dismissed"

    /**
     * Determines whether the user should be shown the opt-in onboarding banner/card.
     */
    fun isOnboardingEligible(
        context: Context,
        isOverlayRunning: Boolean,
        hasOverlayPermission: Boolean,
        hasActiveModalOrFlow: Boolean
    ): Boolean {
        if (hasActiveModalOrFlow) return false
        if (isOverlayRunning) return false
        if (hasOverlayPermission) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_COMPLETED_OR_DISMISSED, false)
    }

    /**
     * Marks the onboarding flow as completed or dismissed (e.g. user tapped 'Not now' or enabled it).
     */
    fun markCompletedOrDismissed(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_COMPLETED_OR_DISMISSED, true)
            .apply()
    }

    fun isCompletedOrDismissed(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_COMPLETED_OR_DISMISSED, false)
    }

    fun resetForTesting(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
