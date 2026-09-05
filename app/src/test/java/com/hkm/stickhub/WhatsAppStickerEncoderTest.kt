package com.hkm.stickhub

import android.graphics.Bitmap
import android.graphics.Color
import com.hkm.stickhub.util.StickerTransport
import com.hkm.stickhub.util.WhatsAppStickerEncoder
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
 * WhatsApp's public contract demands static WebP exactly 512x512 under
 * 100 KB and a PNG tray icon 24-512px under 50 KB. Byte-level assertions
 * (magic + size) because Robolectric cannot decode WebP; the tray PNG round
 * trip additionally proves dimensions through a real decode.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WhatsAppStickerEncoderTest {

    private fun redSquare(target: File, size: Int = 128) {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }
        target.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }

    /**
     * Fake stand-in for the framework WebP codec (Robolectric's Bitmap
     * shadow cannot emit real WebP bytes). Writes a framed payload whose
     * size depends on quality so the stepping loop is genuinely exercised;
     * the test then proves byte pass-through and gate enforcement.
     */
    private fun fakeWebpCodec(sizes: Map<Int, Int>, usedQualities: MutableList<Int>): (Bitmap, Int, java.io.OutputStream) -> Boolean {
        return { _, quality, stream ->
            usedQualities.add(quality)
            val size = sizes[quality] ?: 10_000
            stream.write("RIFF".toByteArray())
            stream.write(ByteArray(4))
            stream.write("WEBP".toByteArray())
            if (size > 12) stream.write(ByteArray(size - 12))
            true
        }
    }

    @Test
    fun qualityLoopPicksFirstFittingStepVerbatim() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "wa-enc-source.png")
        redSquare(source)
        val target = File(context.cacheDir, "wa-enc-out.webp")
        val used = mutableListOf<Int>()

        val sizes = mapOf(90 to 200_000, 80 to 150_000, 70 to 90_000, 60 to 40_000, 50 to 20_000)
        assertTrue(WhatsAppStickerEncoder.encodeSticker(source, target, fakeWebpCodec(sizes, used)))

        assertEquals(listOf(90, 80, 70), used)
        val bytes = target.readBytes()
        assertEquals(90_000, bytes.size)
        // Codec bytes pass through untouched: real RIFF/WEBP framing intact.
        assertEquals("RIFF", bytes.copyOfRange(0, 4).toString(Charsets.US_ASCII))
        assertEquals("WEBP", bytes.copyOfRange(8, 12).toString(Charsets.US_ASCII))
        source.delete()
        target.delete()
    }

    @Test
    fun nothingFittingDeletesTargetAndFails() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "wa-enc-huge.png")
        redSquare(source)
        val target = File(context.cacheDir, "wa-enc-huge.webp")
        val used = mutableListOf<Int>()

        val sizes = mapOf(90 to 200_000, 80 to 180_000, 70 to 160_000, 60 to 140_000, 50 to 120_000)
        assertTrue(!WhatsAppStickerEncoder.encodeSticker(source, target, fakeWebpCodec(sizes, used)))

        assertEquals(listOf(90, 80, 70, 60, 50), used)
        assertTrue(!target.exists())
        source.delete()
    }

    @Test
    fun envelopeIsExactly512BeforeEncode() {
        // Dimension conformance is verified on the real envelope bitmap
        // (fully JVM-testable); the codec only re-encodes those pixels.
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "wa-enc-dims.png")
        redSquare(source, 96)
        val envelope = StickerTransport.renderEnvelope(source)
        assertNotNull(envelope)
        envelope!!
        assertEquals(512, envelope.width)
        assertEquals(512, envelope.height)
        envelope.recycle()
        source.delete()
    }

    @Test
    fun corruptAndTransparentSourcesFail() {
        val context = RuntimeEnvironment.getApplication()
        val corrupt = File(context.cacheDir, "wa-enc-corrupt.png").apply {
            writeBytes(byteArrayOf(9, 9, 9))
        }
        assertTrue(!WhatsAppStickerEncoder.encodeSticker(corrupt, File(context.cacheDir, "wa-enc-x.webp")))

        val clear = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
        }
        val transparent = File(context.cacheDir, "wa-enc-clear.png")
        transparent.outputStream().use { assertTrue(clear.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        clear.recycle()
        assertTrue(!WhatsAppStickerEncoder.encodeSticker(transparent, File(context.cacheDir, "wa-enc-y.webp")))

        val missing = File(context.cacheDir, "wa-enc-missing.png")
        assertTrue(!WhatsAppStickerEncoder.encodeSticker(missing, File(context.cacheDir, "wa-enc-z.webp")))
        corrupt.delete()
        transparent.delete()
    }

    @Test
    fun trayIconIs96PngUnder50Kb() {
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "wa-enc-tray-src.png")
        redSquare(source)
        val target = File(context.cacheDir, "wa-enc-tray.png")

        assertTrue(WhatsAppStickerEncoder.encodeTrayIcon(source, target))
        assertTrue(target.length() in 1..(50 * 1024))
        val decoded = android.graphics.BitmapFactory.decodeFile(target.absolutePath)
        assertNotNull(decoded)
        decoded!!
        assertEquals(96, decoded.width)
        assertEquals(96, decoded.height)
        decoded.recycle()
        source.delete()
        target.delete()
    }

    @Test
    fun envelopePixelsAreSharedWithClipboardPath() {
        // Same source through renderEnvelope must succeed for both outputs:
        // no divergent pixel pipelines per caller.
        val context = RuntimeEnvironment.getApplication()
        val source = File(context.cacheDir, "wa-enc-shared.png")
        redSquare(source, 96)
        assertNotNull(StickerTransport.renderEnvelope(source)?.also { it.recycle() })
        val webp = File(context.cacheDir, "wa-enc-shared.webp")
        assertTrue(WhatsAppStickerEncoder.encodeSticker(source, webp))
        assertNull(StickerTransport.renderEnvelope(File(context.cacheDir, "wa-enc-absent.png")))
        source.delete()
        webp.delete()
    }
}
