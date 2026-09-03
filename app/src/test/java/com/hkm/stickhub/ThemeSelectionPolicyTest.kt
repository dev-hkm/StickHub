package com.hkm.stickhub

import com.hkm.stickhub.ui.theme.AppThemeMode
import com.hkm.stickhub.ui.theme.AppVisualTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeSelectionPolicyTest {

    data class ThemeSelectionState(
        val visualTheme: AppVisualTheme = AppVisualTheme.DEFAULT,
        val themeMode: AppThemeMode = AppThemeMode.SYSTEM
    )

    class ThemeSelectionController(initialState: ThemeSelectionState = ThemeSelectionState()) {
        var state: ThemeSelectionState = initialState
            private set

        var stateChangeCount = 0
            private set

        var hapticTriggerCount = 0
            private set

        fun selectVisualTheme(newTheme: AppVisualTheme): Boolean {
            if (newTheme == state.visualTheme) {
                // Clicking the already selected theme does NOT trigger haptic or state update
                return false
            }
            hapticTriggerCount++
            state = state.copy(visualTheme = newTheme)
            stateChangeCount++
            return true
        }

        fun selectThemeMode(newMode: AppThemeMode): Boolean {
            if (newMode == state.themeMode) {
                return false
            }
            hapticTriggerCount++
            state = state.copy(themeMode = newMode)
            stateChangeCount++
            return true
        }
    }

    @Test
    fun testSelectingSameVisualThemeIsNoOp() {
        val controller = ThemeSelectionController(ThemeSelectionState(visualTheme = AppVisualTheme.DEFAULT))

        val changed = controller.selectVisualTheme(AppVisualTheme.DEFAULT)
        assertFalse(changed)
        assertEquals(0, controller.stateChangeCount)
        assertEquals(0, controller.hapticTriggerCount)
        assertEquals(AppVisualTheme.DEFAULT, controller.state.visualTheme)
    }

    @Test
    fun testSelectingNewVisualThemeTriggersSingleStateChangeAndHaptic() {
        val controller = ThemeSelectionController(ThemeSelectionState(visualTheme = AppVisualTheme.DEFAULT))

        val changed = controller.selectVisualTheme(AppVisualTheme.HERBARIUM)
        assertTrue(changed)
        assertEquals(1, controller.stateChangeCount)
        assertEquals(1, controller.hapticTriggerCount)
        assertEquals(AppVisualTheme.HERBARIUM, controller.state.visualTheme)

        // Selecting Herbarium again does nothing
        val changedAgain = controller.selectVisualTheme(AppVisualTheme.HERBARIUM)
        assertFalse(changedAgain)
        assertEquals(1, controller.stateChangeCount)
        assertEquals(1, controller.hapticTriggerCount)
    }

    @Test
    fun testSelectingSameThemeModeIsNoOp() {
        val controller = ThemeSelectionController(ThemeSelectionState(themeMode = AppThemeMode.SYSTEM))

        val changed = controller.selectThemeMode(AppThemeMode.SYSTEM)
        assertFalse(changed)
        assertEquals(0, controller.stateChangeCount)
        assertEquals(0, controller.hapticTriggerCount)
    }

    @Test
    fun testSelectingNewThemeModeTriggersSingleStateChangeAndHaptic() {
        val controller = ThemeSelectionController(ThemeSelectionState(themeMode = AppThemeMode.SYSTEM))

        val changed = controller.selectThemeMode(AppThemeMode.DARK)
        assertTrue(changed)
        assertEquals(1, controller.stateChangeCount)
        assertEquals(1, controller.hapticTriggerCount)
        assertEquals(AppThemeMode.DARK, controller.state.themeMode)
    }
}
