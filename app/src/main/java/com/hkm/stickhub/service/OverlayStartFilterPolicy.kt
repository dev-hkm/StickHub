package com.hkm.stickhub.service

/**
 * Modes defining which category or filter the Quick Stickers popup opens with.
 */
enum class OverlayStartFilterMode(val id: String, val displayName: String) {
    ALL("all", "All stickers"),
    FAVORITES("favorites", "Favorites"),
    FREQUENT("frequent", "Frequently used"),
    LAST_USED("last_used", "Last used filter"),
    CUSTOM_CATEGORY("custom_category", "Custom category");

    companion object {
        fun fromId(id: String?): OverlayStartFilterMode {
            if (id.isNullOrBlank()) return ALL
            return entries.firstOrNull { it.id.equals(id.trim(), ignoreCase = true) } ?: ALL
        }
    }
}

/**
 * Pure Kotlin policy resolving the initial sticker filter when the Quick Stickers popup is opened.
 */
object OverlayStartFilterPolicy {

    const val FILTER_ALL = "All"
    const val FILTER_FAVORITES = "Favorites"
    const val FILTER_FREQUENT = "Frequent"

    /**
     * Resolves the filter name to activate when opening the popup.
     * Guaranteed to return a valid filter or fallback safely to "All".
     */
    fun resolveActiveFilter(
        mode: OverlayStartFilterMode,
        customCategory: String?,
        lastUsedFilter: String?,
        availableCategories: List<String>
    ): String {
        return when (mode) {
            OverlayStartFilterMode.ALL -> FILTER_ALL
            OverlayStartFilterMode.FAVORITES -> FILTER_FAVORITES
            OverlayStartFilterMode.FREQUENT -> FILTER_FREQUENT
            OverlayStartFilterMode.LAST_USED -> {
                if (!lastUsedFilter.isNullOrBlank()) {
                    when (lastUsedFilter) {
                        FILTER_ALL -> FILTER_ALL
                        FILTER_FAVORITES -> FILTER_FAVORITES
                        FILTER_FREQUENT -> FILTER_FREQUENT
                        else -> {
                            val matched = availableCategories.firstOrNull { it.equals(lastUsedFilter, ignoreCase = true) }
                            matched ?: FILTER_ALL
                        }
                    }
                } else {
                    FILTER_ALL
                }
            }
            OverlayStartFilterMode.CUSTOM_CATEGORY -> {
                if (!customCategory.isNullOrBlank()) {
                    val matched = availableCategories.firstOrNull { it.equals(customCategory, ignoreCase = true) }
                    matched ?: FILTER_ALL
                } else {
                    FILTER_ALL
                }
            }
        }
    }

    /**
     * Determines if a newly selected filter should be recorded as the last-used filter.
     */
    fun shouldRecordLastUsedFilter(currentFilter: String, newFilter: String): Boolean {
        return !currentFilter.equals(newFilter, ignoreCase = true)
    }
}
