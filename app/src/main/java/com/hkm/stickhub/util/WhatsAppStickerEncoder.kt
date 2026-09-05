package com.hkm.stickhub.util

import android.graphics.Bitmap
import android.os.Build
import java.io.File

/**
 * WhatsApp-native sticker encoding. WhatsApp's public third-party contract
 * requires static WebP exactly 512x512 under 100 KB per sticker and a PNG
 * tray icon 24-512px under 50 KB — see WhatsApp/stickers StickerPackValidator
 * (EMOJI_MIN_LIMIT, STATIC_STICKER_FILE_LIMIT_KB, TRAY_IMAGE_*). Pixels come
 * from [StickerTransport.renderEnvelope], so pack art is identical to the
 * clipboard envelope, only re-encoded.
 */
object WhatsAppStickerEncoder {
    const val STICKER_SIZE = 512
    const val STICKER_MAX_BYTES = 100 * 1024L
    const val TRAY_SIZE = 96
    const val TRAY_MAX_BYTES = 50 * 1024L

    private val QUALITY_STEPS = intArrayOf(90, 80, 70, 60, 50)

    /**
     * Encodes [source] to a WhatsApp-conformant static WebP at [target].
     * False means the source had no visible content, encode failed, or no
     * quality step fit under 100 KB.
     */
    fun encodeSticker(source: File, target: File): Boolean {
        return encodeSticker(source, target) { bitmap, quality, stream ->
            bitmap.compress(webpFormat(), quality, stream)
        }
    }

    /**
     * Codec seam: production passes the framework WebP encoder; tests inject
     * a fake because Robolectric's Bitmap shadow cannot emit real WebP bytes
     * (it writes PNG regardless of format). What IS verified here — quality
     * stepping, the 100 KB gate, byte pass-through, cleanup — is all
     * app logic; the framework codec call itself is one line.
     */
    internal fun encodeSticker(
        source: File,
        target: File,
        compress: (Bitmap, Int, java.io.OutputStream) -> Boolean
    ): Boolean {
        val envelope = try {
            StickerTransport.renderEnvelope(source)
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        } ?: return false
        try {
            if (envelope.width != STICKER_SIZE || envelope.height != STICKER_SIZE) return false
            for (quality in QUALITY_STEPS) {
                try {
                    target.outputStream().use { output ->
                        if (!compress(envelope, quality, output)) {
                            target.delete()
                            return false
                        }
                        output.flush()
                    }
                } catch (_: OutOfMemoryError) {
                    target.delete()
                    return false
                } catch (_: Exception) {
                    target.delete()
                    return false
                }
                val length = target.length()
                if (length in 1..STICKER_MAX_BYTES) return true
            }
            target.delete()
            return false
        } finally {
            envelope.recycle()
        }
    }

    /**
     * 96x96 PNG tray icon from the same envelope. Practically always far
     * under the 50 KB cap; still enforced.
     */
    fun encodeTrayIcon(source: File, target: File): Boolean {
        val envelope = try {
            StickerTransport.renderEnvelope(source)
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        } ?: return false
        try {
            val scaled = try {
                Bitmap.createScaledBitmap(envelope, TRAY_SIZE, TRAY_SIZE, true)
            } catch (_: OutOfMemoryError) {
                null
            } catch (_: Exception) {
                null
            } ?: return false
            try {
                try {
                    target.outputStream().use { output ->
                        if (!scaled.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                            target.delete()
                            return false
                        }
                        output.flush()
                    }
                } catch (_: Exception) {
                    target.delete()
                    return false
                }
                val length = target.length()
                if (length !in 1..TRAY_MAX_BYTES) {
                    target.delete()
                    return false
                }
                return true
            } finally {
                if (scaled !== envelope) scaled.recycle()
            }
        } finally {
            envelope.recycle()
        }
    }

    private fun webpFormat(): Bitmap.CompressFormat {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
    }
}
