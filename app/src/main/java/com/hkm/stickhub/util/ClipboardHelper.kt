package com.hkm.stickhub.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.provider.StickerContentProvider
import java.io.File

object ClipboardHelper {

    fun copyStickerToClipboard(context: Context, sticker: StickerItem): Boolean {
        return try {
            val file = File(sticker.filePath)
            if (!file.exists() || !file.isFile) return false

            val stickerUri = StickerContentProvider.getStickerUri(context, file)
            val mimeType = StickerMimeTypes.fromFileName(file.name)

            val clipData = ClipData(
                ClipDescription("Sticker", arrayOf(mimeType, "image/*")),
                ClipData.Item(stickerUri)
            )

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return false
            clipboard.setPrimaryClip(clipData)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Android 13+ (API 33+) provides a built-in system clipboard preview/confirmation.
     * Apps should NOT display redundant custom Toasts or Snackbars on API 33+.
     */
    fun shouldShowCopiedConfirmation(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }

    fun getClipboardImageUri(context: Context): Uri? {
        return getClipboardImageUris(context).firstOrNull()
    }

    /**
     * All eligible image URIs on the clipboard (multi-item ClipData supported).
     * Own stickers copied out of StickHub are filtered so they are never
     * offered back for import.
     */
    fun getClipboardImageUris(context: Context): List<Uri> {
        return getClipboardImagesStamped(context).first
    }

    /**
     * Image URIs plus a copy-event id. The id is the clip timestamp on
     * API 26+ and 0 below that (callers fall back to their own revision
     * counter driven by the primary-clip listener).
     */
    fun getClipboardImagesStamped(context: Context): Pair<List<Uri>, Long> {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return emptyList<Uri>() to 0L
            val clip = clipboard.primaryClip ?: return emptyList<Uri>() to 0L
            val stamp = clipEventId(clipboard)
            val out = mutableListOf<Uri>()
            val hasUriList = try {
                clip.description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST) == true
            } catch (_: Exception) {
                false
            }
            for (i in 0 until clip.itemCount) {
                val item = clip.getItemAt(i) ?: continue
                // Direct URI first, then every URI hidden inside a text/uri-list
                // payload. Some gallery apps copy N photos as one item whose text
                // holds N uri lines — reading item.uri alone kept only the first.
                val candidates = mutableListOf<Uri>()
                item.uri?.let { candidates.add(it) }
                if (hasUriList) {
                    extractUriListText(context, item)?.lineSequence()?.forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty()) {
                            try {
                                Uri.parse(trimmed)?.let { candidates.add(it) }
                            } catch (_: Exception) {
                                // Not a URI line; ignore it.
                            }
                        }
                    }
                }
                for (uri in candidates) {
                    if (uri in out) continue
                    if (isEligibleClipboardImageUri(context, clip.description, uri)) {
                        out.add(uri)
                    }
                }
            }
            return out to stamp
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList<Uri>() to 0L
    }

    /**
     * Best-effort read of a clipboard item's textual payload. Plain text wins;
     * coercion is only a fallback for producers that fill htmlText alone.
     */
    private fun extractUriListText(context: Context, item: ClipData.Item): String? {
        return try {
            item.text?.toString() ?: item.coerceToText(context)?.toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun isEligibleClipboardImageUri(
        context: Context,
        description: ClipDescription,
        uri: Uri
    ): Boolean {
        // A StickerContentProvider URI is a sticker copied *out* of StickHub. Never
        // surface it as a candidate to import back into the library.
        if (ClipboardImportPolicy.isOwnStickerSource(uri.scheme, uri.authority)) {
            return false
        }
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val declaredMimeTypes = listOf(
            ClipDescription.MIMETYPE_TEXT_URILIST,
            "image/*",
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif",
            "image/heic"
        ).filter(description::hasMimeType)
        if (
            ClipboardImportPolicy.isEligibleImage(
                scheme = uri.scheme,
                authority = uri.authority,
                resolvedMimeType = mimeType,
                declaredMimeTypes = declaredMimeTypes
            )
        ) {
            return true
        }
        // Some clipboard producers expose only text/uri-list. Verify the actual
        // stream headers instead of accepting arbitrary content:// data.
        return isDecodableImage(context, uri)
    }

    fun hasImageInClipboard(context: Context): Boolean {
        return getClipboardImageUri(context) != null
    }

    /**
     * Copy-event identity for [getClipboardImagesStamped]. ClipDescription
     * timestamps exist only on API 26+; below that callers must drive their
     * own revision counter from the primary-clip listener.
     */
    fun clipEventId(clipboard: ClipboardManager): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                clipboard.primaryClipDescription?.timestamp ?: 0L
            } else {
                0L
            }
        } catch (_: Exception) {
            0L
        }
    }

    private fun isDecodableImage(context: Context, uri: Uri): Boolean {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
            options.outWidth > 0 && options.outHeight > 0
        } catch (_: Exception) {
            false
        }
    }
}
