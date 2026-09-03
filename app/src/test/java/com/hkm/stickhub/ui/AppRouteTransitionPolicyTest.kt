package com.hkm.stickhub.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRouteTransitionPolicyTest {

    @Test
    fun closingSettingsUsesTheBackDirection() {
        assertEquals(
            AppRouteTransitionDirection.Back,
            AppRouteTransitionPolicy.direction(
                initial = AppRoute.SETTINGS,
                target = AppRoute.LIBRARY
            )
        )
    }

    @Test
    fun openingSettingsUsesTheForwardDirection() {
        assertEquals(
            AppRouteTransitionDirection.Forward,
            AppRouteTransitionPolicy.direction(
                initial = AppRoute.LIBRARY,
                target = AppRoute.SETTINGS
            )
        )
    }
}
