package com.hkm.stickhub.service

import com.hkm.stickhub.data.model.StickerItem

/**
 * Pure filtering for the floating overlay. The overlay must never impose a display cap:
 * scrolling is the mechanism for reaching a large library, not silently dropping stickers.
 */
object OverlayStickerFilter {
    fun filter(
        stickers: List<StickerItem>,
        selectedCategory: String,
        searchQuery: String
    ): List<StickerItem> {
        val normalizedQuery = searchQuery.trim().lowercase()
        val matched = stickers.filter { sticker ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Favorites" -> sticker.isFavorite
                "Frequent" -> sticker.usageCount > 0
                else -> sticker.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesQuery = normalizedQuery.isEmpty() ||
                sticker.title.lowercase().contains(normalizedQuery) ||
                sticker.tags.lowercase().contains(normalizedQuery) ||
                sticker.category.lowercase().contains(normalizedQuery)
            matchesCategory && matchesQuery
        }
        // Frequent ranks by descending usage; sortedByDescending is stable so
        // ties keep library order instead of shuffling on every open.
        return if (selectedCategory == "Frequent") {
            matched.sortedByDescending { it.usageCount }
        } else {
            matched
        }
    }
}
