package com.hkm.stickhub.ui.theme

import android.content.Context
import android.content.res.Configuration

/**
 * Local-only single source of truth for the application and overlay theme preference.
 */
object ThemePreferences {
    private const val PREFS_NAME = "stickhub_theme_preferences"
    private const val KEY_THEME_MODE = "app_theme_mode"

    fun getThemeMode(context: Context): AppThemeMode {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, null)
        return parseThemeMode(raw)
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }

    fun parseThemeMode(raw: String?): AppThemeMode {
        return AppThemeMode.fromString(raw)
    }

    /**
     * Resolves whether dark mode should be applied given the context and selected mode.
     */
    fun resolveIsDark(context: Context, mode: AppThemeMode): Boolean {
        val isSystemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        return resolveIsDark(mode, isSystemDark)
    }

    /**
     * Pure resolver mapping theme mode and system dark state to effective dark appearance.
     */
    fun resolveIsDark(mode: AppThemeMode, isSystemDark: Boolean): Boolean {
        return when (mode) {
            AppThemeMode.SYSTEM -> isSystemDark
            AppThemeMode.LIGHT -> false
            AppThemeMode.DARK -> true
        }
    }
}
