package com.hkm.stickhub.ui

/**
 * Pure Kotlin navigation controller and state machine for StickHub.
 *
 * Enforces a strict, predictable stack:
 * LIBRARY -> SETTINGS -> CATEGORY_MANAGEMENT
 *
 * Guards against:
 * 1. Concurrent transitions and animation retargeting mid-flight.
 * 2. Invalid routes or corrupted navigation stacks.
 * 3. Double-tap back or navigation gestures.
 */
class AppNavigator(
    initialRoute: AppRoute = AppRoute.LIBRARY,
    var onStateChanged: (() -> Unit)? = null
) {
    private val _stack = mutableListOf(initialRoute)
    val stack: List<AppRoute> get() = _stack.toList()

    val currentRoute: AppRoute
        get() = _stack.lastOrNull() ?: AppRoute.LIBRARY

    var isTransitioning: Boolean = false
        private set

    fun hasRoute(route: AppRoute): Boolean = _stack.contains(route)

    fun canNavigate(): Boolean = !isTransitioning

    fun canPop(): Boolean = !isTransitioning && _stack.size > 1

    /**
     * Attempts to push a new destination onto the navigation stack.
     * Returns true if the push was accepted, false otherwise.
     */
    fun requestPush(destination: AppRoute): Boolean {
        if (isTransitioning) return false

        val current = currentRoute
        val isValid = when (current) {
            AppRoute.LIBRARY -> destination == AppRoute.SETTINGS
            AppRoute.SETTINGS -> destination == AppRoute.CATEGORY_MANAGEMENT
            AppRoute.CATEGORY_MANAGEMENT -> false
        }

        if (isValid) {
            _stack.add(destination)
            isTransitioning = true
            onStateChanged?.invoke()
            return true
        }
        return false
    }

    /**
     * Attempts to pop the topmost destination from the stack.
     * Returns true if a screen was popped, false otherwise.
     */
    fun requestPop(): Boolean {
        if (isTransitioning || _stack.size <= 1) return false

        _stack.removeAt(_stack.lastIndex)
        isTransitioning = true
        onStateChanged?.invoke()
        return true
    }

    /**
     * Called when the enter or exit slide animation completes.
     */
    fun onTransitionSettled() {
        isTransitioning = false
        onStateChanged?.invoke()
    }

    /**
     * Rebuilds the stack after process death: [currentRoute] survives via
     * rememberSaveable but the controller itself does not. Without this,
     * a restored SETTINGS/CATEGORY screen can never pop (stack is [LIBRARY]).
     */
    fun restoreTo(route: AppRoute) {
        _stack.clear()
        _stack.add(AppRoute.LIBRARY)
        if (route == AppRoute.SETTINGS || route == AppRoute.CATEGORY_MANAGEMENT) {
            _stack.add(AppRoute.SETTINGS)
        }
        if (route == AppRoute.CATEGORY_MANAGEMENT) {
            _stack.add(AppRoute.CATEGORY_MANAGEMENT)
        }
        isTransitioning = false
        onStateChanged?.invoke()
    }
}
