package com.hkm.stickhub

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.hkm.stickhub.data.provider.StickerContentProvider
import com.hkm.stickhub.util.StickerMimeTypes
import org.junit.Assert.assertArrayEquals
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
import android.provider.OpenableColumns
import java.io.File
import java.io.FileNotFoundException

/**
 * Phase 11-B: the exported provider contract paste/share targets rely on.
 * MIME must match real bytes, reads stay read-only, and nothing outside the
 * sticker/share-cache roots is reachable.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StickerProviderContractTest {

    private fun provider(): StickerContentProvider {
        return Robolectric.buildContentProvider(StickerContentProvider::class.java)
            .create()
            .get()
    }

    private fun pngBytes(size: Int = 16, color: Int = Color.RED): ByteArray {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(color)
        }
        val out = java.io.ByteArrayOutputStream()
        assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
        bitmap.recycle()
        return out.toByteArray()
    }

    @Test
    fun getTypeMatchesRealContainer() {
        val context = RuntimeEnvironment.getApplication()
        val payload = File(File(context.cacheDir, "sticker_share").apply { mkdirs() }, "share_type_test.png")
        payload.writeBytes(pngBytes())
        val uri = StickerContentProvider.getClipboardUri(context, payload)
        try {
            assertEquals(StickerMimeTypes.PNG, StickerContentProvider().getType(uri))
        } finally {
            payload.delete()
        }
    }

    @Test
    fun getStreamTypesNegotiatesImageFilters() {
        val context = RuntimeEnvironment.getApplication()
        val payload = File(File(context.cacheDir, "sticker_share").apply { mkdirs() }, "share_stream_test.png")
        payload.writeBytes(pngBytes())
        val uri = StickerContentProvider.getClipboardUri(context, payload)
        val provider = StickerContentProvider()
        try {
            assertArrayEquals(arrayOf(StickerMimeTypes.PNG), provider.getStreamTypes(uri, "image/*"))
            assertArrayEquals(arrayOf(StickerMimeTypes.PNG), provider.getStreamTypes(uri, "image/png"))
            assertNull(provider.getStreamTypes(uri, "text/plain"))
        } finally {
            payload.delete()
        }
    }

    @Test
    fun queryReturnsDisplayNameAndSize() {
        val context = RuntimeEnvironment.getApplication()
        val payload = File(File(context.cacheDir, "sticker_share").apply { mkdirs() }, "share_query_test.png")
        val bytes = pngBytes()
        payload.writeBytes(bytes)
        val uri = StickerContentProvider.getClipboardUri(context, payload)
        val provider = provider()
        try {
            provider.query(uri, null, null, null, null)!!.use { cursor ->
                assertTrue(cursor.moveToFirst())
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                assertEquals("share_query_test.png", cursor.getString(nameIndex))
                assertEquals(bytes.size.toLong(), cursor.getLong(sizeIndex))
            }
        } finally {
            payload.delete()
        }
    }

    @Test
    fun openFileIsReadOnlyAndReadsPngMagic() {
        val context = RuntimeEnvironment.getApplication()
        val payload = File(File(context.cacheDir, "sticker_share").apply { mkdirs() }, "share_read_test.png")
        payload.writeBytes(pngBytes())
        val uri = StickerContentProvider.getClipboardUri(context, payload)
        val provider = provider()
        try {
            provider.openFile(uri, "r").use { descriptor ->
                java.io.FileInputStream(descriptor.fileDescriptor).use { input ->
                    assertEquals(0x89, input.read())
                }
            }
            try {
                provider.openFile(uri, "w")
                fail("write mode must be refused")
            } catch (_: SecurityException) {
            }
        } finally {
            payload.delete()
        }
    }

    @Test
    fun missingFileThrowsFileNotFound() {
        val context = RuntimeEnvironment.getApplication()
        val uri = StickerContentProvider.getClipboardUri(
            context,
            File(context.cacheDir, "share_absent_test.png")
        )
        try {
            provider().openFile(uri, "r")
            fail("absent payload must throw")
        } catch (_: FileNotFoundException) {
        }
    }

    @Test
    fun clipboardPathRejectsNonShareNames() {
        val context = RuntimeEnvironment.getApplication()
        val uri = Uri.parse("content://com.hkm.stickhub.stickerprovider/clipboard/evil.png")
        try {
            provider().openFile(uri, "r")
            fail("non share_ name must be refused")
        } catch (_: SecurityException) {
        }
    }

    @Test
    fun encodedTraversalIsRejected() {
        val uri = Uri.parse("content://com.hkm.stickhub.stickerprovider/clipboard/share_%2e%2e%2fevil.png")
        try {
            provider().openFile(uri, "r")
            fail("encoded traversal must be refused")
        } catch (_: SecurityException) {
        }
    }

    @Test
    fun foreignAuthorityIsRejected() {
        val uri = Uri.parse("content://com.evil.app/clipboard/share_x.png")
        try {
            provider().openFile(uri, "r")
            fail("foreign authority must be refused")
        } catch (_: SecurityException) {
        }
    }
}
