package com.hkm.stickhub.ui.library

import com.composables.icons.lucide.R as LucideR

/**
 * Display modes for the main sticker collection.
 */
enum class StickerLibraryViewMode(
    val title: String,
    val subtitle: String,
    val iconRes: Int
) {
    COMPACT_GRID(
        title = "Compact Grid",
        subtitle = "4 columns, high density previews",
        iconRes = LucideR.drawable.lucide_ic_grid_3x3
    ),
    STANDARD_GRID(
        title = "Standard Grid",
        subtitle = "3 columns, balanced card size",
        iconRes = LucideR.drawable.lucide_ic_layout_grid
    ),
    LARGE_GRID(
        title = "Large Grid",
        subtitle = "2 columns, prominent cards with titles",
        iconRes = LucideR.drawable.lucide_ic_grid_2x2
    ),
    LIST(
        title = "List",
        subtitle = "Full-width rows with details and tags",
        iconRes = LucideR.drawable.lucide_ic_layout_list
    );

    companion object {
        fun fromString(value: String?): StickerLibraryViewMode {
            if (value.isNullOrBlank()) return STANDARD_GRID
            return entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: STANDARD_GRID
        }
    }
}
