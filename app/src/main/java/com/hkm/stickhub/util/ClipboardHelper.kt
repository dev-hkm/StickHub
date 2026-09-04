package com.hkm.stickhub.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.provider.StickerContentProvider
import java.io.File

object ClipboardHelper {

    private const val TAG = "ClipboardImport"

    /** Compatibility result for older callers that only need URI/count data. */
    data class ClipboardScanResult(
        val uris: List<Uri>,
        val skipped: Int,
        val stamp: Long
    )

    fun copyStickerToClipboard(context: Context, sticker: StickerItem): Boolean {
        return try {
            val file = File(sticker.filePath)
            if (!file.exists() || !file.isFile) return false

            // Messaging clients make their sticker/photo decision from the
            // delivered payload, not from StickHub's internal 1024px library
            // canvas. Prepare a bounded, transparent WebP envelope while
            // retaining the original library file untouched.
            val payload = StickerTransport.prepare(context, file)
            setClipboardPayload(
                context = context,
                uri = payload?.let { StickerContentProvider.getClipboardUri(context, it.file) }
                    ?: StickerContentProvider.getStickerUri(context, file),
                mimeType = payload?.mimeType ?: StickerMimeTypes.fromFileName(file.name)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Copies a freshly cut subject without forcing the user to save it first. */
    fun copyBitmapToClipboard(context: Context, bitmap: android.graphics.Bitmap): Boolean {
        if (bitmap.isRecycled) return false
        val source = File(
            context.cacheDir,
            "sticker_copy_source_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.png"
        )
        return try {
            source.outputStream().use { output ->
                if (!bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)) {
                    return false
                }
                output.flush()
            }
            val payload = StickerTransport.prepare(context, source) ?: return false
            setClipboardPayload(
                context = context,
                uri = StickerContentProvider.getClipboardUri(context, payload.file),
                mimeType = payload.mimeType
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            source.delete()
        }
    }

    private fun setClipboardPayload(context: Context, uri: Uri, mimeType: String): Boolean {
        val clipData = ClipData(
            ClipDescription("Sticker", arrayOf(mimeType, "image/*")),
            ClipData.Item(uri)
        )
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return false
        clipboard.setPrimaryClip(clipData)
        return true
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
     * Captures the current primary clip synchronously while the Activity is in
     * the foreground. This deliberately performs no resolver I/O: provider
     * streams are opened exactly once later by [ClipboardStager] after the
     * user chooses to review/import the frozen batch.
     */
    fun captureClipboardBatch(context: Context, generation: Long): ClipboardBatchSnapshot? {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return null
            val clip = clipboard.primaryClip ?: return null
            val stamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try { clip.description.timestamp } catch (_: Exception) { 0L }
            } else {
                0L
            }
            ClipboardBatchFactory.build(
                generation = generation,
                origin = BatchOrigin.CLIPBOARD,
                sourceItemCount = clip.itemCount,
                stamp = stamp,
                harvested = ClipboardUriHarvester.harvestClipData(clip),
                // MIME and bytes are intentionally checked only during staging.
                // A null/octet-stream provider must not make a valid image vanish.
                resolveMimeType = { null }
            )
        } catch (_: Exception) {
            null
        }
    }

    /** Creates the same immutable batch contract for an inbound share Intent. */
    fun captureShareBatch(intent: android.content.Intent, generation: Long): ClipboardBatchSnapshot {
        val itemCount = try { intent.clipData?.itemCount ?: 0 } catch (_: Exception) { 0 }
        return ClipboardBatchFactory.build(
            generation = generation,
            origin = BatchOrigin.SHARE,
            sourceItemCount = itemCount,
            stamp = 0L,
            harvested = ClipboardUriHarvester.harvestIntent(intent),
            resolveMimeType = { null }
        )
    }

    /** Legacy URI-only scan retained for existing callers and tests. */
    fun scanClipboardImages(context: Context): ClipboardScanResult {
        val batch = captureClipboardBatch(context, generation = 0L)
            ?: return ClipboardScanResult(emptyList(), 0, 0L)
        return ClipboardScanResult(
            uris = batch.uris,
            skipped = batch.rejected.size,
            stamp = batch.stamp
        )
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

}
