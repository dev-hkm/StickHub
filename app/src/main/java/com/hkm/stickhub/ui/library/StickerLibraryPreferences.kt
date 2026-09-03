package com.hkm.stickhub.ui.library

import android.content.Context

/**
 * Local-only persistent preferences for the sticker library view mode.
 */
object StickerLibraryPreferences {
    private const val PREFS_NAME = "stickhub_library_preferences"
    private const val KEY_VIEW_MODE = "library_view_mode"
    private const val KEY_SHOW_SEARCH = "library_show_search"
    private const val KEY_SHOW_CATEGORY_FILTERS = "library_show_category_filters"

    fun getViewMode(context: Context): StickerLibraryViewMode {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_VIEW_MODE, null)
        return StickerLibraryViewMode.fromString(raw)
    }

    fun setViewMode(context: Context, mode: StickerLibraryViewMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_VIEW_MODE, mode.name)
            .apply()
    }

    fun isSearchVisible(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOW_SEARCH, true)
    }

    fun setSearchVisible(context: Context, visible: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_SEARCH, visible)
            .apply()
    }

    fun isCategoryFiltersVisible(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOW_CATEGORY_FILTERS, true)
    }

    fun setCategoryFiltersVisible(context: Context, visible: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_CATEGORY_FILTERS, visible)
            .apply()
    }
}
