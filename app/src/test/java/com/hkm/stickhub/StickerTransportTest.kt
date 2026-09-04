package com.hkm.stickhub

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.hkm.stickhub.data.provider.StickerContentProvider
import com.hkm.stickhub.util.ClipboardHelper
import com.hkm.stickhub.util.StickerMimeTypes
import com.hkm.stickhub.util.StickerTransport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import org.junit.Assert.assertArrayEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StickerTransportTest {
    @Test
    fun copyBitmapPublishesTransparentPngWithMatchingClipboardMime() {
        val context = RuntimeEnvironment.getApplication()
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            setPixel(3, 3, Color.RED)
            setPixel(4, 4, Color.RED)
        }

        assertTrue(ClipboardHelper.copyBitmapToClipboard(context, bitmap))
        bitmap.recycle()

        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
            as android.content.ClipboardManager
        val clip = clipboard.primaryClip
        assertNotNull(clip)
        clip!!
        assertEquals(1, clip.itemCount)
        assertEquals(StickerMimeTypes.PNG, clip.description.getMimeType(0))
        val uri = clip.getItemAt(0).uri
        assertNotNull(uri)
        uri!!
        assertTrue(uri.lastPathSegment.orEmpty().endsWith(".png"))
        assertEquals(StickerMimeTypes.PNG, StickerContentProvider().getType(uri))
    }

    @Test
    fun providerAdvertisesConcreteImageStreamForImageConsumers() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "stream-types-source.png")
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            setPixel(1, 1, Color.RED)
            setPixel(2, 2, Color.RED)
        }
        source.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()

        val payload = StickerTransport.prepare(context, source)
        assertNotNull(payload)
        payload!!
        val uri = StickerContentProvider.getClipboardUri(context, payload.file)
        val provider = StickerContentProvider()

        assertArrayEquals(
            arrayOf(payload.mimeType),
            provider.getStreamTypes(uri, "image/*")
        )
        assertArrayEquals(
            arrayOf(payload.mimeType),
            provider.getStreamTypes(uri, payload.mimeType)
        )

        payload.file.delete()
        source.delete()
    }

    @Test
    fun providerOpensTheAdvertisedImageStream() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "typed-stream-source.png")
        val bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            setPixel(1, 1, Color.RED)
        }
        source.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()

        val payload = StickerTransport.prepare(context, source)
        assertNotNull(payload)
        payload!!
        val uri = StickerContentProvider.getClipboardUri(context, payload.file)
        val provider = Robolectric.buildContentProvider(StickerContentProvider::class.java)
            .create()
            .get()

        val descriptor = provider.openTypedAssetFile(uri, "image/*", null)
        assertNotNull(descriptor)
        descriptor!!.use { asset ->
            asset.createInputStream().use { input ->
                assertEquals(0x89, input.read())
            }
        }

        payload.file.delete()
        source.delete()
    }

    @Test
    fun transportIsBoundedSquareTransparentAndUsesOwnProviderRoute() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "transport-source.png")
        val bitmap = Bitmap.createBitmap(1024, 768, Bitmap.Config.ARGB_8888)
        // Use setPixel rather than Canvas here: Robolectric's software Canvas
        // shadow is intentionally minimal, while the production path receives
        // decoded pixels from BitmapFactory.
        for (y in 120 until 650) {
            for (x in 240 until 780) bitmap.setPixel(x, y, Color.RED)
        }
        assertTrue("source=${Integer.toHexString(bitmap.getPixel(500, 400))}", Color.alpha(bitmap.getPixel(500, 400)) > 200)
        val scaledProbe = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
        assertTrue("scaled=${Integer.toHexString(scaledProbe.getPixel(100, 100))}", Color.alpha(scaledProbe.getPixel(100, 100)) > 200)
        scaledProbe.recycle()
        source.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()

        val payload = StickerTransport.prepare(context, source)
        assertNotNull(payload)
        payload!!
        assertEquals(StickerMimeTypes.PNG, payload.mimeType)
        assertTrue(payload.file.isFile)
        val decoded = BitmapFactory.decodeFile(payload.file.absolutePath)
        assertNotNull(decoded)
        decoded!!
        assertEquals(StickerTransport.CANVAS_SIZE, decoded.width)
        assertEquals(StickerTransport.CANVAS_SIZE, decoded.height)
        val corner = decoded.getPixel(0, 0)
        val center = decoded.getPixel(256, 256)
        assertTrue("corner=${Integer.toHexString(corner)}", Color.alpha(corner) < 16)
        assertTrue("center=${Integer.toHexString(center)}", Color.alpha(center) > 200)
        assertEquals(
            StickerMimeTypes.PNG,
            StickerContentProvider().getType(StickerContentProvider.getClipboardUri(context, payload.file))
        )
        decoded.recycle()
        payload.file.delete()
        source.delete()
    }
}
