package com.hkm.stickhub.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Builds the small, transparent image payload used when a sticker leaves
 * StickHub. Library files intentionally keep their original 1024px canvas and
 * PNG fidelity; clipboard/share/IME consumers get a conventional 512px
 * transparent PNG compatibility envelope instead. PNG is used because it is
 * the image MIME Android clipboard consumers most widely negotiate — not
 * because pixels, canvas size or filenames can force any receiving app to
 * render a native sticker. That decision belongs to the target app alone
 * (see [StickerExportService] for the full contract). The library file
 * itself is never rewritten.
 */
object StickerTransport {
    const val CANVAS_SIZE = 512
    const val CONTENT_MAX_SIZE = 480

    private const val MAX_SOURCE_DIMENSION = 2048
    private const val MIN_VISIBLE_ALPHA = 8
    private const val EDGE_PADDING = 2
    private const val CACHE_DIR = "sticker_share"
    private const val MAX_CACHE_AGE_MS = 24L * 60L * 60L * 1000L

    data class Payload(
        val file: File,
        val mimeType: String = StickerMimeTypes.PNG
    )

    /**
     * Encodes one bounded, square sticker payload in the app-private cache.
     * Returns null for a missing, corrupt, or fully transparent source.
     */
    fun prepare(context: Context, source: File): Payload? {
        if (!source.isFile || source.length() <= 0L) return null
        val bitmap = decodeBounded(source) ?: return null
        return try {
            val visibleBounds = alphaBounds(bitmap) ?: return null
            val paddedBounds = paddedBounds(visibleBounds, bitmap.width, bitmap.height)
            val output = Bitmap.createBitmap(CANVAS_SIZE, CANVAS_SIZE, Bitmap.Config.ARGB_8888)
            try {
                val scale = min(
                    CONTENT_MAX_SIZE.toFloat() / paddedBounds.width().coerceAtLeast(1),
                    CONTENT_MAX_SIZE.toFloat() / paddedBounds.height().coerceAtLeast(1)
                )
                val drawWidth = (paddedBounds.width() * scale).roundToInt().coerceAtLeast(1)
                val drawHeight = (paddedBounds.height() * scale).roundToInt().coerceAtLeast(1)
                val left = (CANVAS_SIZE - drawWidth) / 2
                val top = (CANVAS_SIZE - drawHeight) / 2

                // Keep the transport path deterministic on all API levels. A
                // Canvas draw into a transparent bitmap is normally fine on a
                // device, but a few OEM graphics implementations have been
                // observed to drop the alpha layer when the source and target
                // rectangles are scaled in one call. Crop/scale first, then
                // copy the pixels into the transparent envelope explicitly.
                val cropped = Bitmap.createBitmap(
                    bitmap,
                    paddedBounds.left,
                    paddedBounds.top,
                    paddedBounds.width(),
                    paddedBounds.height()
                )
                val scaled = try {
                    Bitmap.createScaledBitmap(cropped, drawWidth, drawHeight, true)
                } catch (e: Exception) {
                    if (cropped !== bitmap) cropped.recycle()
                    throw e
                }
                try {
                    val pixels = IntArray(drawWidth * drawHeight)
                    scaled.getPixels(pixels, 0, drawWidth, 0, 0, drawWidth, drawHeight)
                    output.setPixels(pixels, 0, drawWidth, left, top, drawWidth, drawHeight)
                } finally {
                    if (scaled !== cropped) scaled.recycle()
                    if (cropped !== bitmap) cropped.recycle()
                }

                val directory = File(context.cacheDir, CACHE_DIR).apply { mkdirs() }
                cleanup(directory)
                val target = File(directory, "share_${System.currentTimeMillis()}_${UUID.randomUUID()}.png")
                val encoded = FileOutputStream(target).use { stream ->
                    output.compress(Bitmap.CompressFormat.PNG, 100, stream).also { stream.flush() }
                }
                if (!encoded || target.length() <= 0L) {
                    target.delete()
                    null
                } else {
                    Payload(target)
                }
            } finally {
                output.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    /** Removes transport files that can no longer be referenced by a recent share. */
    fun cleanup(context: Context) {
        cleanup(File(context.cacheDir, CACHE_DIR))
    }

    private fun cleanup(directory: File) {
        val cutoff = System.currentTimeMillis() - MAX_CACHE_AGE_MS
        try {
            if (!directory.isDirectory) return
            directory.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoff) {
                    file.delete()
                }
            }
        } catch (_: Exception) {
            // Cache cleanup is best effort; never block copying a sticker.
        }
    }

    private fun decodeBounded(source: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (max(bounds.outWidth / sample, bounds.outHeight / sample) > MAX_SOURCE_DIMENSION) {
            sample = (sample * 2).coerceAtMost(128)
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inMutable = false
        }
        return BitmapFactory.decodeFile(source.absolutePath, options)
    }

    private fun alphaBounds(bitmap: Bitmap): Rect? {
        val width = bitmap.width
        val height = bitmap.height
        var left = width
        var top = height
        var right = 0
        var bottom = 0
        val row = IntArray(width)
        for (y in 0 until height) {
            bitmap.getPixels(row, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                if (Color.alpha(row[x]) >= MIN_VISIBLE_ALPHA) {
                    if (x < left) left = x
                    if (y < top) top = y
                    if (x + 1 > right) right = x + 1
                    if (y + 1 > bottom) bottom = y + 1
                }
            }
        }
        return if (left < right && top < bottom) Rect(left, top, right, bottom) else null
    }

    private fun paddedBounds(bounds: Rect, width: Int, height: Int): Rect {
        return Rect(
            (bounds.left - EDGE_PADDING).coerceAtLeast(0),
            (bounds.top - EDGE_PADDING).coerceAtLeast(0),
            (bounds.right + EDGE_PADDING).coerceAtMost(width),
            (bounds.bottom + EDGE_PADDING).coerceAtMost(height)
        )
    }
}
