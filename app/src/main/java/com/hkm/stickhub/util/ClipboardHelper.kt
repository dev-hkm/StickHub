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
            val mimeType = if (file.name.endsWith(".webp", ignoreCase = true)) "image/webp" else "image/png"

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
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
            val clip = clipboard.primaryClip ?: return null
            if (clip.itemCount > 0) {
                val item = clip.getItemAt(0)
                val uri = item.uri
                if (uri != null) {
                    // A StickerContentProvider URI is a sticker copied *out* of StickHub. Never
                    // surface it as a candidate to import back into the library.
                    if (ClipboardImportPolicy.isOwnStickerSource(uri.scheme, uri.authority)) {
                        return null
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
                    ).filter(clip.description::hasMimeType)
                    if (
                        ClipboardImportPolicy.isEligibleImage(
                            scheme = uri.scheme,
                            authority = uri.authority,
                            resolvedMimeType = mimeType,
                            declaredMimeTypes = declaredMimeTypes
                        )
                    ) {
                        return uri
                    }
                    // Some clipboard producers expose only text/uri-list. Verify the actual
                    // stream headers instead of accepting arbitrary content:// data.
                    if (isDecodableImage(context, uri)) {
                        return uri
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun hasImageInClipboard(context: Context): Boolean {
        return getClipboardImageUri(context) != null
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
