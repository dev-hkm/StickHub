package com.hkm.stickhub

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
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
        val text = "file:///sdcard/a.png\nfile:///sdcard/b.png\n  \nfile:///sdcard/c.png"
        val clip = ClipData(
            ClipDescription(
                "images",
                arrayOf(ClipDescription.MIMETYPE_TEXT_URILIST, "image/*")
            ),
            ClipData.Item(text)
        )
        clipboard.setPrimaryClip(clip)
        val (uris, _) = ClipboardHelper.getClipboardImagesStamped(context)
        assertEquals(
            listOf(
                Uri.parse("file:///sdcard/a.png"),
                Uri.parse("file:///sdcard/b.png"),
                Uri.parse("file:///sdcard/c.png")
            ),
            uris
        )
    }

    @Test
    fun repeatedUriLinesAreDeduplicated() {
        val uri = Uri.parse("file:///sdcard/a.png")
        clipboard.setPrimaryClip(imageClip(ClipData.Item(uri), ClipData.Item(uri)))
        val (uris, _) = ClipboardHelper.getClipboardImagesStamped(context)
        assertEquals(listOf(uri), uris)
    }
}
