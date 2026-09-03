package com.hkm.stickhub

import com.hkm.stickhub.ui.theme.AppThemeMode
import com.hkm.stickhub.ui.theme.AppVisualTheme
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

    @Test
    fun testVisualThemeParsingFallbackToDefault() {
        assertEquals(AppVisualTheme.DEFAULT, ThemePreferences.parseVisualTheme("UNKNOWN"))
        assertEquals(AppVisualTheme.DEFAULT, ThemePreferences.parseVisualTheme(null))
        assertEquals(AppVisualTheme.DEFAULT, ThemePreferences.parseVisualTheme(""))
        assertEquals(AppVisualTheme.DEFAULT, ThemePreferences.parseVisualTheme("   "))
        assertEquals(AppVisualTheme.DEFAULT, AppVisualTheme.fromString("invalid"))
    }

    @Test
    fun testVisualThemeParsingValid() {
        assertEquals(AppVisualTheme.DEFAULT, ThemePreferences.parseVisualTheme("default"))
        assertEquals(AppVisualTheme.DEFAULT, ThemePreferences.parseVisualTheme("DEFAULT"))
        assertEquals(AppVisualTheme.HERBARIUM, ThemePreferences.parseVisualTheme("herbarium"))
        assertEquals(AppVisualTheme.HERBARIUM, ThemePreferences.parseVisualTheme("HERBARIUM"))
        assertEquals(AppVisualTheme.HERBARIUM, AppVisualTheme.fromString("herbarium"))
    }

    @Test
    fun testExistingModeOnlyPreferenceYieldsDefaultVisualTheme() {
        // Simulating legacy preferences where only "app_theme_mode" was stored and "visual_theme" is null
        val legacyStoredVisualTheme: String? = null
        val resolvedVisualTheme = ThemePreferences.parseVisualTheme(legacyStoredVisualTheme)
        assertEquals(AppVisualTheme.DEFAULT, resolvedVisualTheme)
    }

    @Test
    fun testVisualThemeIdAndEnumPersistenceConsistency() {
        for (theme in AppVisualTheme.entries) {
            val serialized = theme.id
            val parsedFromId = ThemePreferences.parseVisualTheme(serialized)
            assertEquals(theme, parsedFromId)

            val parsedFromName = ThemePreferences.parseVisualTheme(theme.name)
            assertEquals(theme, parsedFromName)
        }
    }
}
