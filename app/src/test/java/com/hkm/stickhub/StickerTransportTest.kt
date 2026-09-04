package com.hkm.stickhub

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.hkm.stickhub.data.provider.StickerContentProvider
import com.hkm.stickhub.util.StickerTransport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StickerTransportTest {
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
        assertEquals("image/webp", payload.mimeType)
        assertTrue(payload.file.isFile)
        val decoded = BitmapFactory.decodeFile(payload.file.absolutePath)
        assertNotNull(decoded)
        decoded!!
        assertEquals(StickerTransport.CANVAS_SIZE, decoded.width)
        assertEquals(StickerTransport.CANVAS_SIZE, decoded.height)
        // Robolectric's WebP decoder does not always report hasAlpha() even
        // when the decoded pixels retain transparency; assert the pixels that
        // matter to a chat receiver instead.
        val corner = decoded.getPixel(0, 0)
        val center = decoded.getPixel(256, 256)
        assertTrue("corner=${Integer.toHexString(corner)}", Color.alpha(corner) < 16)
        assertTrue("center=${Integer.toHexString(center)}", Color.alpha(center) > 200)
        assertEquals(
            "image/webp",
            StickerContentProvider().getType(StickerContentProvider.getClipboardUri(context, payload.file))
        )
        decoded.recycle()
        payload.file.delete()
        source.delete()
    }
}
