package com.hkm.stickhub.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.hkm.stickhub.data.db.StickHubDbHelper
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.CategoryValidator
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.util.ClipboardContentHasher
import com.hkm.stickhub.util.ClipboardImportPolicy
import com.hkm.stickhub.util.StagedClipboardItem
import com.hkm.stickhub.util.StickerMimeTypes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

class StickerRepository(private val context: Context) {

    /** Per-item failure kept with its staged file so the UI can retry only it. */
    data class ClipboardBatchFailure(
        val item: StagedClipboardItem.Ready,
        val reason: String
    )

    /** The durable result of committing one staged clipboard batch. */
    data class ClipboardBatchImportResult(
        val saved: List<StickerItem>,
        val duplicates: List<StickerItem>,
        val failed: List<ClipboardBatchFailure>
    )

    /** One validated archive category waiting to be merged. */
    data class BackupRestoreCategory(
        val name: String,
        val isDefault: Boolean,
        val displayOrder: Int
    )

    /** One validated archive sticker row with its staged bytes. */
    data class BackupRestoreSticker(
        val title: String,
        val category: String,
        val tags: String,
        val isFavorite: Boolean,
        val createdAt: Long,
        val usageCount: Int,
        val sortOrder: Long,
        val stagedFile: File,
        val format: String,
        val contentHash: String
    )

    data class BackupRestoreOutcome(val imported: Int, val alreadyPresent: Int)

    companion object {
        @Volatile
        private var sharedInstance: StickerRepository? = null

        /**
         * Process-wide repository sharing one DB owner, one StateFlow pair and
         * one clipboard dedup mutex between MainActivity and OverlayService.
         * Always pass an application context; the factory pins it.
         */
        fun getInstance(context: Context): StickerRepository =
            sharedInstance ?: synchronized(this) {
                sharedInstance ?: StickerRepository(context.applicationContext).also {
                    sharedInstance = it
                }
            }

        /**
         * Test seam only. Never call while the app is running: it closes the
         * shared database helper out from under live readers.
         */
        fun resetSharedInstanceForTests() {
            synchronized(this) {
                try {
                    sharedInstance?.close()
                } catch (_: Exception) {
                }
                sharedInstance = null
            }
        }
    }

    /** Closes the underlying database helper. See [resetSharedInstanceForTests]. */
    fun close() {
        try {
            dbHelper.close()
        } catch (_: Exception) {
        }
    }

    private sealed interface StreamSaveResult {
        data class Saved(val sticker: StickerItem) : StreamSaveResult
        data class Duplicate(val existingSticker: StickerItem) : StreamSaveResult
        data class Failed(val reason: String) : StreamSaveResult
    }

    private val dbHelper = StickHubDbHelper(context)
    val stickersDir = File(context.filesDir, "stickers").apply {
        if (!exists()) mkdirs()
    }

    private val _stickersFlow = MutableStateFlow<List<StickerItem>>(emptyList())
    val stickersFlow: StateFlow<List<StickerItem>> = _stickersFlow.asStateFlow()

    private val _categoriesFlow = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categoriesFlow: StateFlow<List<CategoryItem>> = _categoriesFlow.asStateFlow()
    private val clipboardImportMutex = Mutex()
    private val backupMutex = Mutex()

    /** Test-only observation seam for proving batch imports refresh once. */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var refreshListener: (() -> Unit)? = null

    suspend fun refresh() = withContext(Dispatchers.IO) {
        _stickersFlow.value = getAllStickersInternal()
        _categoriesFlow.value = getAllCategoriesInternal()
        refreshListener?.invoke()
    }

