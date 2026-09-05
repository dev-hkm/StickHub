package com.hkm.stickhub

import android.graphics.Bitmap
import android.graphics.Color
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.util.WhatsAppPackBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

/**
 * Pack assembly rules mirroring WhatsApp/stickers limits: 3-30 stickers,
 * identifier charset, default search emoji, a11y cap, durable manifest.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WhatsAppPackBuilderTest {

    private fun redPng(target: File, seedColor: Int = Color.RED) {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            eraseColor(seedColor)
        }
        target.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }

    private fun item(dir: File, name: String, category: String, title: String = "t"): StickerItem {
        val file = File(dir, name)
        redPng(file)
        return StickerItem(filePath = file.absolutePath, title = title, category = category)
    }

    @Test
    fun slugAndIdentifierStayInContractCharset() {
        assertEquals("memes", WhatsAppPackBuilder.slugifyCategory("Memes"))
        assertEquals("my-cats-2026", WhatsAppPackBuilder.slugifyCategory("My Cats! 2026"))
        assertEquals("pack", WhatsAppPackBuilder.slugifyCategory("?!"))
        assertTrue(WhatsAppPackBuilder.isValidPackId(WhatsAppPackBuilder.packIdentifier("General")))
        assertTrue(!WhatsAppPackBuilder.isValidPackId("../evil"))
        assertTrue(!WhatsAppPackBuilder.isValidPackId("a/b"))
    }

    @Test
    fun summarizeKeepsOnlyQualifyingCategories() {
        val items = listOf(
            StickerItem(filePath = "/x/1.png", category = "Big"),
            StickerItem(filePath = "/x/2.png", category = "Big"),
            StickerItem(filePath = "/x/3.png", category = "Big"),
            StickerItem(filePath = "/x/4.png", category = "Small")
        )
        val summaries = WhatsAppPackBuilder.summarize(items)
        assertEquals(1, summaries.size)
        assertEquals("stickhub-big", summaries[0].packId)
        assertEquals("Big", summaries[0].displayName)
        assertEquals(3, summaries[0].stickerCount)
    }

    @Test
    fun buildPackWritesManifestTrayAndCappedStickers() {
        val context = RuntimeEnvironment.getApplication()
        val srcDir = File(context.cacheDir, "wa-build-src").apply { mkdirs() }
        val items = (1..35).map { item(srcDir, "s$it.png", "Cats", "cat $it") }

        val built = WhatsAppPackBuilder.buildPack(context, "stickhub-cats", "Cats", items)
        assertNotNull(built)
        built!!
        assertEquals(30, built.pack.stickers.size)
        assertTrue(built.pack.stickers.all { it.emojis.size == 1 })
        assertTrue(built.pack.stickers.all { it.accessibilityText.length <= 125 })
        assertTrue(File(built.directory, "tray.png").isFile)
        assertTrue(built.pack.stickers.all { File(built.directory, it.fileName).isFile })

        // Manifest round-trips through the provider's reader.
        val reread = WhatsAppPackBuilder.readManifest(built.directory)
        assertNotNull(reread)
        assertEquals("stickhub-cats", reread!!.identifier)
        assertEquals(30, reread.stickers.size)

        srcDir.deleteRecursively()
        File(context.filesDir, WhatsAppPackBuilder.PACKS_DIR).deleteRecursively()
    }

    @Test
    fun tinyCategoryRefusesToBuild() {
        val context = RuntimeEnvironment.getApplication()
        val srcDir = File(context.cacheDir, "wa-build-tiny").apply { mkdirs() }
        val items = (1..2).map { item(srcDir, "t$it.png", "Tiny") }

        assertNull(WhatsAppPackBuilder.buildPack(context, "stickhub-tiny", "Tiny", items))
        assertTrue(!File(File(context.filesDir, WhatsAppPackBuilder.PACKS_DIR), "stickhub-tiny").exists())

        srcDir.deleteRecursively()
    }

    @Test
    fun pruneKeepsOnlyLivePacks() {
        val context = RuntimeEnvironment.getApplication()
        val root = File(context.filesDir, WhatsAppPackBuilder.PACKS_DIR).apply { mkdirs() }
        File(root, "stickhub-keep").apply { mkdirs() }
        File(root, "stickhub-gone").apply { mkdirs() }

        WhatsAppPackBuilder.pruneExcept(context, setOf("stickhub-keep"))

        assertTrue(File(root, "stickhub-keep").isDirectory)
        assertTrue(!File(root, "stickhub-gone").exists())
        File(root, "stickhub-keep").deleteRecursively()
    }
}
