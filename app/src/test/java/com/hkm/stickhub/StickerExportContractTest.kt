package com.hkm.stickhub

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.provider.StickerContentProvider
import com.hkm.stickhub.util.ClipboardHelper
import com.hkm.stickhub.util.StickerExportService
import com.hkm.stickhub.util.StickerMimeTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

/**
 * Phase 11-C/D: the exact wire shape every sender publishes — one content-URI
 * item with the real image MIME for clipboard, canonical ACTION_SEND for
 * share. No text items, no uri-lists, no wildcard types, no file://.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StickerExportContractTest {

    private fun redPng(target: File, size: Int = 48) {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }
        target.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }

    private fun clipboardManager(context: Context): android.content.ClipboardManager {
        return context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    }

    @Test
    fun copyPublishesExactlyOneTypedContentItem() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "contract-source.png")
        redPng(source)

        assertTrue(ClipboardHelper.copyStickerToClipboard(context, StickerItem(filePath = source.absolutePath)))

        val clip = clipboardManager(context).primaryClip
        assertNotNull(clip)
        clip!!
        assertEquals(1, clip.itemCount)
        assertEquals("content", clip.getItemAt(0).uri?.scheme)
        assertEquals(StickerMimeTypes.PNG, clip.description.getMimeType(0))
        assertTrue(clip.description.hasMimeType("image/*"))
        assertNull(clip.getItemAt(0).text)
        assertTrue(!clip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST))
        assertTrue(!clip.description.hasMimeType("application/sticker"))
        assertTrue(clip.getItemAt(0).uri!!.lastPathSegment.orEmpty().endsWith(".png"))
        source.delete()
    }

    @Test
    fun copiedUriOpensThroughTheProvider() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "contract-readable.png")
        redPng(source)
        assertTrue(ClipboardHelper.copyStickerToClipboard(context, StickerItem(filePath = source.absolutePath)))

        val uri = clipboardManager(context).primaryClip!!.getItemAt(0).uri!!
        val provider = Robolectric.buildContentProvider(StickerContentProvider::class.java)
            .create()
            .get()
        provider.openFile(uri, "r").use { descriptor ->
            java.io.FileInputStream(descriptor.fileDescriptor).use { input ->
                assertEquals(0x89, input.read())
            }
        }
        source.delete()
    }

    @Test
    fun ownEchoIsNeverOfferedBackForImport() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "contract-echo.png")
        redPng(source)
        assertTrue(ClipboardHelper.copyStickerToClipboard(context, StickerItem(filePath = source.absolutePath)))

        val scan = ClipboardHelper.scanClipboardImages(context)
        assertTrue(scan.uris.isEmpty())
        source.delete()
    }

    @Test
    fun legacyClipboardImportStillSeesForeignImages() {
        // Regression anchor: the hardening above must not blind the import
        // path to genuine foreign images. file:// URIs with an image/* clip
        // description stay eligible without any provider involved.
        val context = RuntimeEnvironment.getApplication()
        val foreign = File(context.cacheDir, "contract-foreign.png")
        redPng(foreign)
        val clipboard = clipboardManager(context)
        clipboard.setPrimaryClip(
            ClipData(
                ClipDescription("imgs", arrayOf("image/*")),
                ClipData.Item(Uri.fromFile(foreign))
            )
        )
        val found = ClipboardHelper.getClipboardImageUris(context)
        assertEquals(listOf(Uri.fromFile(foreign)), found)
        foreign.delete()
    }

    @Test
    @Suppress("DEPRECATION") // Intentionally exercises the pre-33 compat read path.
    fun shareIntentHasCanonicalShape() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "contract-share-src.png")
        redPng(source, 64)
        val before = source.readBytes()
        val payload = StickerExportService.export(
            context,
            StickerExportService.ExportSource.LibraryFile(source),
            StickerExportService.ExportPurpose.SHARE
        )
        assertNotNull(payload)
        payload!!

        val intent = StickerExportService.buildShareIntent(context, payload)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals(StickerMimeTypes.PNG, intent.type)
        assertEquals(payload.uri, intent.getParcelableExtra(Intent.EXTRA_STREAM))
        val clip = intent.clipData
        assertNotNull(clip)
        clip!!
        assertEquals(1, clip.itemCount)
        assertEquals(payload.uri, clip.getItemAt(0).uri)
        assertTrue((intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
        assertTrue(!intent.toString().contains("file://"))
        // Neither the library source nor the payload was disturbed.
        assertTrue(source.readBytes().contentEquals(before))
        assertTrue(payload.file.isFile)
        payload.file.delete()
        source.delete()
    }
}
