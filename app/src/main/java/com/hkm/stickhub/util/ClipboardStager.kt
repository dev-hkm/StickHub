package com.hkm.stickhub.util

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/** Why one staged item could not be materialized. Safe codes, no URIs. */
enum class StageFailure {
    DENIED,
    TOO_LARGE,
    CORRUPT,
    EMPTY,
    IO_ERROR,
    BATCH_LIMIT
}

/**
 * One snapshot candidate after the single allowed source open: either a
 * private temp file with a sniffed container, or an isolated failure that
 * never affects its siblings.
 */
sealed interface StagedClipboardItem {
    val candidate: ClipboardCandidate

    data class Ready(
        override val candidate: ClipboardCandidate,
        val file: File,
        val extension: String,
        val mimeType: String
    ) : StagedClipboardItem

    data class Failed(
        override val candidate: ClipboardCandidate,
        val reason: StageFailure
    ) : StagedClipboardItem
}

/**
 * Opens each snapshot URI exactly once into app-private temp files.
 *
 * - One open per candidate; the staged file is the only thing the importer
 *   ever reads back, so short-lived grants and one-shot streams are safe.
 * - 32 MiB per image ([BackupHelper.MAX_IMAGE_BYTES]); over-limit items fail
 *   alone and are reported, never silently dropped.
 * - Container comes from [StickerMimeTypes.sniffExtension] on real bytes —
 *   a null/`application/octet-stream` resolver answer never rejects an image.
 * - `.partial` files are deleted on failure/cancel; [cleanupStale] wipes
 *   leftovers at startup. No persistable URI permissions are taken and no
 *   media-read permission is needed.
 */
class ClipboardStager(
    private val appContext: Context,
    private val opener: UriOpener = UriOpener { uri ->
        appContext.contentResolver.openInputStream(uri)
    },
    private val maxImageBytes: Long = BackupHelper.MAX_IMAGE_BYTES,
    private val maxItems: Int = MAX_BATCH_ITEMS
) {
    fun interface UriOpener {
        @Throws(Exception::class)
        fun open(uri: Uri): InputStream?
    }

    suspend fun stage(
        snapshot: ClipboardBatchSnapshot,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): List<StagedClipboardItem> = withContext(Dispatchers.IO) {
        val total = snapshot.candidates.size
        val out = ArrayList<StagedClipboardItem>(total)
        for ((index, candidate) in snapshot.candidates.withIndex()) {
            ensureActive()
            val item = if (index >= maxItems) {
                StagedClipboardItem.Failed(candidate, StageFailure.BATCH_LIMIT)
            } else {
                stageOne(candidate, snapshot.generation, index)
            }
            out.add(item)
            onProgress(out.size, total)
        }
        out
    }

    private suspend fun CoroutineScope.stageOne(
        candidate: ClipboardCandidate,
        generation: Long,
        index: Int
    ): StagedClipboardItem {
        val dir = stagingDir(appContext)
        val partial = File(dir, "clip_stage_g${generation}_$index.partial")
        try {
            val stream = try {
                opener.open(candidate.uri)
            } catch (security: SecurityException) {
                Log.d(TAG, "Staging denied for candidate #$index")
                return StagedClipboardItem.Failed(candidate, StageFailure.DENIED)
            } catch (_: Exception) {
                Log.d(TAG, "Staging open failed for candidate #$index")
                return StagedClipboardItem.Failed(candidate, StageFailure.IO_ERROR)
            } ?: run {
                Log.d(TAG, "Staging got no stream for candidate #$index")
                return StagedClipboardItem.Failed(candidate, StageFailure.IO_ERROR)
            }
            val header = ByteArray(SNIFF_BYTES)
            var headerSize = 0
            var totalBytes = 0L
            var overLimit = false
            stream.use { input ->
                partial.outputStream().use { output ->
                    val buffer = ByteArray(COPY_CHUNK)
                    var chunks = 0
                    while (true) {
                        ensureActive()
                        val read = try {
                            input.read(buffer)
                        } catch (security: SecurityException) {
                            Log.d(TAG, "Staging read denied for candidate #$index")
                            return StagedClipboardItem.Failed(candidate, StageFailure.DENIED)
                        }
                        if (read <= 0) break
                        totalBytes += read
                        if (totalBytes > maxImageBytes) {
                            overLimit = true
                            break
                        }
                        // Keep the head for the container sniff.
                        if (headerSize < SNIFF_BYTES) {
                            val take = minOf(read, SNIFF_BYTES - headerSize)
                            System.arraycopy(buffer, 0, header, headerSize, take)
                            headerSize += take
                        }
                        output.write(buffer, 0, read)
                        if (++chunks % 128 == 0) ensureActive()
                    }
                    output.flush()
                }
            }
            if (overLimit) {
                partial.delete()
                Log.d(TAG, "Staging over size cap for candidate #$index")
                return StagedClipboardItem.Failed(candidate, StageFailure.TOO_LARGE)
            }
            if (totalBytes <= 0L) {
                partial.delete()
                return StagedClipboardItem.Failed(candidate, StageFailure.EMPTY)
            }
            val extension = StickerMimeTypes.sniffExtension(header.copyOf(headerSize))
                ?: run {
                    partial.delete()
                    Log.d(TAG, "Staging found no image container for candidate #$index")
                    return StagedClipboardItem.Failed(candidate, StageFailure.CORRUPT)
                }
            val staged = File(dir, "clip_stage_g${generation}_$index.$extension")
            try {
                if (staged.exists()) staged.delete()
                if (!partial.renameTo(staged)) {
                    partial.copyTo(staged, overwrite = true)
                    partial.delete()
                }
            } catch (_: Exception) {
                partial.delete()
                return StagedClipboardItem.Failed(candidate, StageFailure.IO_ERROR)
            }
            val mime = when (extension) {
                "jpg" -> StickerMimeTypes.JPEG
                "webp" -> StickerMimeTypes.WEBP
                "gif" -> StickerMimeTypes.GIF
                "heic", "heif" -> StickerMimeTypes.HEIC
                else -> StickerMimeTypes.PNG
            }
            return StagedClipboardItem.Ready(candidate, staged, extension, mime)
        } catch (cancelled: CancellationException) {
            partial.delete()
            throw cancelled
        } catch (_: Exception) {
            partial.delete()
            return StagedClipboardItem.Failed(candidate, StageFailure.IO_ERROR)
        }
    }

    companion object {
        const val MAX_BATCH_ITEMS = 50
        private const val TAG = "ClipboardStage"
        private const val COPY_CHUNK = 8192
        private const val SNIFF_BYTES = 16

        fun stagingDir(context: Context): File {
            return File(context.cacheDir, "clipboard_stage").apply { mkdirs() }
        }

        /** Startup sweep: staged temps are single-session by design. */
        fun cleanupStale(context: Context) {
            try {
                val dir = File(context.cacheDir, "clipboard_stage")
                if (dir.isDirectory) {
                    dir.listFiles()?.forEach { file ->
                        try {
                            if (file.isFile) file.delete()
                        } catch (_: Exception) {
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}