    private fun getAllStickersInternal(): List<StickerItem> {
        val list = mutableListOf<StickerItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            StickHubDbHelper.TABLE_STICKERS,
            null,
            null,
            null,
            null,
            null,
            "${StickHubDbHelper.COL_SORT_ORDER} DESC, ${StickHubDbHelper.COL_CREATED_AT} DESC, ${StickHubDbHelper.COL_ID} DESC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(cursorToSticker(c))
            }
        }
        return list
    }

    private fun getAllCategoriesInternal(): List<CategoryItem> {
        val list = mutableListOf<CategoryItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            StickHubDbHelper.TABLE_CATEGORIES,
            null,
            null,
            null,
            null,
            null,
            "CASE WHEN ${StickHubDbHelper.COL_CAT_NAME} = 'General' THEN 0 ELSE 1 END ASC, ${StickHubDbHelper.COL_CAT_DISPLAY_ORDER} ASC, ${StickHubDbHelper.COL_CAT_ID} ASC"
        )
        cursor.use { c ->
            val orderCol = c.getColumnIndex(StickHubDbHelper.COL_CAT_DISPLAY_ORDER)
            while (c.moveToNext()) {
                val catName = c.getString(c.getColumnIndexOrThrow(StickHubDbHelper.COL_CAT_NAME))
                val isDefault = c.getInt(c.getColumnIndexOrThrow(StickHubDbHelper.COL_CAT_IS_DEFAULT)) == 1 || catName.equals("General", ignoreCase = true)
                val displayOrder = if (orderCol >= 0) c.getInt(orderCol) else 0
                list.add(
                    CategoryItem(
                        id = c.getLong(c.getColumnIndexOrThrow(StickHubDbHelper.COL_CAT_ID)),
                        name = catName,
                        isDefault = isDefault,
                        displayOrder = displayOrder
                    )
                )
            }
        }
        return list
    }

    private fun getStickerByIdInternal(id: Long): StickerItem? {
        val cursor = dbHelper.readableDatabase.query(
            StickHubDbHelper.TABLE_STICKERS,
            null,
            "${StickHubDbHelper.COL_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            "1"
        )
        cursor.use { return if (it.moveToFirst()) cursorToSticker(it) else null }
    }

    /**
     * Category requested by a save, resolved against categories that actually
     * exist right now: the requested (stored-cased) name wins, then the
     * default-flagged category, then a freshly created canonical General.
     * No save path can ever persist a ghost category again.
     */
    private fun resolveCategoryForSaveInternal(requested: String): String {
        val fallback = requested.trim().ifBlank { CategoryItem.FALLBACK_NAME }
        // A broken categories table must never take down a sticker save:
        // degrade to the requested name and let the stickers table win.
        return try {
            val db = dbHelper.readableDatabase
            db.rawQuery(
                "SELECT ${StickHubDbHelper.COL_CAT_NAME} FROM ${StickHubDbHelper.TABLE_CATEGORIES} " +
                    "WHERE ${StickHubDbHelper.COL_CAT_NAME} = ? COLLATE NOCASE LIMIT 1",
                arrayOf(fallback)
            ).use { if (it.moveToFirst()) return it.getString(0) }
            db.rawQuery(
                "SELECT ${StickHubDbHelper.COL_CAT_NAME} FROM ${StickHubDbHelper.TABLE_CATEGORIES} " +
                    "WHERE ${StickHubDbHelper.COL_CAT_IS_DEFAULT} = 1 ORDER BY ${StickHubDbHelper.COL_CAT_ID} LIMIT 1",
                null
            ).use { if (it.moveToFirst()) return it.getString(0) }
            val cv = ContentValues().apply {
                put(StickHubDbHelper.COL_CAT_NAME, CategoryItem.FALLBACK_NAME)
                put(StickHubDbHelper.COL_CAT_IS_DEFAULT, 1)
                put(StickHubDbHelper.COL_CAT_DISPLAY_ORDER, 0)
            }
            dbHelper.writableDatabase.insertWithOnConflict(
                StickHubDbHelper.TABLE_CATEGORIES,
                null,
                cv,
                android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
            )
            CategoryItem.FALLBACK_NAME
        } catch (_: Exception) {
            fallback
        }
    }

    suspend fun saveStickerBitmap(
        bitmap: Bitmap,
        title: String = "",
        category: String = "General",
        tags: String = ""
    ): StickerItem? = withContext(Dispatchers.IO) {
        val resolvedCategory = resolveCategoryForSaveInternal(category)
        val tempFile = File(stickersDir, "tmp_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")
        val finalFile = File(stickersDir, "sticker_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")
        var committed = false

        try {
            FileOutputStream(tempFile).use { out ->
                val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                if (!compressed) {
                    tempFile.delete()
                    return@withContext null
                }
                out.flush()
            }

            if (!tempFile.renameTo(finalFile)) {
                // Fallback copy if rename fails
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }

            val contentHash = finalFile.inputStream().use(ClipboardContentHasher::sha256)
            val now = System.currentTimeMillis()

            val cv = ContentValues().apply {
                put(StickHubDbHelper.COL_FILE_PATH, finalFile.absolutePath)
                put(StickHubDbHelper.COL_TITLE, title.ifBlank { "Sticker #${now % 10000}" })
                put(StickHubDbHelper.COL_CATEGORY, resolvedCategory)
                put(StickHubDbHelper.COL_TAGS, tags)
                put(StickHubDbHelper.COL_IS_FAVORITE, 0)
                put(StickHubDbHelper.COL_CREATED_AT, now)
                put(StickHubDbHelper.COL_SORT_ORDER, now)
                put(StickHubDbHelper.COL_USAGE_COUNT, 0)
                put(StickHubDbHelper.COL_CONTENT_SHA256, contentHash)
            }

            val db = dbHelper.writableDatabase
            val id = db.insert(StickHubDbHelper.TABLE_STICKERS, null, cv)
            if (id == -1L) {
                return@withContext null
            }
            committed = true

            // A snapshot failure must never destroy committed data: refresh in
            // isolation and answer from the durable row itself, not the flow.
            try {
                refresh()
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Exception) {
            }
            getStickerByIdInternal(id)
        } catch (ce: CancellationException) {
            if (!committed) {
                tempFile.delete()
                if (finalFile.exists()) finalFile.delete()
            }
            throw ce
        } catch (e: Exception) {
            if (!committed) {
                tempFile.delete()
                if (finalFile.exists()) finalFile.delete()
            }
            e.printStackTrace()
            null
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    suspend fun overwriteStickerBitmap(id: Long, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        // Copy-on-write: the original bytes stay untouched until the new file
        // is fully verified and the DB row points at it. Direct overwrites can
        // truncate the original on ENOSPC, and writing PNG bytes into a .webp
        // path corrupts the format contract.
        val sticker = getStickerByIdInternal(id) ?: return@withContext false
        val originalFile = File(sticker.filePath)
        if (!originalFile.isFile) return@withContext false
        val newFile = File(stickersDir, "sticker_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")

        try {
            FileOutputStream(newFile).use { out ->
                val ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                if (!ok) {
                    newFile.delete()
                    return@withContext false
                }
                out.flush()
            }

            val contentHash = newFile.inputStream().use(ClipboardContentHasher::sha256)
            val updated = dbHelper.writableDatabase.update(
                StickHubDbHelper.TABLE_STICKERS,
                ContentValues().apply {
                    put(StickHubDbHelper.COL_FILE_PATH, newFile.absolutePath)
                    put(StickHubDbHelper.COL_CONTENT_SHA256, contentHash)
                },
                "${StickHubDbHelper.COL_ID} = ?",
                arrayOf(id.toString())
            )
            if (updated <= 0) {
                newFile.delete()
                return@withContext false
            }

            // Commit won: retire the old file only now. External readers
            // holding the old clipboard URI may 404 afterwards; clipboard
            // lifetime is seconds, while a truncated original would be
            // permanent data loss. Documented tradeoff.
            if (newFile.absolutePath != originalFile.absolutePath && originalFile.exists()) {
                originalFile.delete()
            }
            try {
                refresh()
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Exception) {
            }
            true
        } catch (ce: CancellationException) {
            if (newFile.exists()) newFile.delete()
            throw ce
        } catch (e: Exception) {
            if (newFile.exists()) newFile.delete()
            e.printStackTrace()
            false
        }
    }

    suspend fun saveStickerFromStream(
        inputStream: InputStream,
        title: String = "",
        category: String = "General",
        tags: String = ""
    ): StickerItem? = withContext(Dispatchers.IO) {
        // Cancellation here must surface as a quiet null (the caller joins and
        // then verifies consistency), never as a deleted committed file.
        try {
            when (
                val result = saveStickerFromStreamInternal(
                    inputStream = inputStream,
                    title = title,
                    category = category,
                    tags = tags,
                    fileExtension = "png",
                    rejectDuplicates = false
                )
            ) {
                is StreamSaveResult.Saved -> result.sticker
                is StreamSaveResult.Duplicate,
                is StreamSaveResult.Failed -> null
            }
        } catch (ce: CancellationException) {
            null
        }
    }

    suspend fun saveStickerFromUri(
        uri: Uri,
        title: String = "",
        category: String = "General",
        tags: String = ""
    ): StickerItem? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                saveStickerFromStream(stream, title, category, tags)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Keeps the original quick-import behavior for ready-made stickers copied from Google Photos
     * or another app. It streams the source bytes unchanged and never invokes subject cutout.
     */
    suspend fun importClipboardSticker(
        uri: Uri,
        title: String = "",
        category: String = "General",
        tags: String = ""
    ): ClipboardImportResult = clipboardImportMutex.withLock {
        withContext(Dispatchers.IO) {
            if (ClipboardImportPolicy.isOwnStickerSource(uri.scheme, uri.authority)) {
                return@withContext ClipboardImportResult.OwnSource
            }
            if (!uri.scheme.equals("content", ignoreCase = true) &&
                !uri.scheme.equals("file", ignoreCase = true)
            ) {
                return@withContext ClipboardImportResult.InvalidSource("Clipboard item is not an image URI.")
            }

            val mimeType = try {
                context.contentResolver.getType(uri)
            } catch (_: Exception) {
                null
            }
            if (mimeType != null && !mimeType.startsWith("image/", ignoreCase = true)) {
                return@withContext ClipboardImportResult.InvalidSource("Clipboard item is not an image.")
            }

            val result = try {
                val stream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext ClipboardImportResult.Failed("Couldn't open the clipboard image.")
                stream.use {
                    saveStickerFromStreamInternal(
                        inputStream = it,
                        title = title,
                        category = category,
                        tags = tags,
                        fileExtension = fileExtensionForMimeType(mimeType),
                        rejectDuplicates = true
                    )
                }
            } catch (ce: CancellationException) {
                // A cancelled import is not a failure: propagate so callers
                // don't report it (or worse, clean up committed data).
                throw ce
            } catch (error: Exception) {
                return@withContext ClipboardImportResult.Failed(
                    error.localizedMessage ?: "Couldn't import the clipboard sticker."
                )
            }

            when (result) {
                is StreamSaveResult.Saved -> ClipboardImportResult.Saved(result.sticker)
                is StreamSaveResult.Duplicate -> ClipboardImportResult.Duplicate(result.existingSticker)
                is StreamSaveResult.Failed -> ClipboardImportResult.Failed(result.reason)
            }
        }
    }

    /**
     * Commits already-staged clipboard files. External content URIs are never
     * reopened here: staging owns the one allowed provider read, which makes
     * temporary clipboard grants and one-shot streams reliable.
     */
    suspend fun importStagedClipboardBatch(
        stagedItems: List<StagedClipboardItem.Ready>,
        title: String = "",
        category: String = "General",
        tags: String = ""
    ): ClipboardBatchImportResult = clipboardImportMutex.withLock {
        withContext(Dispatchers.IO) {
            val saved = mutableListOf<StickerItem>()
            val duplicates = mutableListOf<StickerItem>()
            val failed = mutableListOf<ClipboardBatchFailure>()

            for (item in stagedItems) {
                val file = item.file
                if (!file.isFile || file.length() <= 0L) {
                    failed += ClipboardBatchFailure(item, "Prepared image is no longer available.")
                    continue
                }
                val result = try {
                    file.inputStream().use { stream ->
                        saveStickerFromStreamInternal(
                            inputStream = stream,
                            title = title,
                            category = category,
                            tags = tags,
                            fileExtension = item.extension,
                            rejectDuplicates = true,
                            refreshAfterSave = false
                        )
                    }
                } catch (ce: CancellationException) {
                    throw ce
                } catch (error: Exception) {
                    StreamSaveResult.Failed(error.localizedMessage ?: "Couldn't save the clipboard sticker.")
                }

                when (result) {
                    is StreamSaveResult.Saved -> {
                        saved += result.sticker
                        file.delete()
                    }
                    is StreamSaveResult.Duplicate -> {
                        duplicates += result.existingSticker
                        file.delete()
                    }
                    is StreamSaveResult.Failed -> {
                        // Keep a readable staged file so only this item can be retried.
                        failed += ClipboardBatchFailure(item, result.reason)
                    }
                }
            }

            if (stagedItems.isNotEmpty()) {
                try {
                    refresh()
                } catch (ce: CancellationException) {
                    throw ce
                } catch (_: Exception) {
                    // The durable writes above remain valid even if the in-memory
                    // snapshot cannot refresh immediately.
                }
            }
            ClipboardBatchImportResult(saved, duplicates, failed)
        }
    }

    private suspend fun saveStickerFromStreamInternal(
        inputStream: InputStream,
        title: String,
        category: String,
        tags: String,
        fileExtension: String,
        rejectDuplicates: Boolean,
        refreshAfterSave: Boolean = true
    ): StreamSaveResult {
        val safeExtension = fileExtension.lowercase().removePrefix(".").ifBlank { "png" }
        val uniquePart = "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val tempFile = File(stickersDir, "tmp_in_$uniquePart.$safeExtension")
        val finalFile = File(stickersDir, "sticker_$uniquePart.$safeExtension")
        var committed = false

        try {
            FileOutputStream(tempFile).use { output ->
                val buffer = ByteArray(8192)
                while (true) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead <= 0) break
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }

            if (tempFile.length() <= 0L) {
                return StreamSaveResult.Failed("Clipboard image was empty.")
            }

            val contentHash = tempFile.inputStream().use(ClipboardContentHasher::sha256)
            if (rejectDuplicates) {
                val existing = findExistingStickerByContentHash(contentHash)
                if (existing != null) {
                    return StreamSaveResult.Duplicate(existing)
                }
            }

            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }

            val resolvedCategory = resolveCategoryForSaveInternal(category)
            val now = System.currentTimeMillis()
            val values = ContentValues().apply {
                put(StickHubDbHelper.COL_FILE_PATH, finalFile.absolutePath)
                put(StickHubDbHelper.COL_TITLE, title.ifBlank { "Sticker #${now % 10000}" })
                put(StickHubDbHelper.COL_CATEGORY, resolvedCategory)
                put(StickHubDbHelper.COL_TAGS, tags)
                put(StickHubDbHelper.COL_IS_FAVORITE, 0)
                put(StickHubDbHelper.COL_CREATED_AT, now)
                put(StickHubDbHelper.COL_SORT_ORDER, now)
                put(StickHubDbHelper.COL_USAGE_COUNT, 0)
                put(StickHubDbHelper.COL_CONTENT_SHA256, contentHash)
            }

            val id = dbHelper.writableDatabase.insert(StickHubDbHelper.TABLE_STICKERS, null, values)
            if (id == -1L) {
                return StreamSaveResult.Failed("Couldn't save the clipboard sticker.")
            }
            committed = true

            if (refreshAfterSave) {
                try {
                    refresh()
                } catch (ce: CancellationException) {
                    throw ce
                } catch (_: Exception) {
                }
            }
            val saved = getStickerByIdInternal(id)
                ?: return StreamSaveResult.Failed("Sticker was saved but could not be read back.")
            return StreamSaveResult.Saved(saved)
        } catch (ce: CancellationException) {
            if (!committed) {
                tempFile.delete()
                if (finalFile.exists()) finalFile.delete()
            }
            throw ce
        } catch (error: Exception) {
            if (!committed) {
                tempFile.delete()
                if (finalFile.exists()) finalFile.delete()
            }
            error.printStackTrace()
            return StreamSaveResult.Failed(
                error.localizedMessage ?: "Couldn't save the clipboard sticker."
            )
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    private fun findExistingStickerByContentHash(contentHash: String): StickerItem? {
        findStickerByContentHash(contentHash)?.let { return it }
        backfillMissingContentHashes()
        return findStickerByContentHash(contentHash)
    }

    private fun findStickerByContentHash(contentHash: String): StickerItem? {
        val cursor = dbHelper.readableDatabase.query(
            StickHubDbHelper.TABLE_STICKERS,
            null,
            "${StickHubDbHelper.COL_CONTENT_SHA256} = ?",
            arrayOf(contentHash),
            null,
            null,
            null,
            "1"
        )
        cursor.use { rows ->
            return if (rows.moveToFirst()) cursorToSticker(rows) else null
        }
    }

    private fun backfillMissingContentHashes() {
        val db = dbHelper.writableDatabase
        val cursor = db.query(
            StickHubDbHelper.TABLE_STICKERS,
            arrayOf(StickHubDbHelper.COL_ID, StickHubDbHelper.COL_FILE_PATH),
            "${StickHubDbHelper.COL_CONTENT_SHA256} IS NULL OR ${StickHubDbHelper.COL_CONTENT_SHA256} = ''",
            null,
            null,
            null,
            null
        )
        cursor.use { rows ->
            while (rows.moveToNext()) {
                val id = rows.getLong(rows.getColumnIndexOrThrow(StickHubDbHelper.COL_ID))
                val file = File(rows.getString(rows.getColumnIndexOrThrow(StickHubDbHelper.COL_FILE_PATH)))
                if (!file.isFile) continue
                val contentHash = try {
                    file.inputStream().use(ClipboardContentHasher::sha256)
                } catch (_: Exception) {
                    continue
                }
                db.update(
                    StickHubDbHelper.TABLE_STICKERS,
                    ContentValues().apply {
                        put(StickHubDbHelper.COL_CONTENT_SHA256, contentHash)
                    },
                    "${StickHubDbHelper.COL_ID} = ?",
                    arrayOf(id.toString())
                )
            }
        }
    }

    private fun fileExtensionForMimeType(mimeType: String?): String {
        return StickerMimeTypes.extensionForMime(mimeType)
    }

    /**
     * Dedicated restore API that preserves exact metadata from backups.
     */
    suspend fun restoreSticker(
        sourceFile: File,
        title: String,
        category: String,
        tags: String,
        isFavorite: Boolean,
        createdAt: Long,
        usageCount: Int
    ): StickerItem? = withContext(Dispatchers.IO) {
        // Skip duplicates: re-importing the same backup must not clone rows.
        // Null means "skipped" to the caller (not counted as merged).
        val sourceHash = try {
            sourceFile.inputStream().use(ClipboardContentHasher::sha256)
        } catch (_: Exception) {
            null
        }
        if (sourceHash != null && findExistingStickerByContentHash(sourceHash) != null) {
            return@withContext null
        }

        // Preserve the source container: bytes are never transcoded here.
        val sourceExtension = sourceFile.extension.lowercase()
            .takeIf { StickerMimeTypes.isSupportedExtension(it) } ?: "png"
        val finalFileName = "sticker_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$sourceExtension"
        val finalFile = File(stickersDir, finalFileName)
        val resolvedCategory = resolveCategoryForSaveInternal(category)

        try {
            sourceFile.copyTo(finalFile, overwrite = true)
            val contentHash = finalFile.inputStream().use(ClipboardContentHasher::sha256)

            val cv = ContentValues().apply {
                put(StickHubDbHelper.COL_FILE_PATH, finalFile.absolutePath)
                put(StickHubDbHelper.COL_TITLE, title)
                put(StickHubDbHelper.COL_CATEGORY, resolvedCategory)
                put(StickHubDbHelper.COL_TAGS, tags)
                put(StickHubDbHelper.COL_IS_FAVORITE, if (isFavorite) 1 else 0)
                val stableCreatedAt = if (createdAt > 0) createdAt else System.currentTimeMillis()
                put(StickHubDbHelper.COL_CREATED_AT, stableCreatedAt)
                put(StickHubDbHelper.COL_SORT_ORDER, stableCreatedAt)
                put(StickHubDbHelper.COL_USAGE_COUNT, usageCount.coerceAtLeast(0))
                put(StickHubDbHelper.COL_CONTENT_SHA256, contentHash)
            }

            val db = dbHelper.writableDatabase
            val id = db.insert(StickHubDbHelper.TABLE_STICKERS, null, cv)
            if (id == -1L) {
                finalFile.delete()
                return@withContext null
            }

            // Return item representation
            StickerItem(
                id = id,
                filePath = finalFile.absolutePath,
                title = title,
                category = resolvedCategory,
                tags = tags,
                isFavorite = isFavorite,
                createdAt = if (createdAt > 0) createdAt else System.currentTimeMillis(),
                usageCount = usageCount.coerceAtLeast(0)
            )
        } catch (e: Exception) {
            finalFile.delete()
            e.printStackTrace()
            null
        }
    }

    /**
     * Validated bulk restore used exclusively by [com.hkm.stickhub.util.BackupHelper]
     * after an archive passed full validation. Multiset matching preserves
     * multiplicity: N identical archive rows against M identical library rows
     * import exactly max(0, N-M) copies and report the rest as already present.
     * Everything (new categories + new rows) commits in ONE transaction; created
     * files are removed again if the database part fails.
     */
    suspend fun restoreBackupPlan(
        categories: List<BackupRestoreCategory>,
        stickers: List<BackupRestoreSticker>
    ): BackupRestoreOutcome = backupMutex.withLock {
        withContext(Dispatchers.IO) {
            val createdFiles = mutableListOf<File>()
            try {
                // Snapshot current library once for matching.
                data class LibraryRow(
                    val hash: String,
                    val title: String,
                    val category: String,
                    val tags: String,
                    val isFavorite: Boolean,
                    val createdAt: Long,
                    val usageCount: Int,
                    var matched: Boolean = false
                )
                val libraryIndex = mutableMapOf<String, MutableList<LibraryRow>>()
                val cursor = dbHelper.readableDatabase.query(
                    StickHubDbHelper.TABLE_STICKERS,
                    arrayOf(
                        StickHubDbHelper.COL_CONTENT_SHA256,
                        StickHubDbHelper.COL_TITLE,
                        StickHubDbHelper.COL_CATEGORY,
                        StickHubDbHelper.COL_TAGS,
                        StickHubDbHelper.COL_IS_FAVORITE,
                        StickHubDbHelper.COL_CREATED_AT,
                        StickHubDbHelper.COL_USAGE_COUNT
                    ),
                    null, null, null, null, null
                )
                cursor.use { rows ->
                    while (rows.moveToNext()) {
                        val hash = rows.getString(0) ?: ""
                        if (hash.isBlank()) continue
                        libraryIndex.getOrPut(hash) { mutableListOf() }.add(
                            LibraryRow(
                                hash = hash,
                                title = rows.getString(1).orEmpty(),
                                category = rows.getString(2).orEmpty(),
                                tags = rows.getString(3).orEmpty(),
                                isFavorite = rows.getInt(4) == 1,
                                createdAt = rows.getLong(5),
                                usageCount = rows.getInt(6)
                            )
                        )
                    }
                }

                var alreadyPresent = 0
                val toImport = mutableListOf<BackupRestoreSticker>()
                for (row in stickers) {
                    val candidates = libraryIndex[row.contentHash].orEmpty().filter { !it.matched }
                    val full = candidates.firstOrNull {
                        it.title == row.title &&
                            it.category == row.category &&
                            it.tags == row.tags &&
                            it.isFavorite == row.isFavorite &&
                            it.createdAt == row.createdAt &&
                            it.usageCount == row.usageCount
                    }
                    val chosen = full ?: candidates.firstOrNull()
                    if (chosen != null) {
                        chosen.matched = true
                        alreadyPresent++
                    } else {
                        toImport.add(row)
                    }
                }

                val db = dbHelper.writableDatabase
                db.beginTransaction()
                try {
                    // New categories only; existing order/names are never touched.
                    var maxOrder = db.rawQuery(
                        "SELECT MAX(${StickHubDbHelper.COL_CAT_DISPLAY_ORDER}) FROM ${StickHubDbHelper.TABLE_CATEGORIES}",
                        null
                    ).use { if (it.moveToFirst() && !it.isNull(0)) it.getInt(0) else -1 }
                    for (cat in categories) {
                        val exists = db.rawQuery(
                            "SELECT 1 FROM ${StickHubDbHelper.TABLE_CATEGORIES} WHERE ${StickHubDbHelper.COL_CAT_NAME} = ? COLLATE NOCASE LIMIT 1",
                            arrayOf(cat.name)
                        ).use { it.moveToFirst() }
                        if (!exists) {
                            maxOrder++
                            val order = if (cat.displayOrder >= 0) cat.displayOrder else maxOrder
                            if (cat.displayOrder >= 0) maxOrder = maxOf(maxOrder, cat.displayOrder)
                            val cv = ContentValues().apply {
                                put(StickHubDbHelper.COL_CAT_NAME, cat.name)
                                put(
                                    StickHubDbHelper.COL_CAT_IS_DEFAULT,
                                    if (cat.isDefault || cat.name.equals(CategoryItem.FALLBACK_NAME, ignoreCase = true)) 1 else 0
                                )
                                put(StickHubDbHelper.COL_CAT_DISPLAY_ORDER, order)
                            }
                            db.insertOrThrow(StickHubDbHelper.TABLE_CATEGORIES, null, cv)
                        }
                    }
                    // Implicit homes for sticker rows whose category is absent.
                    val knownNames = mutableSetOf<String>()
                    db.rawQuery("SELECT ${StickHubDbHelper.COL_CAT_NAME} FROM ${StickHubDbHelper.TABLE_CATEGORIES}", null)
                        .use { rows ->
                            while (rows.moveToNext()) knownNames.add(rows.getString(0).lowercase())
                        }
                    for (row in toImport) {
                        if (row.category.lowercase() !in knownNames) {
                            maxOrder++
                            val cv = ContentValues().apply {
                                put(StickHubDbHelper.COL_CAT_NAME, row.category)
                                put(
                                    StickHubDbHelper.COL_CAT_IS_DEFAULT,
                                    if (row.category.equals(CategoryItem.FALLBACK_NAME, ignoreCase = true)) 1 else 0
                                )
                                put(StickHubDbHelper.COL_CAT_DISPLAY_ORDER, maxOrder)
                            }
                            db.insertOrThrow(StickHubDbHelper.TABLE_CATEGORIES, null, cv)
                            knownNames.add(row.category.lowercase())
                        }
                    }

                    for (row in toImport) {
                        val finalFile = File(
                            stickersDir,
                            "sticker_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.${row.format}"
                        )
                        row.stagedFile.copyTo(finalFile, overwrite = false)
                        createdFiles.add(finalFile)
                        val fileHash = finalFile.inputStream().use(ClipboardContentHasher::sha256)
                        if (!fileHash.equals(row.contentHash, ignoreCase = true)) {
                            throw IOException("Staged image changed during restore.")
                        }
                        val cv = ContentValues().apply {
                            put(StickHubDbHelper.COL_FILE_PATH, finalFile.absolutePath)
                            put(StickHubDbHelper.COL_TITLE, row.title)
                            put(StickHubDbHelper.COL_CATEGORY, row.category)
                            put(StickHubDbHelper.COL_TAGS, row.tags)
                            put(StickHubDbHelper.COL_IS_FAVORITE, if (row.isFavorite) 1 else 0)
                            val stableCreatedAt = if (row.createdAt > 0) row.createdAt else System.currentTimeMillis()
                            put(StickHubDbHelper.COL_CREATED_AT, stableCreatedAt)
                            put(StickHubDbHelper.COL_SORT_ORDER, row.sortOrder)
                            put(StickHubDbHelper.COL_USAGE_COUNT, row.usageCount.coerceAtLeast(0))
                            put(StickHubDbHelper.COL_CONTENT_SHA256, fileHash)
                        }
                        db.insertOrThrow(StickHubDbHelper.TABLE_STICKERS, null, cv)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }

                try {
                    refresh()
                } catch (ce: CancellationException) {
                    throw ce
                } catch (_: Exception) {
                }
                BackupRestoreOutcome(imported = toImport.size, alreadyPresent = alreadyPresent)
            } catch (ce: CancellationException) {
                createdFiles.forEach { try { it.delete() } catch (_: Exception) { } }
                throw ce
            } catch (e: Exception) {
                createdFiles.forEach { try { it.delete() } catch (_: Exception) { } }
                throw e
            }
        }
    }

    suspend fun toggleFavorite(stickerId: Long) = withContext(Dispatchers.IO) {
        // Single-statement flip: immune to double-tap races and works even
        // when the in-memory flow hasn't loaded yet (post-process-death).
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE ${StickHubDbHelper.TABLE_STICKERS} SET ${StickHubDbHelper.COL_IS_FAVORITE} = 1 - ${StickHubDbHelper.COL_IS_FAVORITE} WHERE ${StickHubDbHelper.COL_ID} = ?",
            arrayOf(stickerId.toString())
        )
        refresh()
    }

    suspend fun recordUsage(stickerId: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE ${StickHubDbHelper.TABLE_STICKERS} SET ${StickHubDbHelper.COL_USAGE_COUNT} = ${StickHubDbHelper.COL_USAGE_COUNT} + 1 WHERE ${StickHubDbHelper.COL_ID} = ?",
            arrayOf(stickerId.toString())
        )
        refresh()
    }

    suspend fun updateSticker(
        id: Long,
        title: String,
        category: String,
        tags: String
    ) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(StickHubDbHelper.COL_TITLE, title)
            put(StickHubDbHelper.COL_CATEGORY, resolveCategoryForSaveInternal(category))
            put(StickHubDbHelper.COL_TAGS, tags)
        }
        db.update(
            StickHubDbHelper.TABLE_STICKERS,
            cv,
            "${StickHubDbHelper.COL_ID} = ?",
            arrayOf(id.toString())
        )
        refresh()
    }

    /** Persists one complete manual library ordering in a single transaction. */
    suspend fun persistStickerOrder(orderedIds: List<Long>): Boolean = withContext(Dispatchers.IO) {
        val currentIds = _stickersFlow.value.map { it.id }
        if (!StickerOrderPolicy.isExactPermutation(currentIds, orderedIds)) return@withContext false

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues()
            orderedIds.forEachIndexed { index, id ->
                values.clear()
                values.put(StickHubDbHelper.COL_SORT_ORDER, (orderedIds.size - index).toLong())
                db.update(
                    StickHubDbHelper.TABLE_STICKERS,
                    values,
                    "${StickHubDbHelper.COL_ID} = ?",
                    arrayOf(id.toString())
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refresh()
        true
    }

    suspend fun deleteSticker(stickerId: Long) = withContext(Dispatchers.IO) {
        val doomedPath = _stickersFlow.value.find { it.id == stickerId }?.filePath
        val db = dbHelper.writableDatabase
        db.delete(
            StickHubDbHelper.TABLE_STICKERS,
            "${StickHubDbHelper.COL_ID} = ?",
            arrayOf(stickerId.toString())
        )
        // File goes only after the row is gone: a crash between the two
        // leaves an orphan file (harmless), never a row pointing at nothing.
        doomedPath?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) file.delete()
            } catch (_: Exception) {
            }
        }
        refresh()
    }

    suspend fun addCategory(name: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        val currentCategories = _categoriesFlow.value
        val validation = CategoryValidator.validate(trimmed, currentCategories)
        if (validation !is CategoryValidator.Result.Valid) {
            return@withContext false
        }
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery(
            "SELECT MAX(${StickHubDbHelper.COL_CAT_DISPLAY_ORDER}) FROM ${StickHubDbHelper.TABLE_CATEGORIES}",
            null
        )
        val maxOrder = cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
        val nextOrder = maxOf(maxOrder, currentCategories.size) + 1

        val cv = ContentValues().apply {
            put(StickHubDbHelper.COL_CAT_NAME, trimmed)
            put(StickHubDbHelper.COL_CAT_IS_DEFAULT, 0)
            put(StickHubDbHelper.COL_CAT_DISPLAY_ORDER, nextOrder)
        }
        val id = db.insertWithOnConflict(
            StickHubDbHelper.TABLE_CATEGORIES,
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
        )
        refresh()
        id != -1L
    }

    suspend fun renameCategory(oldName: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        val trimmedOld = oldName.trim()
        val trimmedNew = newName.trim()
        // Every category is renamable, including General.
        val currentCategories = _categoriesFlow.value
        val validation = CategoryValidator.validate(trimmedNew, currentCategories, currentName = trimmedOld)
        if (validation !is CategoryValidator.Result.Valid) {
            return@withContext false
        }

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val catCv = ContentValues().apply {
                put(StickHubDbHelper.COL_CAT_NAME, trimmedNew)
            }
            db.update(
                StickHubDbHelper.TABLE_CATEGORIES,
                catCv,
                "${StickHubDbHelper.COL_CAT_NAME} = ?",
                arrayOf(trimmedOld)
            )

            val stickCv = ContentValues().apply {
                put(StickHubDbHelper.COL_CATEGORY, trimmedNew)
            }
            db.update(
                StickHubDbHelper.TABLE_STICKERS,
                stickCv,
                "${StickHubDbHelper.COL_CATEGORY} = ?",
                arrayOf(trimmedOld)
            )

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refresh()
        true
    }

    /**
     * Fallback home for stickers orphaned by a category delete: "General"
     * when it survives, otherwise the first remaining category by display
     * order. Never null — the library always keeps at least one category.
     */
    fun resolveDeleteFallback(excludeName: String): String {
        return CategoryItem.pickDeleteFallback(_categoriesFlow.value, excludeName)
    }

    suspend fun deleteCategory(name: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return@withContext false
        val category = _categoriesFlow.value.find { it.name.equals(trimmed, ignoreCase = true) }
            ?: return@withContext false

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Move stickers to the fallback home first.
            val fallback = resolveDeleteFallback(category.name)
            val updateCv = ContentValues().apply {
                put(StickHubDbHelper.COL_CATEGORY, fallback)
            }
            db.update(
                StickHubDbHelper.TABLE_STICKERS,
                updateCv,
                "${StickHubDbHelper.COL_CATEGORY} = ?",
                arrayOf(category.name)
            )

            // Delete the category — every category is deletable now.
            db.delete(
                StickHubDbHelper.TABLE_CATEGORIES,
                "${StickHubDbHelper.COL_CAT_NAME} = ?",
                arrayOf(category.name)
            )

            // Invariant: never leave the library category-less. Recreate a
            // fresh empty General when the user deleted the very last one.
            val remaining = db.rawQuery(
                "SELECT COUNT(*) FROM ${StickHubDbHelper.TABLE_CATEGORIES}", null
            ).use { if (it.moveToFirst()) it.getInt(0) else 0 }
            if (remaining == 0) {
                val cv = ContentValues().apply {
                    put(StickHubDbHelper.COL_CAT_NAME, "General")
                    put(StickHubDbHelper.COL_CAT_IS_DEFAULT, 1)
                    put(StickHubDbHelper.COL_CAT_DISPLAY_ORDER, 0)
                }
                db.insertWithOnConflict(
                    StickHubDbHelper.TABLE_CATEGORIES,
                    null,
                    cv,
                    android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refresh()
        true
    }

    suspend fun reorderCategories(orderedNames: List<String>): Boolean = withContext(Dispatchers.IO) {
        if (orderedNames.isEmpty()) return@withContext false
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Every category participates in ordering now (General included).
            var orderIndex = 0
            for (name in orderedNames) {
                val cv = ContentValues().apply {
                    put(StickHubDbHelper.COL_CAT_DISPLAY_ORDER, orderIndex++)
                }
                db.update(
                    StickHubDbHelper.TABLE_CATEGORIES,
                    cv,
                    "${StickHubDbHelper.COL_CAT_NAME} = ?",
                    arrayOf(name)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refresh()
        true
    }

    suspend fun batchDelete(ids: List<Long>): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        var count = 0
        // Snapshot file paths up-front: files are deleted only AFTER the DB
        // transaction commits, so a rollback can never orphan rows that point
        // at already-deleted files.
        val doomedFiles = ids.mapNotNull { id ->
            _stickersFlow.value.find { it.id == id }?.filePath
        }
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (id in ids) {
                count += db.delete(
                    StickHubDbHelper.TABLE_STICKERS,
                    "${StickHubDbHelper.COL_ID} = ?",
                    arrayOf(id.toString())
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        doomedFiles.forEach { path ->
            try {
                val file = File(path)
                if (file.exists()) file.delete()
            } catch (_: Exception) {
            }
        }
        refresh()
        count
    }

    suspend fun batchSetCategory(ids: List<Long>, newCategory: String): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        var count = 0
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put(StickHubDbHelper.COL_CATEGORY, newCategory)
            }
            for (id in ids) {
                count += db.update(
                    StickHubDbHelper.TABLE_STICKERS,
                    cv,
                    "${StickHubDbHelper.COL_ID} = ?",
                    arrayOf(id.toString())
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refresh()
        count
    }

    suspend fun batchToggleFavorite(ids: List<Long>, isFavorite: Boolean): Int = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext 0
        var count = 0
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put(StickHubDbHelper.COL_IS_FAVORITE, if (isFavorite) 1 else 0)
            }
            for (id in ids) {
                count += db.update(
                    StickHubDbHelper.TABLE_STICKERS,
                    cv,
                    "${StickHubDbHelper.COL_ID} = ?",
                    arrayOf(id.toString())
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        refresh()
        count
    }

    fun getStorageSize(): Long {
        var total = 0L
        stickersDir.listFiles()?.forEach {
            if (it.isFile) total += it.length()
        }
        return total
    }

    private fun cursorToSticker(c: Cursor): StickerItem {
        return StickerItem(
            id = c.getLong(c.getColumnIndexOrThrow(StickHubDbHelper.COL_ID)),
            filePath = c.getString(c.getColumnIndexOrThrow(StickHubDbHelper.COL_FILE_PATH)),
            title = c.getString(c.getColumnIndexOrThrow(StickHubDbHelper.COL_TITLE)) ?: "",
            category = c.getString(c.getColumnIndexOrThrow(StickHubDbHelper.COL_CATEGORY)) ?: "General",
            tags = c.getString(c.getColumnIndexOrThrow(StickHubDbHelper.COL_TAGS)) ?: "",
            isFavorite = c.getInt(c.getColumnIndexOrThrow(StickHubDbHelper.COL_IS_FAVORITE)) == 1,
            createdAt = c.getLong(c.getColumnIndexOrThrow(StickHubDbHelper.COL_CREATED_AT)),
            usageCount = c.getInt(c.getColumnIndexOrThrow(StickHubDbHelper.COL_USAGE_COUNT))
        )
    }
}
