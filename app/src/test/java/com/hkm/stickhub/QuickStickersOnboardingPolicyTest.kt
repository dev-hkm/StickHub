package com.hkm.stickhub

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickStickersOnboardingPolicyTest {

    // Pure logic simulation of QuickStickersOnboardingPolicy
    private fun isEligible(
        completedOrDismissed: Boolean,
        isOverlayRunning: Boolean,
        hasOverlayPermission: Boolean,
        hasActiveModalOrFlow: Boolean
    ): Boolean {
        if (hasActiveModalOrFlow) return false
        if (isOverlayRunning) return false
        if (hasOverlayPermission) return false
        return !completedOrDismissed
    }

    @Test
    fun testFreshInstallEligible() {
        val eligible = isEligible(
            completedOrDismissed = false,
            isOverlayRunning = false,
            hasOverlayPermission = false,
            hasActiveModalOrFlow = false
        )
        assertTrue(eligible)
    }

    @Test
    fun testUserDismissedNotEligible() {
        val eligible = isEligible(
            completedOrDismissed = true,
            isOverlayRunning = false,
            hasOverlayPermission = false,
            hasActiveModalOrFlow = false
        )
        assertFalse(eligible)
    }

    @Test
    fun testAlreadyRunningNotEligible() {
        val eligible = isEligible(
            completedOrDismissed = false,
            isOverlayRunning = true,
            hasOverlayPermission = true,
            hasActiveModalOrFlow = false
        )
        assertFalse(eligible)
    }

    @Test
    fun testPermissionAlreadyGrantedNotEligible() {
        val eligible = isEligible(
            completedOrDismissed = false,
            isOverlayRunning = false,
            hasOverlayPermission = true,
            hasActiveModalOrFlow = false
        )
        assertFalse(eligible)
    }

    @Test
    fun testActiveModalSuppressesOnboarding() {
        val eligible = isEligible(
            completedOrDismissed = false,
            isOverlayRunning = false,
            hasOverlayPermission = false,
            hasActiveModalOrFlow = true
        )
        assertFalse(eligible)
    }
}
