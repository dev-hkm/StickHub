package com.hkm.stickhub.util

import android.content.Context
import com.hkm.stickhub.data.model.StickerItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * One WhatsApp sticker file inside a pack. WhatsApp's validator requires at
 * least one emoji per sticker: [DEFAULT_EMOJI] is pack metadata for
 * WhatsApp search, not app UI (StickHub UI itself stays emoji-free).
 */
data class WhatsAppStickerEntry(
    val fileName: String,
    val emojis: List<String>,
    val accessibilityText: String
)

data class WhatsAppStickerPack(
    val identifier: String,
    val name: String,
    val publisher: String,
    val trayImageFile: String,
    val stickers: List<WhatsAppStickerEntry>,
    val imageDataVersion: String
)

/** Lightweight row for Settings: no file I/O needed to list packs. */
data class WhatsAppPackSummary(
    val packId: String,
    val displayName: String,
    val stickerCount: Int
)

data class BuiltWhatsAppPack(
    val pack: WhatsAppStickerPack,
    val directory: File
)

/**
 * Builds WhatsApp-native sticker packs from library categories. One pack per
 * qualifying category (3-30 stickers after encoding; WhatsApp limits mirror
 * WhatsApp/stickers StickerPackValidator). Pack files live in durable
 * app-private storage — WhatsApp re-reads them whenever the user sends a
 * sticker, so this is never cache and never TTL-swept.
 */
object WhatsAppPackBuilder {
    const val PACKS_DIR = "whatsapp_packs"
    const val MIN_STICKERS = 3
    const val MAX_STICKERS = 30
    const val PUBLISHER = "StickHub"
    const val TRAY_FILE = "tray.png"
    const val MANIFEST_FILE = "pack.json"
    const val MAX_A11Y_CHARS = 125

    /**
     * Placeholder search emoji required by WhatsApp's per-sticker metadata.
     * Never rendered by StickHub itself.
     */
    const val DEFAULT_EMOJI = "🙂"

    private val IDENTIFIER_ALLOWED = Regex("^[a-z0-9][a-z0-9\\-_]{0,100}$")

    fun isValidPackId(packId: String): Boolean = IDENTIFIER_ALLOWED.matches(packId)

    fun slugifyCategory(name: String): String {
        val slug = name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(100)
        return slug.ifBlank { "pack" }
    }

    fun packIdentifier(category: String): String = "stickhub-" + slugifyCategory(category)

    /** Pure grouping for UI summaries: no file access. */
    fun summarize(items: List<StickerItem>): List<WhatsAppPackSummary> {
        return items.groupBy { it.category.ifBlank { "General" } }
            .filter { it.value.size >= MIN_STICKERS }
            .map { (category, stickers) ->
                WhatsAppPackSummary(
                    packId = packIdentifier(category),
                    displayName = category,
                    stickerCount = minOf(stickers.size, MAX_STICKERS)
                )
            }
            .sortedBy { it.displayName.lowercase() }
    }

    fun packsRoot(context: Context): File {
        return File(context.filesDir, PACKS_DIR).apply { mkdirs() }
    }

    /**
     * (Re)builds one pack from [items] (already filtered to one category, in
     * library order). Deletes any previous build first so renamed/edited
     * stickers never linger. Null when fewer than 3 stickers encode.
     *
     * Must be called off the main thread: encodes up to 30 WebP files.
     */
    fun buildPack(
        context: Context,
        packId: String,
        displayName: String,
        items: List<StickerItem>
    ): BuiltWhatsAppPack? {
        if (!isValidPackId(packId)) return null
        val root = packsRoot(context)
        val dir = File(root, packId)
        try {
            if (dir.exists()) dir.deleteRecursively()
            dir.mkdirs()

            val traySource = items.firstOrNull { File(it.filePath).isFile } ?: return null
            if (!WhatsAppStickerEncoder.encodeTrayIcon(File(traySource.filePath), File(dir, TRAY_FILE))) {
                dir.deleteRecursively()
                return null
            }

            val entries = mutableListOf<WhatsAppStickerEntry>()
            for ((index, item) in items.withIndex()) {
                if (entries.size >= MAX_STICKERS) break
                val source = File(item.filePath)
                if (!source.isFile) continue
                val fileName = "s%02d.webp".format(index + 1)
                if (!WhatsAppStickerEncoder.encodeSticker(source, File(dir, fileName))) continue
                entries.add(
                    WhatsAppStickerEntry(
                        fileName = fileName,
                        emojis = listOf(DEFAULT_EMOJI),
                        accessibilityText = item.title.ifBlank { "StickHub sticker" }.take(MAX_A11Y_CHARS)
                    )
                )
            }
            if (entries.size < MIN_STICKERS) {
                dir.deleteRecursively()
                return null
            }

            val pack = WhatsAppStickerPack(
                identifier = packId,
                name = displayName.take(128),
                publisher = PUBLISHER,
                trayImageFile = TRAY_FILE,
                stickers = entries,
                imageDataVersion = System.currentTimeMillis().toString()
            )
            writeManifest(dir, pack)
            return BuiltWhatsAppPack(pack, dir)
        } catch (_: Exception) {
            try {
                dir.deleteRecursively()
            } catch (_: Exception) {
            }
            return null
        }
    }

