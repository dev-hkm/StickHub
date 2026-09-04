package com.hkm.stickhub.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {

    @Test
    fun testNavigationStackLifecycle() {
        val navigator = AppNavigator()
        assertEquals(AppRoute.LIBRARY, navigator.currentRoute)
        assertEquals(listOf(AppRoute.LIBRARY), navigator.stack)

        // Push Settings
        val pushedSettings = navigator.requestPush(AppRoute.SETTINGS)
        assertTrue(pushedSettings)
        assertTrue(navigator.isTransitioning)
        assertEquals(AppRoute.SETTINGS, navigator.currentRoute)
        assertEquals(listOf(AppRoute.LIBRARY, AppRoute.SETTINGS), navigator.stack)

        navigator.onTransitionSettled()
        assertFalse(navigator.isTransitioning)

        // Push CategoryManagement
        val pushedCat = navigator.requestPush(AppRoute.CATEGORY_MANAGEMENT)
        assertTrue(pushedCat)
        assertEquals(AppRoute.CATEGORY_MANAGEMENT, navigator.currentRoute)

        navigator.onTransitionSettled()

        // Pop CategoryManagement -> Settings
        val poppedCat = navigator.requestPop()
        assertTrue(poppedCat)
        assertEquals(AppRoute.SETTINGS, navigator.currentRoute)

        navigator.onTransitionSettled()

        // Pop Settings -> Library
        val poppedSettings = navigator.requestPop()
        assertTrue(poppedSettings)
        assertEquals(AppRoute.LIBRARY, navigator.currentRoute)

        navigator.onTransitionSettled()

        // Pop at root returns false and does not corrupt stack
        val poppedRoot = navigator.requestPop()
        assertFalse(poppedRoot)
        assertEquals(AppRoute.LIBRARY, navigator.currentRoute)
        assertEquals(listOf(AppRoute.LIBRARY), navigator.stack)
    }

    @Test
    fun testPushWhileTransitioningIsIgnored() {
        val navigator = AppNavigator()
        navigator.requestPush(AppRoute.SETTINGS)
        assertTrue(navigator.isTransitioning)

        // Second push while transition is still running
        val secondPush = navigator.requestPush(AppRoute.CATEGORY_MANAGEMENT)
        assertFalse("Push while transitioning must be ignored", secondPush)
        assertEquals(AppRoute.SETTINGS, navigator.currentRoute)
    }

    @Test
    fun testPopWhileTransitioningIsIgnored() {
        val navigator = AppNavigator()
        navigator.requestPush(AppRoute.SETTINGS)
        navigator.onTransitionSettled()

        navigator.requestPop()
        assertTrue(navigator.isTransitioning)

        // Pop while already popping
        val secondPop = navigator.requestPop()
        assertFalse("Pop while transitioning must be ignored", secondPop)
    }

    @Test
    fun testInvalidDirectPushIsRejected() {
        val navigator = AppNavigator()
        // Cannot jump directly from Library to CategoryManagement
        val invalidPush = navigator.requestPush(AppRoute.CATEGORY_MANAGEMENT)
        assertFalse(invalidPush)
        assertEquals(AppRoute.LIBRARY, navigator.currentRoute)
    }

    @Test
    fun testRestoreToRebuildsStackAfterProcessDeath() {
        // Fresh controller always starts at [LIBRARY], even when the UI
        // restored a deeper route via rememberSaveable.
        val navigator = AppNavigator()
        navigator.restoreTo(AppRoute.CATEGORY_MANAGEMENT)
        assertEquals(AppRoute.CATEGORY_MANAGEMENT, navigator.currentRoute)
        assertEquals(
            listOf(AppRoute.LIBRARY, AppRoute.SETTINGS, AppRoute.CATEGORY_MANAGEMENT),
            navigator.stack
        )
        // Back works again from the restored depth.
        assertTrue(navigator.requestPop())
        assertEquals(AppRoute.SETTINGS, navigator.currentRoute)

        val navigator2 = AppNavigator()
        navigator2.restoreTo(AppRoute.SETTINGS)
        assertEquals(listOf(AppRoute.LIBRARY, AppRoute.SETTINGS), navigator2.stack)
        assertTrue(navigator2.requestPop())
        assertEquals(AppRoute.LIBRARY, navigator2.currentRoute)

        val navigator3 = AppNavigator()
        navigator3.restoreTo(AppRoute.LIBRARY)
        assertEquals(listOf(AppRoute.LIBRARY), navigator3.stack)
        assertFalse(navigator3.requestPop())
    }
}
