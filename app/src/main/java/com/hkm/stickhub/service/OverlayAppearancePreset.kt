package com.hkm.stickhub.service

/** Opt-in presets only change appearance, never size, position, filters or user content. */
enum class OverlayAppearancePreset(
    val title: String,
    val description: String,
    val bubble: Float,
    val master: Float,
    val surface: Float,
    val stickers: Float,
    val chrome: Float,
    val close: Float,
    val resize: Float,
    val shadow: Float
) {
    BALANCED("Balanced", "Solid contrast with discreet controls", 1f, 1f, .96f, 1f, 1f, 1f, .85f, .45f),
    FLOATING("Floating stickers", "No panel background; full-strength stickers", .75f, 1f, 0f, 1f, 0f, .65f, .55f, .65f),
    DISCREET("Discreet", "A softer panel without dimming your stickers", .5f, 1f, .65f, 1f, .85f, .75f, .6f, .35f)
}
