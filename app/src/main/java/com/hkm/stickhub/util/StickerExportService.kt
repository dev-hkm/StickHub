package com.hkm.stickhub.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.hkm.stickhub.data.provider.StickerContentProvider
import java.io.File
import java.util.UUID

/**
 * Single choke point for every sticker that leaves StickHub, no matter the
 * caller (library tap, overlay tap, cutout copy, detail share, IME insert).
 *
 * Transport contract, stated plainly: StickHub sends a valid transparent
 * image payload (PNG 512x512 compatibility envelope) through public Android
 * APIs — URI, MIME type, ClipData, temporary read permission, raw bytes.
 * That envelope is NOT a universal native sticker object. Whether a target
 * app renders it compactly (as Zalo does) or as a large photo (as Messenger
 * does) is decided solely by the receiving app's own renderer/catalog, which
 * no sender-side pixel count, canvas size, label, filename or MIME string can
 * override. Do not add invented "sticker" MIME types to pretend otherwise.
 */
object StickerExportService {

    /** Why a payload is being produced. Same bytes, same envelope, every path. */
    enum class ExportPurpose {
        CLIPBOARD,
        SHARE,
        IME
    }

    /** Today every payload lives under the 24h share-cache TTL. */
    enum class CleanupPolicy {
        SHARED_TTL
    }

    /**
     * One validated payload ready to hand to clipboard/share/IME.
     *
     * @property fromOriginal true when the transport encode was unavailable
     * and the untouched original library file is shared instead. Bytes and
     * MIME always agree in both branches.
     */
    data class ExportPayload(
        val file: File,
        val uri: Uri,
        val mimeType: String,
        /** Basename + length only; safe for logs, never a full path. */
        val sourceIdentity: String,
        val purpose: ExportPurpose,
        val fromOriginal: Boolean,
        val cleanupPolicy: CleanupPolicy = CleanupPolicy.SHARED_TTL
    )

    sealed interface ExportSource {
        data class LibraryFile(val file: File) : ExportSource
        data class BitmapSource(val bitmap: Bitmap) : ExportSource
    }

    /**
     * Honest user-facing copy: the receiving app decides photo vs sticker.
     * Reuse verbatim wherever a target cannot take rich content.
     */
    const val TARGET_DECIDES_MESSAGE =
        "Ứng dụng đích quyết định hiển thị ảnh hay sticker. " +
            "Hãy dùng StickHub Keyboard nếu ứng dụng hỗ trợ chèn nội dung trực tiếp."

