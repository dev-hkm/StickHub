package com.hkm.stickhub.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException
import java.net.URLDecoder

class StickerContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.hkm.stickhub.stickerprovider"
        val CONTENT_URI: Uri get() = Uri.parse("content://$AUTHORITY")

        private const val CODE_STICKER = 1
        private val uriMatcher by lazy {
            UriMatcher(UriMatcher.NO_MATCH).apply {
                addURI(AUTHORITY, "stickers/*", CODE_STICKER)
            }
        }

        fun getStickerUri(context: Context, file: File): Uri {
            return Uri.parse("content://$AUTHORITY/stickers/${file.name}")
        }

        fun isValidBasename(name: String): Boolean {
            if (name.isBlank() || name.length > 128) return false
            if (name.contains("/") || name.contains("\\") || name.contains("..")) return false
            return name.matches(Regex("^[a-zA-Z0-9._-]+$"))
        }
    }

    override fun onCreate(): Boolean = true

    private fun resolveStickerFile(uri: Uri): File {
        val match = uriMatcher.match(uri)
        if (match != CODE_STICKER) {
            throw SecurityException("Unauthorized URI pattern: $uri")
        }

        val rawSegment = uri.lastPathSegment ?: throw FileNotFoundException("Missing file path")
        val decodedName = try {
            URLDecoder.decode(rawSegment, "UTF-8")
        } catch (_: Exception) {
            rawSegment
        }

        if (!isValidBasename(decodedName)) {
            throw SecurityException("Invalid sticker basename: $decodedName")
        }

        val ctx = context ?: throw IllegalStateException("Provider context is null")
        val stickersDir = File(ctx.filesDir, "stickers")
        val canonicalDir = stickersDir.canonicalPath

        val targetFile = File(stickersDir, decodedName)
        val canonicalTarget = targetFile.canonicalPath

        // Strict canonical boundary check
        if (!canonicalTarget.startsWith(canonicalDir + File.separator)) {
            throw SecurityException("Path traversal attempt detected: $decodedName")
        }

        return targetFile
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val file = resolveStickerFile(uri)
        val cols = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        val matrixCursor = MatrixCursor(cols, 1)
        val row = matrixCursor.newRow()
        for (col in cols) {
            when (col) {
                OpenableColumns.DISPLAY_NAME -> row.add(file.name)
                OpenableColumns.SIZE -> row.add(if (file.exists()) file.length() else 0L)
                else -> row.add(null)
            }
        }
        return matrixCursor
    }

    override fun getType(uri: Uri): String {
        val fileName = uri.lastPathSegment.orEmpty()
        return when {
            fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
            fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            else -> "image/png"
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") {
            throw SecurityException("Write mode not permitted")
        }

        val file = resolveStickerFile(uri)
        if (!file.exists() || !file.isFile) {
            throw FileNotFoundException("Sticker not found: ${file.name}")
        }

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
