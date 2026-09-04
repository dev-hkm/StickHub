package com.hkm.stickhub

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hkm.stickhub.util.ClipboardHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClipboardMultiImportTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val clipboard: ClipboardManager
        get() = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private fun imageClip(vararg items: ClipData.Item): ClipData {
        return ClipData(
            ClipDescription("images", arrayOf("image/*")),
            items[0]
        ).apply {
            for (i in 1 until items.size) addItem(items[i])
        }
    }

    @Test
    fun severalUriItemsAreAllCollected() {
        clipboard.setPrimaryClip(
            imageClip(
                ClipData.Item(Uri.parse("file:///sdcard/a.png")),
                ClipData.Item(Uri.parse("file:///sdcard/b.png")),
                ClipData.Item(Uri.parse("file:///sdcard/c.png"))
            )
        )
        val (uris, _) = ClipboardHelper.getClipboardImagesStamped(context)
        assertEquals(3, uris.size)
        assertTrue(uris.contains(Uri.parse("file:///sdcard/b.png")))
    }

    @Test
    fun singleItemUriListTextExpandsToEveryLine() {
        // Some gallery apps copy N photos as one item whose text holds N uri lines.
        // A garbage line must not kill the good ones around it.
        val text = "file:///sdcard/a.png\nnot a uri at all\nfile:///sdcard/b.png\n  \nfile:///sdcard/c.png"
        val clip = ClipData(
            ClipDescription(
                "images",
                arrayOf(ClipDescription.MIMETYPE_TEXT_URILIST, "image/*")
            ),
            ClipData.Item(text)
        )
        clipboard.setPrimaryClip(clip)
        val (uris, _) = ClipboardHelper.getClipboardImagesStamped(context)
        // A garbage line must not kill the good ones around it. (Exact contents
        // are not asserted: Robolectric shadows decode any stream, while a real
        // device rejects the garbage line at the eligibility gate.)
        assertTrue(
            uris.containsAll(
                listOf(
                    Uri.parse("file:///sdcard/a.png"),
                    Uri.parse("file:///sdcard/b.png"),
                    Uri.parse("file:///sdcard/c.png")
                )
            )
        )
    }

    @Test
    fun repeatedUriLinesAreDeduplicated() {
        val uri = Uri.parse("file:///sdcard/a.png")
        clipboard.setPrimaryClip(imageClip(ClipData.Item(uri), ClipData.Item(uri)))
        val (uris, _) = ClipboardHelper.getClipboardImagesStamped(context)
        assertEquals(listOf(uri), uris)
    }

    @Test
    fun plainTextLinesAreParsedWithoutUriListMime() {
        val text = "file:///sdcard/a.png\nfile:///sdcard/b.png"
        val clip = ClipData(
            ClipDescription("images", arrayOf("text/plain", "image/*")),
            ClipData.Item(text)
        )
        clipboard.setPrimaryClip(clip)
        val (uris, _) = ClipboardHelper.getClipboardImagesStamped(context)
        assertEquals(
            listOf(Uri.parse("file:///sdcard/a.png"), Uri.parse("file:///sdcard/b.png")),
            uris
        )
    }

    @Test
    fun htmlImgSourcesAreCollected() {
        val html = "<p>pics</p><img src=\"file:///sdcard/a.png\"/><img src=\"file:///sdcard/b.png\">"
        val clip = ClipData(
            ClipDescription("images", arrayOf("text/html", "image/*")),
            ClipData.Item("", html)
        )
        clipboard.setPrimaryClip(clip)
        val (uris, _) = ClipboardHelper.getClipboardImagesStamped(context)
        assertEquals(
            listOf(Uri.parse("file:///sdcard/a.png"), Uri.parse("file:///sdcard/b.png")),
            uris
        )
    }

    @Test
    fun intentSingleStreamIsCollected() {
        val uri = Uri.parse("file:///sdcard/a.png")
        val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uri)
        clipboard.setPrimaryClip(imageClip(ClipData.Item(intent)))
        val (uris, _) = ClipboardHelper.getClipboardImagesStamped(context)
        assertEquals(listOf(uri), uris)
    }

    @Test
    fun intentMultipleStreamsAreAllCollected() {
        val a = Uri.parse("file:///sdcard/a.png")
        val b = Uri.parse("file:///sdcard/b.png")
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
            .putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(a, b))
        clipboard.setPrimaryClip(imageClip(ClipData.Item(intent)))
        val (uris, _) = ClipboardHelper.getClipboardImagesStamped(context)
        assertEquals(listOf(a, b), uris)
    }

    @Test
    fun scanReportsSkippedCount() {
        val clip = ClipData(
            ClipDescription("images", arrayOf("image/*")),
            ClipData.Item(Uri.parse("file:///sdcard/a.png"))
        )
        clipboard.setPrimaryClip(clip)
        val scan = ClipboardHelper.scanClipboardImages(context)
        assertEquals(listOf(Uri.parse("file:///sdcard/a.png")), scan.uris)
        assertEquals(0, scan.skipped)
    }
}
