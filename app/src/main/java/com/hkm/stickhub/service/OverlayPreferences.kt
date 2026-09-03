package com.hkm.stickhub.service

import android.content.Context

/**
 * Persisted local-only preferences for the floating quick-sticker overlay and popup.
 */
object OverlayPreferences {
    private const val PREFS_NAME = "stickhub_overlay_preferences"

    private const val KEY_BUBBLE_SIZE_DP = "bubble_size_dp"
    private const val KEY_SHOW_TITLE = "show_quick_stickers_title"
    private const val KEY_SHOW_SEARCH = "show_quick_stickers_search"
    private const val KEY_SHOW_CATEGORIES = "show_quick_stickers_categories"

    private const val KEY_BUBBLE_POS_FRACTION_X = "bubble_pos_fraction_x"
    private const val KEY_BUBBLE_POS_FRACTION_Y = "bubble_pos_fraction_y"

    private const val KEY_PANEL_WIDTH = "panel_width_px"
    private const val KEY_PANEL_HEIGHT = "panel_height_px"
    private const val KEY_PANEL_X = "panel_pos_x"
    private const val KEY_PANEL_Y = "panel_pos_y"

    const val DEFAULT_BUBBLE_SIZE_DP = 40f
    const val MIN_BUBBLE_SIZE_DP = 32f
    const val MAX_BUBBLE_SIZE_DP = 72f

    const val DEFAULT_BUBBLE_FRACTION_X = 0.92f
    const val DEFAULT_BUBBLE_FRACTION_Y = 0.33f

    fun bubblePositionFractionX(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_BUBBLE_POS_FRACTION_X, DEFAULT_BUBBLE_FRACTION_X)
        .coerceIn(0f, 1f)

    fun bubblePositionFractionY(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_BUBBLE_POS_FRACTION_Y, DEFAULT_BUBBLE_FRACTION_Y)
        .coerceIn(0f, 1f)

    fun setBubblePositionFraction(context: Context, fractionX: Float, fractionY: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BUBBLE_POS_FRACTION_X, fractionX.coerceIn(0f, 1f))
            .putFloat(KEY_BUBBLE_POS_FRACTION_Y, fractionY.coerceIn(0f, 1f))
            .apply()
    }

    fun bubbleSizeDp(context: Context): Float = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getFloat(KEY_BUBBLE_SIZE_DP, DEFAULT_BUBBLE_SIZE_DP)
        .coerceIn(MIN_BUBBLE_SIZE_DP, MAX_BUBBLE_SIZE_DP)

    fun setBubbleSizeDp(context: Context, sizeDp: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BUBBLE_SIZE_DP, sizeDp.coerceIn(MIN_BUBBLE_SIZE_DP, MAX_BUBBLE_SIZE_DP))
            .apply()
    }

    fun showTitle(context: Context): Boolean = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_SHOW_TITLE, true)

    fun setShowTitle(context: Context, show: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_TITLE, show)
            .apply()
    }

    fun showSearch(context: Context): Boolean = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_SHOW_SEARCH, true)

    fun setShowSearch(context: Context, show: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_SEARCH, show)
            .apply()
    }

    fun showCategories(context: Context): Boolean = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_SHOW_CATEGORIES, true)

    fun setShowCategories(context: Context, show: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_CATEGORIES, show)
            .apply()
    }

    fun panelWidthPx(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_WIDTH, -1)

    fun setPanelWidthPx(context: Context, widthPx: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PANEL_WIDTH, widthPx)
            .apply()
    }

    fun panelHeightPx(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_HEIGHT, -1)

    fun setPanelHeightPx(context: Context, heightPx: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PANEL_HEIGHT, heightPx)
            .apply()
    }

    fun panelPositionX(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_X, -1)

    fun panelPositionY(context: Context): Int = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_PANEL_Y, -1)

    fun setPanelPosition(context: Context, x: Int, y: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_PANEL_X, x)
            .putInt(KEY_PANEL_Y, y)
            .apply()
    }
}
