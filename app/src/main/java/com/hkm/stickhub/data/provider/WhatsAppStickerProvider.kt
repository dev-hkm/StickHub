package com.hkm.stickhub.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.hkm.stickhub.util.WhatsAppPackBuilder
import com.hkm.stickhub.util.WhatsAppStickerPack
import java.io.File
import java.io.FileNotFoundException

/**
 * WhatsApp third-party sticker pack endpoint, implementing WhatsApp's public
 * sticker-app contract (see WhatsApp/stickers sample: 4 query APIs, typed
 * MIME strings, ENABLE_STICKER_PACK intent, whitelist check).
 *
 * This is the ONLY path that produces genuinely native stickers inside a
 * chat app: WhatsApp itself reads these packs into its own sticker tray
 * after the user confirms. Nothing here can do the same for Messenger,
 * whose sticker catalog has no third-party insertion API.
 *
 * Security posture: exported=true with readPermission
 * `com.whatsapp.sticker.READ` (exactly as WhatsApp's docs require — without
 * it WhatsApp cannot read at all). Containment: pack ids and filenames are
 * allowlisted against on-disk manifests plus strict regexes and canonical
 * boundary checks, read-only, no database or unrelated file exposure.
 */
class WhatsAppStickerProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.hkm.stickhub.whatsappstickers"

        // Column names are WhatsApp's contract — do not rename.
        const val COL_PACK_IDENTIFIER = "sticker_pack_identifier"
        const val COL_PACK_NAME = "sticker_pack_name"
        const val COL_PACK_PUBLISHER = "sticker_pack_publisher"
        const val COL_PACK_ICON = "sticker_pack_icon"
        const val COL_ANDROID_LINK = "android_play_store_link"
        const val COL_IOS_LINK = "ios_app_download_link"
        const val COL_PUBLISHER_EMAIL = "sticker_pack_publisher_email"
        const val COL_PUBLISHER_WEBSITE = "sticker_pack_publisher_website"
        const val COL_PRIVACY_WEBSITE = "sticker_pack_privacy_policy_website"
        const val COL_LICENSE_WEBSITE = "sticker_pack_license_agreement_website"
        const val COL_IMAGE_DATA_VERSION = "image_data_version"
        const val COL_AVOID_CACHE = "whatsapp_will_not_cache_stickers"
        const val COL_ANIMATED_PACK = "animated_sticker_pack"

        const val COL_STICKER_FILE_NAME = "sticker_file_name"
        const val COL_STICKER_EMOJI = "sticker_emoji"
        const val COL_STICKER_A11Y = "sticker_accessibility_text"

        private const val PATH_METADATA = "metadata"
        private const val PATH_STICKERS = "stickers"
        private const val PATH_ASSET = "stickers_asset"

        private const val CODE_METADATA = 1
        private const val CODE_METADATA_SINGLE = 2
        private const val CODE_STICKERS = 3
        private const val CODE_ASSET = 4

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_METADATA, CODE_METADATA)
            addURI(AUTHORITY, "$PATH_METADATA/*", CODE_METADATA_SINGLE)
            addURI(AUTHORITY, "$PATH_STICKERS/*", CODE_STICKERS)
            addURI(AUTHORITY, "$PATH_ASSET/*/*", CODE_ASSET)
        }

        private val ASSET_NAME = Regex("^(tray\\.png|s\\d{2}\\.webp)$")

        fun packUri(packId: String, fileName: String): Uri {
            return Uri.parse("content://$AUTHORITY/$PATH_ASSET/$packId/$fileName")
        }
    }

    override fun onCreate(): Boolean = true

    private fun packs(): List<Pair<WhatsAppStickerPack, File>> {
        val ctx = context ?: return emptyList()
        return try {
            WhatsAppPackBuilder.readBuiltPacks(ctx).map { it.pack to it.directory }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        return when (uriMatcher.match(uri)) {
            CODE_METADATA -> packCursor(packs().map { it.first })
            CODE_METADATA_SINGLE -> {
                val id = uri.lastPathSegment.orEmpty()
                packCursor(packs().map { it.first }.filter { it.identifier == id })
            }
            CODE_STICKERS -> {
                val id = uri.pathSegments.getOrNull(1).orEmpty()
                val pack = packs().map { it.first }.firstOrNull { it.identifier == id }
                stickerCursor(pack)
            }
            else -> throw IllegalArgumentException("Unknown URI: ${uri.authority}${uri.path}")
        }
    }

    private fun packCursor(packList: List<WhatsAppStickerPack>): Cursor {
        val cursor = MatrixCursor(
            arrayOf(
                COL_PACK_IDENTIFIER,
                COL_PACK_NAME,
                COL_PACK_PUBLISHER,
                COL_PACK_ICON,
                COL_ANDROID_LINK,
                COL_IOS_LINK,
                COL_PUBLISHER_EMAIL,
                COL_PUBLISHER_WEBSITE,
                COL_PRIVACY_WEBSITE,
                COL_LICENSE_WEBSITE,
                COL_IMAGE_DATA_VERSION,
                COL_AVOID_CACHE,
                COL_ANIMATED_PACK
            )
        )
        packList.forEach { pack ->
            // Positional adds in the exact column order declared above
            // (MatrixCursor.RowBuilder has no named-add overload).
            cursor.newRow()
                .add(pack.identifier)
                .add(pack.name)
                .add(pack.publisher)
                .add(pack.trayImageFile)
                .add("")
                .add("")
                .add("")
                .add("")
                .add("")
                .add("")
                .add(pack.imageDataVersion)
                .add("0")
                .add("0")
        }
        return cursor
    }

    private fun stickerCursor(pack: WhatsAppStickerPack?): Cursor {
        val cursor = MatrixCursor(
            arrayOf(COL_STICKER_FILE_NAME, COL_STICKER_EMOJI, COL_STICKER_A11Y)
        )
        pack?.stickers?.forEach { entry ->
            cursor.newRow()
                .add(entry.fileName)
                .add(entry.emojis.joinToString(","))
                .add(entry.accessibilityText)
        }
        return cursor
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            CODE_METADATA -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$PATH_METADATA"
            CODE_METADATA_SINGLE -> "vnd.android.cursor.item/vnd.$AUTHORITY.$PATH_METADATA"
            CODE_STICKERS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$PATH_STICKERS"
            CODE_ASSET -> {
                when (uri.lastPathSegment) {
                    WhatsAppPackBuilder.TRAY_FILE -> "image/png"
                    else -> "image/webp"
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: ${uri.authority}${uri.path}")
        }
    }

    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        if (uriMatcher.match(uri) != CODE_ASSET) return null
        if (mode != "r") throw SecurityException("Write mode not permitted")
        val file = resolveAssetFile(uri) ?: throw FileNotFoundException("Sticker asset not found")
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        return AssetFileDescriptor(descriptor, 0, file.length())
    }

    /**
     * Triple-gated asset resolution: well-formed pack id, manifest-allowlisted
     * filename with a strict shape, and a canonical boundary check. Stray
     * files dropped into a pack dir are never served.
     */
    private fun resolveAssetFile(uri: Uri): File? {
        val segments = uri.pathSegments
        if (segments.size != 3) return null
        val packId = segments[1]
        val fileName = segments[2]
        if (!WhatsAppPackBuilder.isValidPackId(packId)) return null
        if (!ASSET_NAME.matches(fileName)) return null
        val ctx = context ?: return null
        val dir = File(File(ctx.filesDir, WhatsAppPackBuilder.PACKS_DIR), packId)
        val manifest = try {
            WhatsAppPackBuilder.readManifest(dir)
        } catch (_: Exception) {
            null
        } ?: return null
        if (manifest.identifier != packId) return null
        val allowed = manifest.stickers.any { it.fileName == fileName } ||
            fileName == manifest.trayImageFile
        if (!allowed) return null
        return try {
            val canonicalDir = dir.canonicalPath
            val target = File(dir, fileName)
            val canonicalTarget = target.canonicalPath
            if (!canonicalTarget.startsWith(canonicalDir + File.separator)) return null
            if (!target.isFile) return null
            target
        } catch (_: Exception) {
            null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
