package com.hkm.stickhub.util

import android.content.Context
import android.net.Uri
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupHelper {

    private const val MAX_ZIP_ENTRIES = 500
    private const val MAX_ENTRY_SIZE_BYTES = 20L * 1024 * 1024 // 20 MB per image
    private const val MAX_TOTAL_SIZE_BYTES = 100L * 1024 * 1024 // 100 MB total uncompressed

    suspend fun exportBackup(
        context: Context,
        outputUri: Uri,
        stickers: List<StickerItem>,
        categories: List<CategoryItem>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val outputStream = context.contentResolver.openOutputStream(outputUri) ?: return@withContext false
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                // 1. Write metadata.json
                val rootJson = JSONObject()
                rootJson.put("version", 2)
                rootJson.put("exportedAt", System.currentTimeMillis())

                val catArray = JSONArray()
                categories.forEach { cat ->
                    val catObj = JSONObject().apply {
                        put("name", cat.name)
                        put("isDefault", cat.isDefault)
                        put("displayOrder", cat.displayOrder)
                    }
                    catArray.put(catObj)
                }
                rootJson.put("categories", catArray)

                val stickersArray = JSONArray()
                stickers.forEach { s ->
                    val file = File(s.filePath)
                    if (file.exists() && file.isFile) {
                        val safeName = file.name
                        val zipEntryName = "stickers/$safeName"
                        val sObj = JSONObject().apply {
                            put("title", s.title)
                            put("category", s.category)
                            put("tags", s.tags)
                            put("isFavorite", s.isFavorite)
                            put("createdAt", s.createdAt)
                            put("usageCount", s.usageCount)
                            put("fileName", safeName)
                        }
                        stickersArray.put(sObj)

                        // Add image file to zip
                        zipOut.putNextEntry(ZipEntry(zipEntryName))
                        FileInputStream(file).use { fileIn ->
                            fileIn.copyTo(zipOut, 8192)
                        }
                        zipOut.closeEntry()
                    }
                }
                rootJson.put("stickers", stickersArray)

                // Add metadata.json entry
                zipOut.putNextEntry(ZipEntry("metadata.json"))
                zipOut.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importBackup(
        context: Context,
        inputUri: Uri,
        repository: StickerRepository
    ): Int = withContext(Dispatchers.IO) {
        var importedCount = 0
        val stagingDir = File(context.cacheDir, "backup_staging_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}")
        val canonicalStagingPath = stagingDir.canonicalPath

        try {
            if (!stagingDir.exists()) stagingDir.mkdirs()
            val inputStream = context.contentResolver.openInputStream(inputUri) ?: return@withContext 0

            var metadataJsonStr: String? = null
            val extractedFiles = mutableMapOf<String, File>()

            var totalEntries = 0
            var totalExtractedBytes = 0L

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    totalEntries++
                    if (totalEntries > MAX_ZIP_ENTRIES) {
                        throw SecurityException("Zip file contains too many entries (max $MAX_ZIP_ENTRIES)")
                    }

                    val entryName = entry.name
                    // Check for path traversal in entry name
                    if (entryName.contains("..") || entryName.startsWith("/") || entryName.startsWith("\\")) {
                        throw SecurityException("Malicious zip entry path: $entryName")
                    }

                    if (entryName == "metadata.json") {
                        val metaFile = File(stagingDir, "metadata.json")
                        val canonicalFile = metaFile.canonicalPath
                        if (!canonicalFile.startsWith(canonicalStagingPath)) {
                            throw SecurityException("Path traversal attempt in zip: $entryName")
                        }
                        metadataJsonStr = zipIn.bufferedReader(Charsets.UTF_8).readText()
                    } else if (entryName.startsWith("stickers/") && !entry.isDirectory) {
                        val simpleName = entryName.removePrefix("stickers/")
                        if (!isValidBasename(simpleName)) {
                            throw SecurityException("Invalid sticker filename in zip: $simpleName")
                        }

                        val targetFile = File(stagingDir, simpleName)
                        val canonicalTarget = targetFile.canonicalPath
                        if (!canonicalTarget.startsWith(canonicalStagingPath)) {
                            throw SecurityException("Zip entry targets outside staging directory: $entryName")
                        }

                        FileOutputStream(targetFile).use { out ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            var entryBytes = 0L
                            while (zipIn.read(buffer).also { bytesRead = it } != -1) {
                                entryBytes += bytesRead
                                totalExtractedBytes += bytesRead
                                if (entryBytes > MAX_ENTRY_SIZE_BYTES) {
                                    throw SecurityException("Zip entry exceeds max size limit")
                                }
                                if (totalExtractedBytes > MAX_TOTAL_SIZE_BYTES) {
                                    throw SecurityException("Zip archive total uncompressed size exceeds limit")
                                }
                                out.write(buffer, 0, bytesRead)
                            }
                            out.flush()
                        }
                        extractedFiles[simpleName] = targetFile
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            if (metadataJsonStr == null) {
                throw IOException("Missing metadata.json in backup archive")
            }

            val rootJson = JSONObject(metadataJsonStr!!)
            val version = rootJson.optInt("version", 1)
            if (version < 1 || version > 3) {
                throw IOException("Unsupported backup archive version: $version")
            }

            // 1. Import categories preserving order
            val catArray = rootJson.optJSONArray("categories")
            if (catArray != null) {
                val categoryNamesToOrder = mutableListOf<String>()
                for (i in 0 until catArray.length()) {
                    val catObj = catArray.getJSONObject(i)
                    val name = catObj.optString("name", "").trim()
                    if (name.isNotBlank()) {
                        repository.addCategory(name)
                        categoryNamesToOrder.add(name)
                    }
                }
                if (categoryNamesToOrder.isNotEmpty()) {
                    repository.reorderCategories(categoryNamesToOrder)
                }
            }

            // 2. Import stickers preserving full metadata
            val stickArray = rootJson.optJSONArray("stickers")
            if (stickArray != null) {
                for (i in 0 until stickArray.length()) {
                    val sObj = stickArray.getJSONObject(i)
                    val fileName = sObj.optString("fileName", "")
                    val file = extractedFiles[fileName]
                    if (file != null && file.exists() && file.length() > 0) {
                        val title = sObj.optString("title", "")
                        val category = sObj.optString("category", "General").ifBlank { "General" }
                        val tags = sObj.optString("tags", "")
                        val isFavorite = sObj.optBoolean("isFavorite", false)
                        val createdAt = sObj.optLong("createdAt", System.currentTimeMillis())
                        val usageCount = sObj.optInt("usageCount", 0)

                        val restored = repository.restoreSticker(
                            sourceFile = file,
                            title = title,
                            category = category,
                            tags = tags,
                            isFavorite = isFavorite,
                            createdAt = createdAt,
                            usageCount = usageCount
                        )
                        if (restored != null) {
                            importedCount++
                        }
                    }
                }
            }

            repository.refresh()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Guarantee cleanup of staging dir
            stagingDir.deleteRecursively()
        }
        return@withContext importedCount
    }

    fun isSafeZipPath(entryName: String): Boolean {
        if (entryName.isBlank()) return false
        if (entryName.contains("..") || entryName.startsWith("/") || entryName.startsWith("\\")) {
            return false
        }
        return true
    }

    fun isValidBasename(name: String): Boolean {
        if (name.isBlank() || name.length > 120) return false
        if (name.contains("/") || name.contains("\\") || name.contains("..")) return false
        // Allow standard image filenames: a-zA-Z0-9._-
        return name.matches(Regex("^[a-zA-Z0-9._-]+$"))
    }
}
