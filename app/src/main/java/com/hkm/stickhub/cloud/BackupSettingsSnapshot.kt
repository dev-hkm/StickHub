package com.hkm.stickhub.cloud

import android.content.Context
import com.hkm.stickhub.service.OverlayAfterCopyAction
import com.hkm.stickhub.service.OverlayPreferences
import com.hkm.stickhub.service.OverlayStartFilterMode
import com.hkm.stickhub.ui.library.StickerLibraryPreferences
import com.hkm.stickhub.ui.library.StickerLibraryViewMode
import com.hkm.stickhub.ui.theme.AppThemeMode
import com.hkm.stickhub.ui.theme.ThemePreferences
import org.json.JSONArray
import org.json.JSONObject

/** Explicit allow-list of user preferences that travel with a backup archive. */
object BackupSettingsSnapshot {
    private const val OVERLAY = "overlay"
    private const val THEME = "theme"
    private const val LIBRARY = "library"

    fun capture(context: Context): JSONObject = JSONObject().apply {
        put(THEME, JSONObject().apply {
            put("mode", ThemePreferences.getThemeMode(context).name)
            put("visual", ThemePreferences.getVisualTheme(context).id)
        })
        put(LIBRARY, JSONObject().apply {
            put("viewMode", StickerLibraryPreferences.getViewMode(context).name)
            put("showSearch", StickerLibraryPreferences.isSearchVisible(context))
            put("showCategoryFilters", StickerLibraryPreferences.isCategoryFiltersVisible(context))
            put("categoryOrder", JSONArray().apply {
                StickerLibraryPreferences.getCategoryTabOrder(context).orEmpty().forEach(::put)
            })
        })
        put(OVERLAY, JSONObject().apply {
            put("bubbleSizeDp", OverlayPreferences.bubbleSizeDp(context).toDouble())
            put("bubbleOpacity", OverlayPreferences.bubbleOpacity(context).toDouble())
            put("popupMasterOpacity", OverlayPreferences.popupMasterOpacity(context).toDouble())
            put("popupSurfaceOpacity", OverlayPreferences.popupSurfaceOpacity(context).toDouble())
            put("popupStickersOpacity", OverlayPreferences.popupStickersOpacity(context).toDouble())
            put("popupChromeOpacity", OverlayPreferences.popupChromeOpacity(context).toDouble())
            put("popupCloseOpacity", OverlayPreferences.popupCloseOpacity(context).toDouble())
            put("popupResizeOpacity", OverlayPreferences.popupResizeOpacity(context).toDouble())
            put("stickerShadowStrength", OverlayPreferences.stickerShadowStrength(context).toDouble())
            put("bubblePositionX", OverlayPreferences.bubblePositionFractionX(context).toDouble())
            put("bubblePositionY", OverlayPreferences.bubblePositionFractionY(context).toDouble())
            put("panelWidthPx", OverlayPreferences.panelWidthPx(context))
            put("panelHeightPx", OverlayPreferences.panelHeightPx(context))
            put("panelX", OverlayPreferences.panelPositionX(context))
            put("panelY", OverlayPreferences.panelPositionY(context))
            put("showTitle", OverlayPreferences.showTitle(context))
            put("showSearch", OverlayPreferences.showSearch(context))
            put("showCategories", OverlayPreferences.showCategories(context))
            put("startFilterMode", OverlayPreferences.startFilterMode(context).id)
            put("startCustomCategory", OverlayPreferences.startCustomCategory(context))
            put("lastUsedFilter", OverlayPreferences.lastUsedFilter(context) ?: JSONObject.NULL)
            put("afterCopyAction", OverlayPreferences.afterCopyAction(context).id)
        })
    }

