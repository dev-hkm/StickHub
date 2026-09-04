package com.hkm.stickhub.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import com.hkm.stickhub.data.db.StickHubDbHelper
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.CategoryValidator
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.util.ClipboardContentHasher
import com.hkm.stickhub.util.ClipboardImportPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class StickerRepository(private val context: Context) {

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

    suspend fun refresh() = withContext(Dispatchers.IO) {
        _stickersFlow.value = getAllStickersInternal()
        _categoriesFlow.value = getAllCategoriesInternal()
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

    suspend fun saveStickerBitmap(
        bitmap: Bitmap,
        title: String = "",
        category: String = "General",
        tags: String = ""
    ): StickerItem? = withContext(Dispatchers.IO) {
        val tempFile = File(stickersDir, "tmp_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")
        val finalFile = File(stickersDir, "sticker_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")

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

            val cv = ContentValues().apply {
                put(StickHubDbHelper.COL_FILE_PATH, finalFile.absolutePath)
                put(StickHubDbHelper.COL_TITLE, title.ifBlank { "Sticker #${System.currentTimeMillis() % 10000}" })
                put(StickHubDbHelper.COL_CATEGORY, category.ifBlank { "General" })
                put(StickHubDbHelper.COL_TAGS, tags)
                put(StickHubDbHelper.COL_IS_FAVORITE, 0)
                put(StickHubDbHelper.COL_CREATED_AT, System.currentTimeMillis())
                put(StickHubDbHelper.COL_SORT_ORDER, System.currentTimeMillis())
                put(StickHubDbHelper.COL_USAGE_COUNT, 0)
                put(StickHubDbHelper.COL_CONTENT_SHA256, contentHash)
            }

            val db = dbHelper.writableDatabase
            val id = db.insert(StickHubDbHelper.TABLE_STICKERS, null, cv)
            if (id == -1L) {
                finalFile.delete()
                return@withContext null
            }

            refresh()
            _stickersFlow.value.find { it.id == id }
        } catch (e: Exception) {
            tempFile.delete()
            finalFile.delete()
            e.printStackTrace()
            null
        }
    }

    suspend fun overwriteStickerBitmap(id: Long, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val sticker = _stickersFlow.value.find { it.id == id } ?: return@withContext false
        val originalFile = File(sticker.filePath)
        val tempFile = File(stickersDir, "tmp_ow_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")

        try {
            FileOutputStream(tempFile).use { out ->
                val ok = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                if (!ok) {
                    tempFile.delete()
                    return@withContext false
                }
                out.flush()
            }

            if (!tempFile.renameTo(originalFile)) {
                tempFile.copyTo(originalFile, overwrite = true)
                tempFile.delete()
            }

            val contentHash = originalFile.inputStream().use(ClipboardContentHasher::sha256)
            dbHelper.writableDatabase.update(
                StickHubDbHelper.TABLE_STICKERS,
                ContentValues().apply {
                    put(StickHubDbHelper.COL_CONTENT_SHA256, contentHash)
                },
                "${StickHubDbHelper.COL_ID} = ?",
                arrayOf(id.toString())
            )

            refresh()
            true
        } catch (e: Exception) {
            tempFile.delete()
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

    private suspend fun saveStickerFromStreamInternal(
        inputStream: InputStream,
        title: String,
        category: String,
        tags: String,
        fileExtension: String,
        rejectDuplicates: Boolean
    ): StreamSaveResult {
        val safeExtension = fileExtension.lowercase().removePrefix(".").ifBlank { "png" }
        val uniquePart = "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val tempFile = File(stickersDir, "tmp_in_$uniquePart.$safeExtension")
        val finalFile = File(stickersDir, "sticker_$uniquePart.$safeExtension")

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

            val values = ContentValues().apply {
                put(StickHubDbHelper.COL_FILE_PATH, finalFile.absolutePath)
                put(StickHubDbHelper.COL_TITLE, title.ifBlank { "Sticker #${System.currentTimeMillis() % 10000}" })
                put(StickHubDbHelper.COL_CATEGORY, category.ifBlank { "General" })
                put(StickHubDbHelper.COL_TAGS, tags)
                put(StickHubDbHelper.COL_IS_FAVORITE, 0)
                put(StickHubDbHelper.COL_CREATED_AT, System.currentTimeMillis())
                put(StickHubDbHelper.COL_SORT_ORDER, System.currentTimeMillis())
                put(StickHubDbHelper.COL_USAGE_COUNT, 0)
                put(StickHubDbHelper.COL_CONTENT_SHA256, contentHash)
            }

            val id = dbHelper.writableDatabase.insert(StickHubDbHelper.TABLE_STICKERS, null, values)
            if (id == -1L) {
                finalFile.delete()
                return StreamSaveResult.Failed("Couldn't save the clipboard sticker.")
            }

            refresh()
            val saved = _stickersFlow.value.find { it.id == id }
                ?: return StreamSaveResult.Failed("Sticker was saved but could not be read back.")
            return StreamSaveResult.Saved(saved)
        } catch (error: Exception) {
            if (finalFile.exists()) finalFile.delete()
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
        return when (mimeType?.lowercase()) {
            "image/webp" -> "webp"
            "image/jpeg", "image/jpg" -> "jpg"
            "image/gif" -> "gif"
            "image/heic", "image/heif" -> "heic"
            else -> "png"
        }
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

        val finalFileName = "sticker_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png"
        val finalFile = File(stickersDir, finalFileName)

        try {
            sourceFile.copyTo(finalFile, overwrite = true)
            val contentHash = finalFile.inputStream().use(ClipboardContentHasher::sha256)

            val cv = ContentValues().apply {
                put(StickHubDbHelper.COL_FILE_PATH, finalFile.absolutePath)
                put(StickHubDbHelper.COL_TITLE, title)
                put(StickHubDbHelper.COL_CATEGORY, category.ifBlank { "General" })
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
                category = category.ifBlank { "General" },
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
            put(StickHubDbHelper.COL_CATEGORY, category)
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
        if (trimmedOld.equals("General", ignoreCase = true)) {
            return@withContext false
        }
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

    suspend fun deleteCategory(name: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        if (trimmed.equals("General", ignoreCase = true)) {
            return@withContext false
        }
        val category = _categoriesFlow.value.find { it.name.equals(trimmed, ignoreCase = true) }
        if (category == null || category.isDefault) return@withContext false

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Move stickers to General in transaction first
            val updateCv = ContentValues().apply {
                put(StickHubDbHelper.COL_CATEGORY, "General")
            }
            db.update(
                StickHubDbHelper.TABLE_STICKERS,
                updateCv,
                "${StickHubDbHelper.COL_CATEGORY} = ?",
                arrayOf(category.name)
            )

            // Delete the custom category
            db.delete(
                StickHubDbHelper.TABLE_CATEGORIES,
                "${StickHubDbHelper.COL_CAT_NAME} = ? AND ${StickHubDbHelper.COL_CAT_IS_DEFAULT} = 0",
                arrayOf(category.name)
            )
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
            var orderIndex = 1
            for (name in orderedNames) {
                if (name.equals("General", ignoreCase = true)) continue
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