    /**
     * Encodes and validates one payload. The library source file is never
     * rewritten: transport bytes are always copy-on-write into a new file.
     * Returns null for missing/corrupt/fully-transparent sources, recycled
     * bitmaps, zero-byte output or out-of-memory during encode.
     *
     * Heavy bitmap work happens here, so callers must invoke this off the
     * main thread for large sources (the IME path does; legacy tap-to-copy
     * keeps its historical main-thread behavior to avoid Zalo regressions).
     */
    fun export(context: Context, source: ExportSource, purpose: ExportPurpose): ExportPayload? {
        val appContext = context.applicationContext
        return try {
            when (source) {
                is ExportSource.LibraryFile -> exportFile(appContext, source.file, purpose, allowOriginalFallback = true)
                is ExportSource.BitmapSource -> exportBitmap(appContext, source.bitmap, purpose)
            }
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun exportFile(
        appContext: Context,
        file: File,
        purpose: ExportPurpose,
        allowOriginalFallback: Boolean
    ): ExportPayload? {
        if (!file.isFile || file.length() <= 0L) return null
        val identity = "${file.name}:${file.length()}"
        val transport = try {
            StickerTransport.prepare(appContext, file)
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
        if (transport != null && verifyPayload(transport.file)) {
            return ExportPayload(
                file = transport.file,
                uri = StickerContentProvider.getClipboardUri(appContext, transport.file),
                mimeType = transport.mimeType,
                sourceIdentity = identity,
                purpose = purpose,
                fromOriginal = false
            )
        }
        transport?.file?.delete()
        // Compatibility fallback, library files only: share the untouched
        // original with a MIME sniffed from its real bytes, never blindly
        // trusted from extension. Bitmap sources have no stable original
        // (their temp file is deleted below), so they fail instead of
        // publishing a URI that points at a deleted file.
        if (!allowOriginalFallback) return null
        val sniffed = sniffFileExtension(file) ?: file.name.substringAfterLast('.', "")
        return ExportPayload(
            file = file,
            uri = StickerContentProvider.getStickerUri(appContext, file),
            mimeType = StickerMimeTypes.fromFileName("x.$sniffed"),
            sourceIdentity = identity,
            purpose = purpose,
            fromOriginal = true
        )
    }

    private fun exportBitmap(appContext: Context, bitmap: Bitmap, purpose: ExportPurpose): ExportPayload? {
        if (bitmap.isRecycled) return null
        val source = File(
            appContext.cacheDir,
            "sticker_copy_source_${System.currentTimeMillis()}_${UUID.randomUUID()}.png"
        )
        return try {
            source.outputStream().use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) return null
                output.flush()
            }
            exportFile(appContext, source, purpose, allowOriginalFallback = false)
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        } finally {
            source.delete()
        }
    }

    /**
     * Re-decodes the encoded payload to prove the bytes a receiver will open
     * are a real 512x512 PNG with visible alpha. Transport encode already
     * checks stream success; this guards against truncated/corrupt writes.
     */
    fun verifyPayload(file: File): Boolean {
        return try {
            if (!file.isFile || file.length() <= 0L) return false
            val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
            val decoded = BitmapFactory.decodeFile(file.absolutePath, options) ?: return false
            try {
                if (decoded.width != StickerTransport.CANVAS_SIZE ||
                    decoded.height != StickerTransport.CANVAS_SIZE
                ) {
                    return false
                }
                if (StickerMimeTypes.sniffExtension(readHead(file, 16)) != "png") return false
                var visible = 0
                val row = IntArray(decoded.width)
                var y = 0
                while (y < decoded.height && visible == 0) {
                    decoded.getPixels(row, 0, decoded.width, 0, y, decoded.width, 1)
                    for (pixel in row) {
                        if (android.graphics.Color.alpha(pixel) >= 8) {
                            visible++
                            break
                        }
                    }
                    y++
                }
                visible > 0
            } finally {
                decoded.recycle()
            }
        } catch (_: OutOfMemoryError) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Single clipboard shape for every copy caller: exactly one content-URI
     * item with the payload's real MIME. The label is descriptive only; it
     * carries no native-sticker semantics. No text items, no raw uri-lists,
     * no invented MIME types.
     */
    fun buildClipData(context: Context, payload: ExportPayload): ClipData {
        return buildClipData(context, payload.uri, payload.mimeType)
    }

    /** URI+MIME core shared by [buildClipData] and legacy clipboard callers. */
    fun buildClipData(context: Context, uri: Uri, mimeType: String): ClipData {
        val inferred = runCatching {
            ClipData.newUri(context.contentResolver, "Sticker", uri)
        }.getOrNull()
        return if (inferred != null && inferred.description.hasMimeType("image/*")) {
            inferred
        } else {
            ClipData(
                ClipDescription("Sticker", arrayOf(mimeType, "image/*")),
                ClipData.Item(uri)
            )
        }
    }

    /**
     * Canonical ACTION_SEND shape: type = real payload MIME (image/png for the
     * default transport), EXTRA_STREAM and clipData carrying the same content
     * URI, FLAG_GRANT_READ_URI_PERMISSION. Callers wrap with createChooser.
     * No type wildcard, no file://, no fake sticker MIME, no semantic extras.
     */
    fun buildShareIntent(context: Context, payload: ExportPayload): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = payload.mimeType
            putExtra(Intent.EXTRA_STREAM, payload.uri)
            // Same provider-backed typed URI clip as the copy path. A raw-URI
            // clip would expose only text/uri-list and can make chat clients
            // fall back to a photo.
            clipData = buildClipData(context, payload)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /** Best-effort TTL sweep; never deletes a payload still within its window. */
    fun cleanup(context: Context) {
        StickerTransport.cleanup(context.applicationContext)
    }

    private fun sniffFileExtension(file: File): String? {
        return try {
            file.inputStream().use { input ->
                val head = ByteArray(16)
                var filled = 0
                while (filled < head.size) {
                    val read = input.read(head, filled, head.size - filled)
                    if (read <= 0) break
                    filled += read
                }
                if (filled <= 0) null else StickerMimeTypes.sniffExtension(head.copyOf(filled))
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readHead(file: File, size: Int): ByteArray {
        return try {
            file.inputStream().use { input ->
                val head = ByteArray(size)
                var filled = 0
                while (filled < head.size) {
                    val read = input.read(head, filled, head.size - filled)
                    if (read <= 0) break
                    filled += read
                }
                head.copyOf(filled)
            }
        } catch (_: Exception) {
            ByteArray(0)
        }
    }
}
