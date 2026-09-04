package com.hkm.stickhub.util

import android.content.Context
import android.net.Uri
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.data.repository.StickerRepository.BackupRestoreCategory
import com.hkm.stickhub.data.repository.StickerRepository.BackupRestoreSticker
import kotlinx.coroutines.CancellationException
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

/**
 * Structured backup import outcome. `Success(0, n)` means the archive was
 * valid but everything was already present — not an error.
 */
sealed interface BackupImportResult {
    data class Success(val imported: Int, val alreadyPresent: Int) : BackupImportResult
    data class Invalid(val reason: String) : BackupImportResult
    data class Failed(val reason: String) : BackupImportResult
}

object BackupHelper {

    const val BACKUP_FORMAT_VERSION = 3
    const val MAX_METADATA_BYTES = 1_048_576 // 1 MiB hard cap (untrusted input)
    const val MAX_STICKERS = 10_000
    const val MAX_IMAGE_BYTES = 32L * 1024 * 1024 // 32 MiB per image
    const val MAX_TOTAL_BYTES = 1024L * 1024 * 1024 // 1 GiB total uncompressed
    const val MAX_ZIP_ENTRIES = 10_010 // stickers + metadata + small headroom

    private const val METADATA_NAME = "metadata.json"
    private const val IMAGE_PREFIX = "stickers/"

