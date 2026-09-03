package com.hkm.stickhub.ui

enum class AppRouteTransitionDirection {
    Forward,
    Back
}

/** Keeps route direction explicit so enter and exit motion stay paired. */
object AppRouteTransitionPolicy {
    fun direction(initial: AppRoute, target: AppRoute): AppRouteTransitionDirection {
        val isForward = (initial == AppRoute.LIBRARY && target != AppRoute.LIBRARY) ||
            (initial == AppRoute.SETTINGS && target == AppRoute.CATEGORY_MANAGEMENT)
        return if (isForward) AppRouteTransitionDirection.Forward else AppRouteTransitionDirection.Back
    }
}
