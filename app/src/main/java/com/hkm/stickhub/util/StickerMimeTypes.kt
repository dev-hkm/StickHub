package com.hkm.stickhub.util

/**
 * Single source of truth mapping sticker image formats to MIME types and
 * file extensions. Used by the clipboard path, the content provider and the
 * backup format so bytes, extensions and MIME types can never disagree.
 */
object StickerMimeTypes {
    const val PNG = "image/png"
    const val JPEG = "image/jpeg"
    const val WEBP = "image/webp"
    const val GIF = "image/gif"
    const val HEIC = "image/heic"
    const val HEIF = "image/heif"

    private val EXTENSIONS = setOf("png", "jpg", "jpeg", "webp", "gif", "heic", "heif")

    fun isSupportedExtension(extension: String): Boolean =
        extension.lowercase() in EXTENSIONS

    fun fromFileName(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> JPEG
            "webp" -> WEBP
            "gif" -> GIF
            "heic" -> HEIC
            "heif" -> HEIF
            else -> PNG
        }
    }

    fun extensionForMime(mimeType: String?): String {
        return when (mimeType?.lowercase()) {
            WEBP -> "webp"
            "image/jpg", JPEG -> "jpg"
            GIF -> "gif"
            HEIC, HEIF -> "heic"
            else -> "png"
        }
    }

    /**
     * Best-effort container sniff from leading bytes. Returns the detected
     * extension or null when the bytes match no known image container.
     */
    fun sniffExtension(bytes: ByteArray): String? {
        if (bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
        ) return "png"
        if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
            return "jpg"
        }
        if (bytes.size >= 6 &&
            bytes[0] == 0x47.toByte() && bytes[1] == 0x49.toByte() && bytes[2] == 0x46.toByte()
        ) return "gif"
        if (bytes.size >= 12 &&
            bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() &&
            bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() &&
            bytes[8] == 0x57.toByte() && bytes[9] == 0x45.toByte() &&
            bytes[10] == 0x42.toByte() && bytes[11] == 0x50.toByte()
        ) return "webp"
        if (bytes.size >= 12 &&
            bytes[4] == 0x66.toByte() && bytes[5] == 0x74.toByte() &&
            bytes[6] == 0x79.toByte() && bytes[7] == 0x70.toByte()
        ) {
            val brand = try {
                bytes.copyOfRange(8, 12).toString(Charsets.US_ASCII)
            } catch (_: Exception) {
                ""
            }
            if (brand == "heic" || brand == "heix" || brand == "hevc" || brand == "hevx") return "heic"
            if (brand == "mif1" || brand == "msf1") return "heif"
        }
        return null
    }
}
