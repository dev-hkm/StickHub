package com.hkm.stickhub

import com.hkm.stickhub.ui.theme.AppThemeMode
import com.hkm.stickhub.ui.theme.ThemePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeResolverTest {

    @Test
    fun testSystemModeResolvesToSystemDarkState() {
        assertTrue(ThemePreferences.resolveIsDark(AppThemeMode.SYSTEM, isSystemDark = true))
        assertFalse(ThemePreferences.resolveIsDark(AppThemeMode.SYSTEM, isSystemDark = false))
    }

    @Test
    fun testLightModeAlwaysResolvesToFalseRegardlessOfSystem() {
        assertFalse(ThemePreferences.resolveIsDark(AppThemeMode.LIGHT, isSystemDark = true))
        assertFalse(ThemePreferences.resolveIsDark(AppThemeMode.LIGHT, isSystemDark = false))
    }

    @Test
    fun testDarkModeAlwaysResolvesToTrueRegardlessOfSystem() {
        assertTrue(ThemePreferences.resolveIsDark(AppThemeMode.DARK, isSystemDark = true))
        assertTrue(ThemePreferences.resolveIsDark(AppThemeMode.DARK, isSystemDark = false))
    }

    @Test
    fun testInvalidOrUnknownPreferencesDefaultToSystemMode() {
        assertEquals(AppThemeMode.SYSTEM, ThemePreferences.parseThemeMode("UNKNOWN_VALUE"))
        assertEquals(AppThemeMode.SYSTEM, ThemePreferences.parseThemeMode(""))
        assertEquals(AppThemeMode.SYSTEM, ThemePreferences.parseThemeMode("   "))
        assertEquals(AppThemeMode.SYSTEM, ThemePreferences.parseThemeMode(null))
        assertEquals(AppThemeMode.SYSTEM, ThemePreferences.parseThemeMode("system"))
        assertEquals(AppThemeMode.SYSTEM, ThemePreferences.parseThemeMode("SYSTEM"))
        assertEquals(AppThemeMode.LIGHT, ThemePreferences.parseThemeMode("light"))
        assertEquals(AppThemeMode.LIGHT, ThemePreferences.parseThemeMode("LIGHT"))
        assertEquals(AppThemeMode.DARK, ThemePreferences.parseThemeMode("dark"))
        assertEquals(AppThemeMode.DARK, ThemePreferences.parseThemeMode("DARK"))
    }
}
