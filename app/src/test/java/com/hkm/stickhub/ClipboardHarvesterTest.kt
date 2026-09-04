package com.hkm.stickhub

import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.net.Uri
import com.hkm.stickhub.util.CandidateSource
import com.hkm.stickhub.util.ClipboardUriHarvester
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * C1/C2/C3/C10: the shared harvester must surface every URI Android exposes,
 * in first-seen order, without opening any stream.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClipboardHarvesterTest {

    private fun desc(vararg mimes: String) = ClipDescription("x", mimes)

    private fun contentUri(n: Int) = Uri.parse("content://com.example.pics/img$n.jpg")

    @Test
    fun sendMultipleWithStreamsInClipDataReturnsAllThreeInOrder() {
        // The headline Google-Photos shape: EXTRA_STREAM carries only the first
        // URI while intent.clipData carries the whole batch.
        val u1 = contentUri(1)
        val u2 = contentUri(2)
        val u3 = contentUri(3)
        val clip = ClipData(desc("image/*"), ClipData.Item(u1)).apply {
            addItem(ClipData.Item(u2))
            addItem(ClipData.Item(u3))
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(u1))
            setClipData(clip)
        }
        val got = ClipboardUriHarvester.harvestIntent(intent).map { it.uri }
        assertEquals(listOf(u1, u2, u3), got)
    }

    @Test
    fun topLevelClipDataSurfacesEveryDirectUri() {
        val clip = ClipData(desc("image/*"), ClipData.Item(contentUri(1))).apply {
            addItem(ClipData.Item(contentUri(2)))
            addItem(ClipData.Item(contentUri(3)))
            addItem(ClipData.Item(contentUri(4)))
        }
        val got = ClipboardUriHarvester.harvestClipData(clip).map { it.uri }
        assertEquals(listOf(contentUri(1), contentUri(2), contentUri(3), contentUri(4)), got)
    }

    @Test
    fun nestedClipDataStreamsDataAndDuplicatesKeepFirstSeenOrder() {
        val u1 = contentUri(1)
        val u2 = contentUri(2)
        val u3 = contentUri(3)
        val u4 = contentUri(4)
        val nested = ClipData(desc("image/*"), ClipData.Item(u3)).apply {
            addItem(ClipData.Item(u2)) // duplicate of outer u2
            addItem(ClipData.Item(u4))
        }
        val nestedIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            data = u4 // duplicate of nested u4
            putExtra(Intent.EXTRA_STREAM, u1) // duplicate of outer u1
            setClipData(nested)
        }
        val outer = ClipData(desc("image/*"), ClipData.Item(u1)).apply {
            addItem(ClipData.Item(u2))
            addItem(ClipData.Item(nestedIntent))
        }
        val got = ClipboardUriHarvester.harvestClipData(outer).map { it.uri }
        assertEquals(listOf(u1, u2, u3, u4), got)
    }

    @Test
    fun mimeHintsFollowTheirOwnSource() {
        val pngClip = ClipData(desc("image/png"), ClipData.Item(contentUri(1)))
        val got = ClipboardUriHarvester.harvestClipData(pngClip)
        assertEquals(1, got.size)
        assertTrue(got[0].mimeHints.contains("image/png"))

        val jpegIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, contentUri(9))
        }
        val gotIntent = ClipboardUriHarvester.harvestIntent(jpegIntent)
        assertEquals(1, gotIntent.size)
        assertTrue(gotIntent[0].mimeHints.contains("image/jpeg"))
        assertEquals(CandidateSource.INTENT_STREAM, gotIntent[0].source)
    }

    @Test
    fun ownStickerProviderIsDroppedAbsolutely() {
        val own = Uri.parse("content://com.hkm.stickhub.stickerprovider/stickers/7.png")
        val clip = ClipData(desc("image/*"), ClipData.Item(own)).apply {
            addItem(ClipData.Item(contentUri(1)))
        }
        val got = ClipboardUriHarvester.harvestClipData(clip).map { it.uri }
        assertEquals(listOf(contentUri(1)), got)
    }

    @Test
    fun cyclicNestedIntentsTerminate() {
        val loop = Intent(Intent.ACTION_SEND)
        val item = ClipData.Item(loop)
        val clip = ClipData(desc("image/*"), item)
        loop.setClipData(clip)
        // Must return, not spin forever; the envelope itself is not an image.
        val got = ClipboardUriHarvester.harvestClipData(clip, maxDepth = 4)
        assertTrue(got.isEmpty())
    }

    @Test
    fun googleStyleShareMultipleKeepsOrder() {
        val uris = (21..25).map { Uri.parse("content://com.google.android.apps.photos.contentprovider/m_$it") }
        val clip = ClipData(desc("image/*"), ClipData.Item(uris[0]))
        uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            setClipData(clip)
        }
        assertEquals(uris, ClipboardUriHarvester.harvestIntent(intent).map { it.uri })
    }
}
