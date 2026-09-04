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
    ),
    NEUBRUTALISM(
        id = "neubrutalism",
        title = "Neubrutalism",
        subtitle = "Bold borders and hard offset shadows"
    ),
    OLD_MONEY(
        id = "oldmoney",
        title = "Old Money",
        subtitle = "Heritage serif and brass gold"
    ),
    PRESSROOM(
        id = "pressroom",
        title = "Pressroom",
        subtitle = "Warm newsprint and cocoa ink"
    ),
    ATELIER(
        id = "atelier",
        title = "Atelier",
        subtitle = "Refined editorial minimalism"
    ),
    STARBASE(
        id = "starbase",
        title = "Starbase",
        subtitle = "Retro-futurist tech sans"
    ),
    COTTAGE(
        id = "cottage",
        title = "Cottage",
        subtitle = "Faded pastels and vintage romance"
    ),
    AURORA(
        id = "aurora",
        title = "Aurora",
        subtitle = "Northern mesh gradients"
    ),
    SYNTHWAVE(
        id = "synthwave",
        title = "Synthwave",
        subtitle = "Neon grids and chrome suns"
    ),
    GATSBY(
        id = "gatsby",
        title = "Gatsby",
        subtitle = "Black tie and gold leaf"
    ),
    UKIYO(
        id = "ukiyo",
        title = "Ukiyo",
        subtitle = "Waves, ink and vermilion"
    ),
    PIXEL(
        id = "pixel",
        title = "Pixel",
        subtitle = "Phosphor terminal arcade"
    ),
    KAWAII(
        id = "kawaii",
        title = "Kawaii",
        subtitle = "Pastel pop and sparkles"
    ),
    SOLARPUNK(
        id = "solarpunk",
        title = "Solarpunk",
        subtitle = "Sun through leaves"
    ),
    NOIR(
        id = "noir",
        title = "Noir",
        subtitle = "Venetian blinds and streetlamps"
    ),
    GLASS(
        id = "glass",
        title = "Glass",
        subtitle = "Liquid light on color"
    ),
    NOUVEAU(
        id = "nouveau",
        title = "Nouveau",
        subtitle = "Whiplash curves and gold leaf"
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