    suspend fun exportBackup(
        context: Context,
        outputUri: Uri,
        stickers: List<StickerItem>,
        categories: List<CategoryItem>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (stickers.size > MAX_STICKERS) return@withContext false

            // 1. Preflight: every source file must exist. A missing file fails
            // the export loudly instead of shipping a silently short backup.
            // Hashes are computed here (single read pass per file).
            data class ExportRow(val sticker: StickerItem, val file: File, val hash: String, val format: String)
            val rows = mutableListOf<ExportRow>()
            for (sticker in stickers) {
                val file = File(sticker.filePath)
                if (!file.isFile || !file.canRead()) return@withContext false
                if (file.length() > MAX_IMAGE_BYTES) return@withContext false
                val ext = file.extension.lowercase().ifBlank { "png" }
                val format = when {
                    StickerMimeTypes.isSupportedExtension(ext) -> ext
                    else -> null
                } ?: return@withContext false
                val hash = try {
                    file.inputStream().use(ClipboardContentHasher::sha256)
                } catch (_: Exception) {
                    return@withContext false
                }
                rows.add(ExportRow(sticker, file, hash, format))
            }

            // 2. Unique entry names (two stickers can share a basename).
            val usedNames = mutableSetOf<String>()
            fun uniqueEntryName(base: String): String {
                var candidate = base
                var n = 2
                while (!usedNames.add(candidate)) {
                    val dot = base.lastIndexOf('.')
                    candidate = if (dot > 0) {
                        base.substring(0, dot) + "_$n" + base.substring(dot)
                    } else {
                        "${base}_$n"
                    }
                    n++
                }
                return candidate
            }

            val stickersArray = JSONArray()
            val stagedEntries = mutableListOf<Triple<String, File, String>>()
            rows.forEachIndexed { index, row ->
                val entryName = uniqueEntryName(row.file.name)
                stagedEntries.add(Triple(entryName, row.file, row.hash))
                stickersArray.put(JSONObject().apply {
                    put("fileName", entryName)
                    put("format", row.format)
                    put("sha256", row.hash)
                    put("title", row.sticker.title)
                    put("category", row.sticker.category)
                    put("tags", row.sticker.tags)
                    put("isFavorite", row.sticker.isFavorite)
                    put("createdAt", row.sticker.createdAt)
                    put("usageCount", row.sticker.usageCount)
                    put("order", index)
                })
            }

            val catArray = JSONArray()
            categories.forEach { cat ->
                catArray.put(JSONObject().apply {
                    put("name", cat.name)
                    put("isDefault", cat.isDefault)
                    put("displayOrder", cat.displayOrder)
                })
            }

            val rootJson = JSONObject()
            rootJson.put("version", BACKUP_FORMAT_VERSION)
            rootJson.put("exportedAt", System.currentTimeMillis())
            rootJson.put("categories", catArray)
            rootJson.put("stickers", stickersArray)
            val metadataBytes = rootJson.toString().toByteArray(Charsets.UTF_8)
            // Oversized metadata fails loudly at export, never ships truncated.
            if (metadataBytes.size > MAX_METADATA_BYTES) return@withContext false

            // 3. Stage the whole archive to a temp file first; the destination
            // is only touched once a complete, valid archive exists.
            val stagingFile = File(context.cacheDir, "export_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.stickhub")
            try {
                ZipOutputStream(BufferedOutputStream(FileOutputStream(stagingFile))).use { zipOut ->
                    for ((entryName, file, _) in stagedEntries) {
                        zipOut.putNextEntry(ZipEntry("stickers/$entryName"))
                        FileInputStream(file).use { fileIn ->
                            fileIn.copyTo(zipOut, 8192)
                        }
                        zipOut.closeEntry()
                    }
                    zipOut.putNextEntry(ZipEntry(METADATA_NAME))
                    zipOut.write(metadataBytes)
                    zipOut.closeEntry()
                }

                val outputStream = try {
                    context.contentResolver.openOutputStream(outputUri, "wt")
                        ?: context.contentResolver.openOutputStream(outputUri)
                } catch (_: Exception) {
                    null
                } ?: return@withContext false
                try {
                    outputStream.use { out ->
                        FileInputStream(stagingFile).use { staged ->
                            staged.copyTo(out, 8192)
                        }
                        out.flush()
                    }
                } catch (_: Exception) {
                    return@withContext false
                }
            } finally {
                stagingFile.delete()
            }
            true
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Legacy integer wrapper: number of newly imported stickers, 0 otherwise. */
    suspend fun importBackup(
        context: Context,
        inputUri: Uri,
        repository: StickerRepository
    ): Int = withContext(Dispatchers.IO) {
        return@withContext when (val result = importBackupDetailed(context, inputUri, repository)) {
            is BackupImportResult.Success -> result.imported
            else -> 0
        }
    }

    suspend fun importBackupDetailed(
        context: Context,
        inputUri: Uri,
        repository: StickerRepository
    ): BackupImportResult = withContext(Dispatchers.IO) {
        val stagingDir = File(context.cacheDir, "backup_staging_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}")
        try {
            if (!stagingDir.exists()) stagingDir.mkdirs()
            val canonicalStagingPath = stagingDir.canonicalPath
            val inputStream = try {
                context.contentResolver.openInputStream(inputUri)
            } catch (_: Exception) {
                null
            } ?: return@withContext BackupImportResult.Failed("Couldn't open the backup file.")

            // ---- Stage 1: extract with budgets, no mutation yet ----
            var metadataBytes: ByteArray? = null
            var metadataSeen = false
            val extractedFiles = mutableMapOf<String, File>()
            var totalEntries = 0
            var totalBytes = 0L

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    val current = entry
                    if (!current.isDirectory) {
                        totalEntries++
                        if (totalEntries > MAX_ZIP_ENTRIES) {
                            return@withContext BackupImportResult.Invalid("Archive has too many entries.")
                        }
                        val entryName = current.name
                        if (!isSafeZipPath(entryName)) {
                            return@withContext BackupImportResult.Invalid("Unsafe entry path in archive.")
                        }
                        if (entryName == METADATA_NAME) {
                            if (metadataSeen) {
                                return@withContext BackupImportResult.Invalid("Duplicate metadata in archive.")
                            }
                            metadataSeen = true
                            metadataBytes = readCapped(zipIn, MAX_METADATA_BYTES + 1)
                                ?: return@withContext BackupImportResult.Invalid("Couldn't read archive metadata.")
                            if (metadataBytes!!.size > MAX_METADATA_BYTES) {
                                return@withContext BackupImportResult.Invalid("Archive metadata is too large.")
                            }
                            totalBytes += metadataBytes!!.size
                        } else if (entryName.startsWith(IMAGE_PREFIX)) {
                            val simpleName = entryName.removePrefix(IMAGE_PREFIX)
                            if (!isValidBasename(simpleName)) {
                                return@withContext BackupImportResult.Invalid("Invalid image name in archive: $simpleName.")
                            }
                            if (extractedFiles.containsKey(simpleName)) {
                                return@withContext BackupImportResult.Invalid("Duplicate image entry in archive: $simpleName.")
                            }
                            val targetFile = File(stagingDir, simpleName)
                            if (!targetFile.canonicalPath.startsWith(canonicalStagingPath + File.separator)) {
                                return@withContext BackupImportResult.Invalid("Entry escapes the archive staging area.")
                            }
                            val written = copyCapped(zipIn, targetFile, MAX_IMAGE_BYTES)
                            if (written < 0) {
                                return@withContext BackupImportResult.Invalid("Image entry is too large: $simpleName.")
                            }
                            totalBytes += written
                            if (totalBytes > MAX_TOTAL_BYTES) {
                                return@withContext BackupImportResult.Invalid("Archive is too large.")
                            }
                            extractedFiles[simpleName] = targetFile
                        } else {
                            return@withContext BackupImportResult.Invalid("Unknown entry in archive: $entryName.")
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            val metaBytes = metadataBytes
                ?: return@withContext BackupImportResult.Invalid("Archive is missing its metadata.")

            // ---- Stage 2: parse + fully validate the manifest ----
            val rootJson = try {
                JSONObject(metaBytes.toString(Charsets.UTF_8))
            } catch (_: Exception) {
                return@withContext BackupImportResult.Invalid("Archive metadata is corrupt.")
            }
            val version = rootJson.optInt("version", 1)
            if (version < 1 || version > BACKUP_FORMAT_VERSION) {
                return@withContext BackupImportResult.Invalid("Unsupported archive version: $version.")
            }
            val stickArray = rootJson.optJSONArray("stickers")
                ?: return@withContext BackupImportResult.Invalid("Archive manifest has no stickers.")
            if (stickArray.length() > MAX_STICKERS) {
                return@withContext BackupImportResult.Invalid("Archive has too many stickers.")
            }

            data class ValidatedRow(
                val fileName: String,
                val format: String,
                val expectedHash: String?,
                val title: String,
                val category: String,
                val tags: String,
                val isFavorite: Boolean,
                val createdAt: Long,
                val usageCount: Int,
                val stagedFile: File,
                val contentHash: String
            )
            val rows = mutableListOf<ValidatedRow>()
            for (i in 0 until stickArray.length()) {
                val sObj = try {
                    stickArray.getJSONObject(i)
                } catch (_: Exception) {
                    return@withContext BackupImportResult.Invalid("Archive manifest entry #$i is corrupt.")
                }
                val fileName = sObj.optString("fileName", "")
                if (!isValidBasename(fileName)) {
                    return@withContext BackupImportResult.Invalid("Archive references an invalid file name.")
                }
                val stagedFile = extractedFiles[fileName]
                if (stagedFile == null || !stagedFile.isFile || stagedFile.length() <= 0L) {
                    return@withContext BackupImportResult.Invalid("Archive is missing image data for an entry.")
                }
                val contentHash = try {
                    stagedFile.inputStream().use(ClipboardContentHasher::sha256)
                } catch (_: Exception) {
                    return@withContext BackupImportResult.Invalid("Couldn't verify an archived image.")
                }
                if (version >= 3) {
                    val format = sObj.optString("format", "").lowercase()
                    if (!StickerMimeTypes.isSupportedExtension(format)) {
                        return@withContext BackupImportResult.Invalid("Archive entry has an unsupported image format.")
                    }
                    val sniffed = readImageFormat(stagedFile)
                    if (sniffed != null && !sameImageFormat(sniffed, format)) {
                        return@withContext BackupImportResult.Invalid("An archived image does not match its declared format.")
                    }
                    val expectedHash = sObj.optString("sha256", "")
                    if (expectedHash.isBlank() || !expectedHash.equals(contentHash, ignoreCase = true)) {
                        return@withContext BackupImportResult.Invalid("An archived image failed its integrity check.")
                    }
                }
                rows.add(
                    ValidatedRow(
                        fileName = fileName,
                        format = if (version >= 3) sObj.optString("format", "png").lowercase() else fileName.substringAfterLast('.', "png").lowercase(),
                        expectedHash = sObj.optString("sha256", "").ifBlank { null },
                        title = sObj.optString("title", ""),
                        category = sObj.optString("category", "General").ifBlank { "General" },
                        tags = sObj.optString("tags", ""),
                        isFavorite = sObj.optBoolean("isFavorite", false),
                        createdAt = sObj.optLong("createdAt", System.currentTimeMillis()),
                        usageCount = sObj.optInt("usageCount", 0).coerceAtLeast(0),
                        stagedFile = stagedFile,
                        contentHash = contentHash
                    )
                )
            }

            val catArray = rootJson.optJSONArray("categories")
            data class ValidatedCategory(val name: String, val isDefault: Boolean, val displayOrder: Int)
            val archiveCategories = mutableListOf<ValidatedCategory>()
            if (catArray != null) {
                val seenNames = mutableSetOf<String>()
                for (i in 0 until catArray.length()) {
                    val catObj = try {
                        catArray.getJSONObject(i)
                    } catch (_: Exception) {
                        return@withContext BackupImportResult.Invalid("Archive category entry #$i is corrupt.")
                    }
                    val name = catObj.optString("name", "").trim()
                    if (name.isBlank() || name.length > 64) {
                        return@withContext BackupImportResult.Invalid("Archive has an invalid category name.")
                    }
                    val lower = name.lowercase()
                    if (!seenNames.add(lower)) {
                        return@withContext BackupImportResult.Invalid("Archive lists a category twice: $name.")
                    }
                    archiveCategories.add(
                        ValidatedCategory(
                            name = name,
                            isDefault = catObj.optBoolean("isDefault", false),
                            displayOrder = catObj.optInt("displayOrder", -1)
                        )
                    )
                }
            }

            // ---- Stage 3: single atomic restore (rows keep archive order) ----
            val outcome = repository.restoreBackupPlan(
                categories = archiveCategories.map {
                    BackupRestoreCategory(name = it.name, isDefault = it.isDefault, displayOrder = it.displayOrder)
                },
                stickers = rows.mapIndexed { index, row ->
                    BackupRestoreSticker(
                        title = row.title,
                        category = row.category,
                        tags = row.tags,
                        isFavorite = row.isFavorite,
                        createdAt = row.createdAt,
                        usageCount = row.usageCount,
                        // Archive order, appended after existing library content.
                        sortOrder = -(index + 1).toLong(),
                        stagedFile = row.stagedFile,
                        format = row.format,
                        contentHash = row.contentHash
                    )
                }
            )
            BackupImportResult.Success(imported = outcome.imported, alreadyPresent = outcome.alreadyPresent)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            e.printStackTrace()
            BackupImportResult.Failed(e.localizedMessage ?: "Couldn't import the backup.")
        } finally {
            stagingDir.deleteRecursively()
        }
    }

    /** Reads at most [limit] bytes; null only on IO failure (caller checks size). */
    private fun readCapped(stream: java.io.InputStream, limit: Int): ByteArray? {
        return try {
            val out = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            var total = 0
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                total += read
                if (total > limit) {
                    // Keep draining limit+1 proof without unbounded growth.
                    out.write(buffer, 0, read)
                    break
                }
                out.write(buffer, 0, read)
            }
            out.toByteArray()
        } catch (_: Exception) {
            null
        }
    }

    /** Copies at most [limit] bytes; returns bytes written, -1 when over limit. IO errors throw. */
    private fun copyCapped(stream: java.io.InputStream, target: File, limit: Long): Long {
        var total = 0L
        try {
            FileOutputStream(target).use { out ->
                val buffer = ByteArray(8192)
                while (true) {
                    val read = stream.read(buffer)
                    if (read <= 0) break
                    total += read
                    if (total > limit) {
                        out.flush()
                        return -1L
                    }
                    out.write(buffer, 0, read)
                }
                out.flush()
            }
        } catch (e: Exception) {
            try {
                target.delete()
            } catch (_: Exception) {
            }
            throw e
        }
        return total
    }

    private fun readImageFormat(file: File): String? {
        return try {
            val head = ByteArray(16)
            var filled = 0
            file.inputStream().use { stream ->
                while (filled < 12) {
                    val read = stream.read(head, filled, 12 - filled)
                    if (read <= 0) break
                    filled += read
                }
            }
            if (filled < 12) null else StickerMimeTypes.sniffExtension(head)
        } catch (_: Exception) {
            null
        }
    }

    private fun sameImageFormat(a: String, b: String): Boolean {
        if (a == b) return true
        val pair = setOf(a, b)
        return pair == setOf("jpg", "jpeg")
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
