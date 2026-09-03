package com.hkm.stickhub.ui.theme

/**
 * Supported app-wide visual themes.
 * Determines the color palette, typography hierarchy, and decorative design language.
 */
enum class AppVisualTheme(
    val id: String,
    val title: String,
    val subtitle: String
) {
    DEFAULT(
        id = "default",
        title = "Default",
        subtitle = "System palette"
    ),
    HERBARIUM(
        id = "herbarium",
        title = "Herbarium",
        subtitle = "Parchment, botanical ink, and scientific detail"
    ),
    SKETCHBOOK(
        id = "sketchbook",
        title = "Sketchbook",
        subtitle = "Notebook paper and hand-drawn ink"
    );

    companion object {
        fun fromString(value: String?): AppVisualTheme {
            if (value.isNullOrBlank()) return DEFAULT
            val trimmed = value.trim()
            return entries.firstOrNull {
                it.name.equals(trimmed, ignoreCase = true) || it.id.equals(trimmed, ignoreCase = true)
            } ?: DEFAULT
        }
    }
}
