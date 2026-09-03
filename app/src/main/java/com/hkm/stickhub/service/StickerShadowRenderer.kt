package com.hkm.stickhub.service

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.util.LruCache

/**
 * High-performance silhouette shadow renderer for sticker thumbnails.
 * Caches composite bitmaps in memory so grid scrolling stays at 60fps.
 */
object StickerShadowRenderer {
    // 16 MB memory cache
    private val shadowBitmapCache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun getCached(key: String): Bitmap? = shadowBitmapCache.get(key)

    fun putCache(key: String, bitmap: Bitmap) {
        shadowBitmapCache.put(key, bitmap)
    }

    fun clearCache() {
        shadowBitmapCache.evictAll()
    }

    /**
     * Renders a sticker bitmap with a silhouette-conforming drop shadow.
     */
    fun renderWithShadow(
        source: Bitmap,
        shadowStrength: Float,
        isDark: Boolean,
        density: Float
    ): Bitmap {
        if (!StickerShadowPolicy.isShadowEnabled(shadowStrength)) {
            return source
        }

        val blurRadius = StickerShadowPolicy.resolveBlurRadius(shadowStrength, density)
        val shadowColor = StickerShadowPolicy.resolveShadowColor(shadowStrength, isDark)

        val pad = (blurRadius * 1.5f).toInt().coerceAtLeast(4)
        val outW = source.width + pad * 2
        val outH = source.height + pad * 2

        val result = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = shadowColor
            maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        }

        val offset = IntArray(2)
        val alphaMask = try {
            source.extractAlpha(shadowPaint, offset)
        } catch (_: Exception) {
            null
        }

        if (alphaMask != null) {
            val dy = 1.5f * density
            canvas.drawBitmap(alphaMask, (pad + offset[0]).toFloat(), (pad + offset[1] + dy), shadowPaint)
            alphaMask.recycle()
        }

        val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, pad.toFloat(), pad.toFloat(), drawPaint)

        return result
    }
}