    /** Drops pack directories outside [keepIds] (e.g. renamed categories). */
    fun pruneExcept(context: Context, keepIds: Set<String>) {
        try {
            packsRoot(context).listFiles()
                ?.filter { it.isDirectory && it.name !in keepIds }
                ?.forEach {
                    try {
                        it.deleteRecursively()
                    } catch (_: Exception) {
                    }
                }
        } catch (_: Exception) {
        }
    }

    fun readBuiltPacks(context: Context): List<BuiltWhatsAppPack> {
        val root = try {
            packsRoot(context)
        } catch (_: Exception) {
            return emptyList()
        }
        return root.listFiles()
            ?.filter { it.isDirectory && isValidPackId(it.name) }
            ?.mapNotNull { dir ->
                try {
                    readManifest(dir)?.let { BuiltWhatsAppPack(it, dir) }
                } catch (_: Exception) {
                    null
                }
            }
            ?: emptyList()
    }

    private fun writeManifest(dir: File, pack: WhatsAppStickerPack) {
        val stickers = JSONArray()
        pack.stickers.forEach { entry ->
            stickers.put(
                JSONObject()
                    .put("file", entry.fileName)
                    .put("emojis", JSONArray(entry.emojis))
                    .put("a11y", entry.accessibilityText)
            )
        }
        val json = JSONObject()
            .put("identifier", pack.identifier)
            .put("name", pack.name)
            .put("publisher", pack.publisher)
            .put("tray", pack.trayImageFile)
            .put("version", pack.imageDataVersion)
            .put("stickers", stickers)
        File(dir, MANIFEST_FILE).writeText(json.toString())
    }

    fun readManifest(dir: File): WhatsAppStickerPack? {
        return try {
            val file = File(dir, MANIFEST_FILE)
            if (!file.isFile) return null
            val json = JSONObject(file.readText())
            val stickers = mutableListOf<WhatsAppStickerEntry>()
            val array = json.optJSONArray("stickers") ?: return null
            for (i in 0 until array.length()) {
                val row = array.optJSONObject(i) ?: continue
                val fileName = row.optString("file").orEmpty()
                if (fileName.isBlank()) continue
                val emojis = mutableListOf<String>()
                val emojiArray = row.optJSONArray("emojis")
                if (emojiArray != null) {
                    for (j in 0 until emojiArray.length()) {
                        emojiArray.optString(j)?.takeIf { it.isNotBlank() }?.let { emojis.add(it) }
                    }
                }
                if (emojis.isEmpty()) emojis.add(DEFAULT_EMOJI)
                stickers.add(
                    WhatsAppStickerEntry(
                        fileName = fileName,
                        emojis = emojis,
                        accessibilityText = row.optString("a11y").orEmpty()
                    )
                )
            }
            WhatsAppStickerPack(
                identifier = json.optString("identifier").orEmpty(),
                name = json.optString("name").orEmpty(),
                publisher = json.optString("publisher").orEmpty().ifBlank { PUBLISHER },
                trayImageFile = json.optString("tray").orEmpty().ifBlank { TRAY_FILE },
                stickers = stickers,
                imageDataVersion = json.optString("version").orEmpty()
            ).takeIf { it.identifier.isNotBlank() && it.stickers.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }
}
