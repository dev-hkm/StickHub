package com.hkm.stickhub.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.provider.StickerContentProvider
import java.io.File

object ClipboardHelper {

    private const val TAG = "ClipboardImport"

    /** What a clipboard scan found, plus how many candidates were unreadable. */
    data class ClipboardScanResult(
        val uris: List<Uri>,
        val skipped: Int,
        val stamp: Long
    )

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
        return scanClipboardImages(context).let { it.uris to it.stamp }
    }

    /**
     * Full multi-shape scan. Clipboard producers disagree wildly: direct item
     * URIs, text/uri-list (or plain text) holding one URI per line, <img> tags
     * in HTML payloads, and ACTION_SEND intents with EXTRA_STREAM(S). Every
     * shape is harvested and every candidate is gated, so a producer using
     * any of them yields the whole set instead of just the first image.
     */
    fun scanClipboardImages(context: Context): ClipboardScanResult {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return ClipboardScanResult(emptyList(), 0, 0L)
            val clip = clipboard.primaryClip ?: return ClipboardScanResult(emptyList(), 0, 0L)
            val stamp = clipEventId(clipboard)
            val out = mutableListOf<Uri>()
            var skipped = 0
            for (i in 0 until clip.itemCount) {
                val item = clip.getItemAt(i) ?: continue
                val candidates = collectItemUris(context, item)
                for (uri in candidates) {
                    if (uri in out) continue
                    // One poisoned URI (dead provider, revoked grant, getType
                    // throwing) must never truncate the URIs after it.
                    val eligible = try {
                        isEligibleClipboardImageUri(context, clip.description, uri)
                    } catch (e: Exception) {
                        Log.d(TAG, "Skipping ineligible clipboard uri $uri", e)
                        false
                    }
                    if (eligible) {
                        out.add(uri)
                    } else if (!ClipboardImportPolicy.isOwnStickerSource(uri.scheme, uri.authority)) {
                        skipped++
                    }
                }
            }
            if (skipped > 0) Log.d(TAG, "Clipboard scan kept ${out.size}, skipped $skipped")
            return ClipboardScanResult(out, skipped, stamp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ClipboardScanResult(emptyList(), 0, 0L)
    }

    /** Every URI shape a single clip item can carry. Never throws. */
    private fun collectItemUris(context: Context, item: ClipData.Item): List<Uri> {
        val candidates = mutableListOf<Uri>()
        try {
            // Intent-backed items surface a pseudo "intent:" URI here; the real
            // streams come from extractIntentUris below.
            item.uri?.takeUnless { it.scheme.equals("intent", ignoreCase = true) }
                ?.let { candidates.add(it) }
        } catch (_: Exception) {
            Log.d(TAG, "Skipping unreadable clipboard item uri")
        }
        // Plain or uri-list text: one URI per line. Parsed regardless of the
        // declared mime type — producers mix text/plain and text/uri-list freely.
        extractUriListText(context, item)?.lineSequence()?.forEach { line ->
            sanitizeUriLine(line)?.let { candidates.add(it) }
        }
        // HTML payloads (<img src="content://...">).
        try {
            item.htmlText?.toString()?.let { html ->
                UriLinePattern.findAll(html).forEach { match ->
                    sanitizeUriLine(match.value)?.let { candidates.add(it) }
                }
            }
        } catch (_: Exception) {
        }
        // ACTION_SEND(_MULTIPLE) intents carrying EXTRA_STREAM(S).
        candidates.addAll(extractIntentUris(item))
        // Intent-backed items leak their pseudo "intent:#Intent;..." form through
        // coerceToText above; that is an envelope, never an image.
        return candidates.filterNot { it.scheme.equals("intent", ignoreCase = true) }
    }

    private fun sanitizeUriLine(raw: String): Uri? {
        val trimmed = raw.trim().trimEnd(',', ';', '.', '!', '?', ')', '"', '\'')
        if (trimmed.isEmpty()) return null
        return try {
            Uri.parse(trimmed)
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun extractIntentUris(item: ClipData.Item): List<Uri> {
        return try {
            val intent = item.intent ?: return emptyList()
            val out = mutableListOf<Uri>()
            try {
                intent.data?.let { out.add(it) }
            } catch (_: Exception) {
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { out.add(it) }
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { out.addAll(it) }
                } else {
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { out.add(it) }
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { out.addAll(it) }
                }
            } catch (_: Exception) {
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    private val UriLinePattern = Regex("""(content|file)://[^\s"\'<>]+""")

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
