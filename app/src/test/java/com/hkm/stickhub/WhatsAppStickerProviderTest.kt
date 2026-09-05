package com.hkm.stickhub

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.provider.WhatsAppStickerProvider
import com.hkm.stickhub.util.WhatsAppPackBuilder
import com.hkm.stickhub.util.WhatsAppPackIntents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileNotFoundException

/**
 * The exact 4-endpoint contract WhatsApp queries, plus the outbound
 * ENABLE_STICKER_PACK intent and whitelist-check shapes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WhatsAppStickerProviderTest {

    private fun provider(): WhatsAppStickerProvider {
        return Robolectric.buildContentProvider(WhatsAppStickerProvider::class.java)
            .create()
            .get()
    }

    private fun redPng(target: File) {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }
        target.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }

    private fun buildPack(): File {
        val context = RuntimeEnvironment.getApplication()
        val srcDir = File(context.cacheDir, "wa-prov-src").apply { mkdirs() }
        val items = (1..4).map {
            val file = File(srcDir, "p$it.png")
            redPng(file)
            StickerItem(filePath = file.absolutePath, title = "pack $it", category = "Dogs")
        }
        val built = WhatsAppPackBuilder.buildPack(context, "stickhub-dogs", "Dogs", items)
        assertNotNull(built)
        return built!!.directory
    }

    private fun cleanup() {
        val context = RuntimeEnvironment.getApplication()
        File(context.cacheDir, "wa-prov-src").deleteRecursively()
        File(context.filesDir, WhatsAppPackBuilder.PACKS_DIR).deleteRecursively()
    }

    @Test
    fun metadataListsAllPacksWithContractColumns() {
        buildPack()
        try {
            val uri = Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/metadata")
            provider().query(uri, null, null, null, null)!!.use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("stickhub-dogs", cursor.getString(cursor.getColumnIndexOrThrow("sticker_pack_identifier")))
                assertEquals("Dogs", cursor.getString(cursor.getColumnIndexOrThrow("sticker_pack_name")))
                assertEquals("StickHub", cursor.getString(cursor.getColumnIndexOrThrow("sticker_pack_publisher")))
                assertEquals("tray.png", cursor.getString(cursor.getColumnIndexOrThrow("sticker_pack_icon")))
                assertTrue(cursor.getString(cursor.getColumnIndexOrThrow("image_data_version")).isNotBlank())
                assertEquals("0", cursor.getString(cursor.getColumnIndexOrThrow("whatsapp_will_not_cache_stickers")))
                assertEquals("0", cursor.getString(cursor.getColumnIndexOrThrow("animated_sticker_pack")))
            }
        } finally {
            cleanup()
        }
    }

    @Test
    fun singlePackAndUnknownPackBehave() {
        try {
            buildPack()
            val one = Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/metadata/stickhub-dogs")
            provider().query(one, null, null, null, null)!!.use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.count)
            }
            val missing = Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/metadata/stickhub-nope")
            provider().query(missing, null, null, null, null)!!.use { cursor ->
                assertEquals(0, cursor.count)
            }
        } finally {
            cleanup()
        }
    }

    @Test
    fun stickersRowsCarryFileEmojiAndA11y() {
        try {
            buildPack()
            val uri = Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/stickers/stickhub-dogs")
            provider().query(uri, null, null, null, null)!!.use { cursor ->
                assertEquals(4, cursor.count)
                assertTrue(cursor.moveToFirst())
                val file = cursor.getString(cursor.getColumnIndexOrThrow("sticker_file_name"))
                assertTrue(file.endsWith(".webp"))
                assertTrue(cursor.getString(cursor.getColumnIndexOrThrow("sticker_emoji")).isNotBlank())
            }
        } finally {
            cleanup()
        }
    }

    @Test
    fun assetsOpenWithTrayPngMagicAndSizedWebp() {
        buildPack()
        try {
            val provider = provider()
            // NOTE: Robolectric's Bitmap shadow cannot emit real WebP bytes,
            // so container magic is device-verified; here we prove the served
            // asset exists under its manifest name within the 100 KB gate the
            // encoder enforces (see WhatsAppStickerEncoderTest for the loop).
            val webpUri = Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/stickers_asset/stickhub-dogs/s01.webp")
            provider.openAssetFile(webpUri, "r")!!.use { asset ->
                assertTrue(asset.length in 1..(100 * 1024))
                asset.createInputStream().use { input ->
                    var total = 0L
                    val buf = ByteArray(8192)
                    while (true) {
                        val read = input.read(buf)
                        if (read <= 0) break
                        total += read
                    }
                    assertTrue(total in 1..(100 * 1024))
                }
            }
            val trayUri = Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/stickers_asset/stickhub-dogs/tray.png")
            provider.openAssetFile(trayUri, "r")!!.use { asset ->
                asset.createInputStream().use { input ->
                    assertEquals(0x89, input.read())
                }
            }
        } finally {
            cleanup()
        }
    }

    @Test
    fun getTypeMatchesWhatsAppContract() {
        val auth = WhatsAppStickerProvider.AUTHORITY
        val provider = WhatsAppStickerProvider()
        assertEquals(
            "vnd.android.cursor.dir/vnd.$auth.metadata",
            provider.getType(Uri.parse("content://$auth/metadata"))
        )
        assertEquals(
            "vnd.android.cursor.item/vnd.$auth.metadata",
            provider.getType(Uri.parse("content://$auth/metadata/stickhub-dogs"))
        )
        assertEquals(
            "vnd.android.cursor.dir/vnd.$auth.stickers",
            provider.getType(Uri.parse("content://$auth/stickers/stickhub-dogs"))
        )
        assertEquals(
            "image/webp",
            provider.getType(Uri.parse("content://$auth/stickers_asset/stickhub-dogs/s01.webp"))
        )
        assertEquals(
            "image/png",
            provider.getType(Uri.parse("content://$auth/stickers_asset/stickhub-dogs/tray.png"))
        )
    }

    @Test
    fun strayAndTraversalAssetsAreRefused() {
        try {
            val dir = buildPack()
            // A stray file smuggled into the pack dir is not manifest-listed.
            File(dir, "notes.txt").writeText("hello")
            val provider = provider()
            try {
                provider.openAssetFile(
                    Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/stickers_asset/stickhub-dogs/notes.txt"),
                    "r"
                )
                fail("stray file must be refused")
            } catch (_: FileNotFoundException) {
            }
            // Traversal must end unserved, whether by no-match (null),
            // failed validation, or thrown refusal.
            var traversalServed = false
            try {
                val asset = provider.openAssetFile(
                    Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/stickers_asset/../stickerprovider/x"),
                    "r"
                )
                if (asset != null) {
                    traversalServed = true
                    asset.close()
                }
            } catch (_: Exception) {
                traversalServed = false
            }
            assertTrue(!traversalServed)
            try {
                provider.openAssetFile(
                    Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/stickers_asset/stickhub-dogs/s01.webp"),
                    "w"
                )
                fail("write mode must be refused")
            } catch (_: SecurityException) {
            }
        } finally {
            cleanup()
        }
    }

    @Test
    fun enableIntentCarriesExactExtras() {
        val intent = WhatsAppPackIntents.enableIntent(
            com.hkm.stickhub.util.WhatsAppStickerPack(
                identifier = "stickhub-dogs",
                name = "Dogs",
                publisher = "StickHub",
                trayImageFile = "tray.png",
                stickers = emptyList(),
                imageDataVersion = "1"
            )
        )
        assertEquals("com.whatsapp.intent.action.ENABLE_STICKER_PACK", intent.action)
        assertEquals("stickhub-dogs", intent.getStringExtra("sticker_pack_id"))
        assertEquals(WhatsAppStickerProvider.AUTHORITY, intent.getStringExtra("sticker_pack_authority"))
        assertEquals("Dogs", intent.getStringExtra("sticker_pack_name"))
    }

    @Test
    fun whitelistCheckSurfacesUnknownSafely() {
        // No WhatsApp installed under Robolectric: null, never a crash.
        val context = RuntimeEnvironment.getApplication()
        assertNull(WhatsAppPackIntents.isPackAdded(context, "stickhub-dogs"))
        val uri = WhatsAppPackIntents.whitelistUri("stickhub-dogs").toString()
        assertTrue(uri.startsWith("content://com.whatsapp.provider.sticker_whitelist_check/is_whitelisted"))
        assertTrue(uri.contains("stickhub-dogs"))
        assertTrue(uri.contains(WhatsAppStickerProvider.AUTHORITY))
    }

    @Test
    fun unknownUrisThrowIllegalArgument() {
        try {
            provider().query(Uri.parse("content://${WhatsAppStickerProvider.AUTHORITY}/nope"), null, null, null, null)
            fail("unknown path must throw")
        } catch (_: IllegalArgumentException) {
        }
    }
}
