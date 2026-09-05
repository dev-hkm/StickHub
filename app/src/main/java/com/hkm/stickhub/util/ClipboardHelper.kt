package com.hkm.stickhub.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import com.hkm.stickhub.data.model.StickerItem
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

            // Single shared export path (see StickerExportService): the library
            // file stays untouched while receivers get the validated 512px
            // transparent PNG envelope. Messaging clients make their
            // sticker/photo decision from the delivered payload, not from
            // StickHub's internal 1024px library canvas.
            val payload = StickerExportService.export(
                context,
                StickerExportService.ExportSource.LibraryFile(file),
                StickerExportService.ExportPurpose.CLIPBOARD
            ) ?: return false
            setClipboardPayload(context, payload.uri, payload.mimeType)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Copies a freshly cut subject without forcing the user to save it first. */
    fun copyBitmapToClipboard(context: Context, bitmap: android.graphics.Bitmap): Boolean {
        return try {
            val payload = StickerExportService.export(
                context,
                StickerExportService.ExportSource.BitmapSource(bitmap),
                StickerExportService.ExportPurpose.CLIPBOARD
            ) ?: return false
            setClipboardPayload(context, payload.uri, payload.mimeType)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun setClipboardPayload(context: Context, uri: Uri, mimeType: String): Boolean {
        val clipData = createImageClipData(context, uri, mimeType)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return false
        clipboard.setPrimaryClip(clipData)
        return true
    }

    /**
     * Builds a URI clip using the payload's real MIME type. Android's
     * [ClipData.newUri] path is important here: paste targets can negotiate a
     * typed image stream from the URI instead of receiving an opaque
     * text/URI-list reference. The explicit fallback keeps copying functional
     * if a target/provider lookup is temporarily unavailable.
     *
     * Single implementation lives in [StickerExportService].
     */
    fun createImageClipData(context: Context, uri: Uri, fallbackMimeType: String): ClipData {
        return StickerExportService.buildClipData(context, uri, fallbackMimeType)
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