    fun restore(context: Context, snapshot: JSONObject?) {
        if (snapshot == null) return
        snapshot.optJSONObject(THEME)?.let { theme ->
            ThemePreferences.setThemeMode(context, AppThemeMode.fromString(theme.optString("mode", "")))
            ThemePreferences.setVisualTheme(context, ThemePreferences.parseVisualTheme(theme.optString("visual", "")))
        }
        snapshot.optJSONObject(LIBRARY)?.let { library ->
            StickerLibraryPreferences.setViewMode(
                context,
                StickerLibraryViewMode.fromString(library.optString("viewMode", ""))
            )
            if (library.has("showSearch")) {
                StickerLibraryPreferences.setSearchVisible(context, library.optBoolean("showSearch"))
            }
            if (library.has("showCategoryFilters")) {
                StickerLibraryPreferences.setCategoryFiltersVisible(context, library.optBoolean("showCategoryFilters"))
            }
            val order = library.optJSONArray("categoryOrder")?.let { array ->
                buildList {
                    for (index in 0 until array.length()) {
                        array.optString(index).trim().takeIf(String::isNotBlank)?.let(::add)
                    }
                }
            }.orEmpty()
            if (order.isNotEmpty()) StickerLibraryPreferences.setCategoryTabOrder(context, order)
        }
        snapshot.optJSONObject(OVERLAY)?.let { overlay ->
            OverlayPreferences.setBubbleSizeDp(
                context,
                overlay.floatValue("bubbleSizeDp", OverlayPreferences.bubbleSizeDp(context), 32f, 72f)
            )
            OverlayPreferences.setBubbleOpacity(context, overlay.floatValue("bubbleOpacity", OverlayPreferences.bubbleOpacity(context)))
            OverlayPreferences.setPopupMasterOpacity(context, overlay.floatValue("popupMasterOpacity", OverlayPreferences.popupMasterOpacity(context)))
            OverlayPreferences.setPopupSurfaceOpacity(context, overlay.floatValue("popupSurfaceOpacity", OverlayPreferences.popupSurfaceOpacity(context)))
            OverlayPreferences.setPopupStickersOpacity(context, overlay.floatValue("popupStickersOpacity", OverlayPreferences.popupStickersOpacity(context)))
            OverlayPreferences.setPopupChromeOpacity(context, overlay.floatValue("popupChromeOpacity", OverlayPreferences.popupChromeOpacity(context)))
            OverlayPreferences.setPopupCloseOpacity(context, overlay.floatValue("popupCloseOpacity", OverlayPreferences.popupCloseOpacity(context)))
            OverlayPreferences.setPopupResizeOpacity(context, overlay.floatValue("popupResizeOpacity", OverlayPreferences.popupResizeOpacity(context)))
            OverlayPreferences.setStickerShadowStrength(
                context,
                overlay.floatValue("stickerShadowStrength", OverlayPreferences.stickerShadowStrength(context), 0f, 1f)
            )
            OverlayPreferences.setBubblePositionFraction(
                context,
                overlay.floatValue("bubblePositionX", OverlayPreferences.bubblePositionFractionX(context), 0f, 1f),
                overlay.floatValue("bubblePositionY", OverlayPreferences.bubblePositionFractionY(context), 0f, 1f)
            )
            if (overlay.has("panelWidthPx") && overlay.optInt("panelWidthPx", -1) >= -1) {
                OverlayPreferences.setPanelWidthPx(context, overlay.optInt("panelWidthPx", -1))
            }
            if (overlay.has("panelHeightPx") && overlay.optInt("panelHeightPx", -1) >= -1) {
                OverlayPreferences.setPanelHeightPx(context, overlay.optInt("panelHeightPx", -1))
            }
            if (overlay.has("panelX") && overlay.optInt("panelX", -1) >= -1 &&
                overlay.has("panelY") && overlay.optInt("panelY", -1) >= -1
            ) {
                OverlayPreferences.setPanelPosition(context, overlay.optInt("panelX"), overlay.optInt("panelY"))
            }
            if (overlay.has("showTitle")) OverlayPreferences.setShowTitle(context, overlay.optBoolean("showTitle"))
            if (overlay.has("showSearch")) OverlayPreferences.setShowSearch(context, overlay.optBoolean("showSearch"))
            if (overlay.has("showCategories")) OverlayPreferences.setShowCategories(context, overlay.optBoolean("showCategories"))
            OverlayPreferences.setStartFilterMode(
                context,
                OverlayStartFilterMode.fromId(overlay.optString("startFilterMode", ""))
            )
            OverlayPreferences.setStartCustomCategory(context, overlay.optString("startCustomCategory", ""))
            OverlayPreferences.setAfterCopyAction(
                context,
                OverlayAfterCopyAction.fromId(overlay.optString("afterCopyAction", ""))
            )
            if (overlay.has("lastUsedFilter") && !overlay.isNull("lastUsedFilter")) {
                OverlayPreferences.setLastUsedFilter(context, overlay.optString("lastUsedFilter"))
            }
        }
    }

    private fun JSONObject.floatValue(key: String, fallback: Float, min: Float = 0f, max: Float = 1f): Float {
        val candidate = optDouble(key, Double.NaN)
        return if (candidate.isFinite()) candidate.toFloat().coerceIn(min, max) else fallback
    }
}
