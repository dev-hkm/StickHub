package com.hkm.stickhub.service

/**
 * Pure policy for computing silhouette shadow properties, colors, and cache keys.
 * Implemented with pure bitwise operations for high performance and pure JVM testability.
 */
object StickerShadowPolicy {
    const val MIN_STRENGTH = 0.0f
    const val MAX_STRENGTH = 1.0f
    const val DEFAULT_STRENGTH = 0.45f

    fun isShadowEnabled(strength: Float): Boolean = strength > 0.01f

    fun resolveBlurRadius(strength: Float, density: Float): Float {
        // Subtle: 2dp..10dp blur
        val baseDp = 2f + (strength.coerceIn(0f, 1f) * 8f)
        return (baseDp * density).coerceIn(1f, 30f)
    }

    fun resolveShadowAlpha(strength: Float): Int {
        // Gentle alpha: 0..160 (never harsh pure opaque black)
        return (strength.coerceIn(0f, 1f) * 160f).toInt().coerceIn(0, 255)
    }

    fun resolveShadowColor(strength: Float, isDark: Boolean): Int {
        val baseR = if (isDark) 10 else 30
        val baseG = if (isDark) 10 else 32
        val baseB = if (isDark) 15 else 38
        val alpha = resolveShadowAlpha(strength)
        return argb(alpha, baseR, baseG, baseB)
    }

    fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
        return ((alpha.coerceIn(0, 255) and 0xFF) shl 24) or
               ((red.coerceIn(0, 255) and 0xFF) shl 16) or
               ((green.coerceIn(0, 255) and 0xFF) shl 8) or
               (blue.coerceIn(0, 255) and 0xFF)
    }

    fun applyAlphaToColor(color: Int, alpha: Int): Int {
        return ((alpha.coerceIn(0, 255) and 0xFF) shl 24) or (color and 0x00FFFFFF)
    }

    fun buildCacheKey(
        filePath: String,
        lastModified: Long,
        targetSize: Int,
        shadowStrength: Float,
        isDark: Boolean
    ): String {
        val strengthBucket = (shadowStrength.coerceIn(0f, 1f) * 20).toInt()
        return "${filePath}_${lastModified}_${targetSize}_s${strengthBucket}_d${isDark}"
    }
}
