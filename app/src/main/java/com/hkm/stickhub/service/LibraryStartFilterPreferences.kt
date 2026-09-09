package com.hkm.stickhub.service

import android.content.Context

/** Persists the filter that the main library should select on a fresh app start. */
object LibraryStartFilterPreferences {
    private const val PREFS_NAME = "stickhub_library_start_filter_preferences"
    private const val KEY_MODE = "mode"
    private const val KEY_CUSTOM_CATEGORY = "custom_category"

    fun startFilterMode(context: Context): OverlayStartFilterMode {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MODE, OverlayStartFilterMode.ALL.id)
        return OverlayStartFilterMode.fromId(raw)
    }

    fun customCategory(context: Context): String = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_CUSTOM_CATEGORY, "")
        .orEmpty()

    fun setStartFilter(context: Context, mode: OverlayStartFilterMode, customCategory: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.id)
            .putString(KEY_CUSTOM_CATEGORY, customCategory.trim())
            .apply()
    }
}
