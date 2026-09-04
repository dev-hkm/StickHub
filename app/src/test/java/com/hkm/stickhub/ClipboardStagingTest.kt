package com.hkm.stickhub

import android.content.Context
import android.net.Uri
import com.hkm.stickhub.util.BatchOrigin
import com.hkm.stickhub.util.CandidateSource
import com.hkm.stickhub.util.ClipboardBatchFactory
import com.hkm.stickhub.util.ClipboardBatchSnapshot
import com.hkm.stickhub.util.ClipboardCandidate
import com.hkm.stickhub.util.ClipboardStager
import com.hkm.stickhub.util.StageFailure
import com.hkm.stickhub.util.StagedClipboardItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.io.InputStream

/**
 * C4/C8: staging opens each source URI exactly once into app-private temp
 * files using REAL container magic bytes — no `file:///sdcard` fiction.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClipboardStagingTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    // Real leading bytes per container; the payload after the header is filler.
    private fun png(size: Int = 4096) =
        byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) + ByteArray(size - 8)
    private fun jpg(size: Int = 4096) =
        byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()) + ByteArray(size - 4)
    private fun webp(size: Int = 4096) =
        "RIFF".toByteArray() + ByteArray(4) + "WEBP".toByteArray() + ByteArray(size - 12)
    private fun corrupt() = "this is not an image at all".toByteArray()

    private class FakeOpener(
        val data: MutableMap<String, ByteArray>,
        val denied: MutableSet<String> = mutableSetOf(),
        val opens: MutableMap<String, Int> = mutableMapOf()
    ) : ClipboardStager.UriOpener {
        override fun open(uri: Uri): InputStream? {
            val key = uri.toString()
            opens[key] = (opens[key] ?: 0) + 1
            if (key in denied) throw SecurityException("grant revoked")
            return data[key]?.inputStream()
        }
    }

    private fun cand(n: Int) = ClipboardCandidate(
        uri = Uri.parse("content://pics/$n"),
        stableKey = "content://pics/$n",
        source = CandidateSource.DIRECT_URI,
        itemIndex = n,
        mimeHints = emptyList()
    )

    private fun snapshot(vararg ids: Int, generation: Long = 1): ClipboardBatchSnapshot {
        val harvested = ids.map { cand(it) }
        return ClipboardBatchFactory.build(
            generation = generation,
            origin = BatchOrigin.CLIPBOARD,
            sourceItemCount = ids.size,
            stamp = 999L,
            harvested = harvested,
            resolveMimeType = { null } // producer gave no usable MIME: bytes decide
        )
    }

    @Test
    fun eachSourceOpensOnceAndSniffsRealFormats() = runBlocking {
        val opener = FakeOpener(mutableMapOf(
            "content://pics/1" to png(),
            "content://pics/2" to jpg(),
            "content://pics/3" to webp()
        ))
        val stager = ClipboardStager(context, opener)
        val progress = mutableListOf<Pair<Int, Int>>()
        val staged = stager.stage(snapshot(1, 2, 3)) { done, total -> progress.add(done to total) }

        assertEquals(3, staged.size)
        val ready = staged.filterIsInstance<StagedClipboardItem.Ready>()
        assertEquals(3, ready.size)
        assertEquals(listOf("png", "jpg", "webp"), ready.map { it.extension })
        assertEquals(mapOf("content://pics/1" to 1, "content://pics/2" to 1, "content://pics/3" to 1), opener.opens)
        assertTrue(ready.all { File(it.file.absolutePath).isFile })
        assertEquals(listOf(1 to 3, 2 to 3, 3 to 3), progress)
    }

    @Test
    fun badUriInTheMiddleNeverTruncatesTheBatch() = runBlocking {
        val opener = FakeOpener(
            mutableMapOf("content://pics/1" to png(), "content://pics/3" to jpg()),
            denied = mutableSetOf("content://pics/2")
        )
        val stager = ClipboardStager(context, opener)
        val staged = stager.stage(snapshot(1, 2, 3))
        val ready = staged.filterIsInstance<StagedClipboardItem.Ready>()
        val failed = staged.filterIsInstance<StagedClipboardItem.Failed>()
        assertEquals(2, ready.size)
        assertEquals(listOf("content://pics/1", "content://pics/3"), ready.map { it.candidate.stableKey })
        assertEquals(1, failed.size)
        assertEquals(StageFailure.DENIED, failed.single().reason)
        // No orphaned partials left behind.
        assertTrue(ClipboardStager.stagingDir(context).listFiles().orEmpty().none { it.name.endsWith(".partial") })
    }

    @Test
    fun corruptAndOversizeFailAlone() = runBlocking {
        val opener = FakeOpener(mutableMapOf(
            "content://pics/1" to corrupt(),
            "content://pics/2" to ByteArray(33 * 1024 * 1024), // over the 32 MiB cap
            "content://pics/3" to png()
        ))
        val stager = ClipboardStager(context, opener)
        val staged = stager.stage(snapshot(1, 2, 3))
        val byKey = staged.associateBy { it.candidate.stableKey }
        assertEquals(StageFailure.CORRUPT, (byKey["content://pics/1"] as StagedClipboardItem.Failed).reason)
        assertEquals(StageFailure.TOO_LARGE, (byKey["content://pics/2"] as StagedClipboardItem.Failed).reason)
        assertTrue(byKey["content://pics/3"] is StagedClipboardItem.Ready)
        assertTrue(ClipboardStager.stagingDir(context).listFiles().orEmpty().none { it.name.endsWith(".partial") })
    }

    @Test
    fun stagedFilesSurviveSourceLoss() = runBlocking {
        val opener = FakeOpener(mutableMapOf("content://pics/1" to png(), "content://pics/2" to jpg()))
        val stager = ClipboardStager(context, opener)
        val staged = stager.stage(snapshot(1, 2))
        // The grant dies after staging (provider gone, clipboard replaced...).
        opener.data.clear()
        opener.denied.addAll(listOf("content://pics/1", "content://pics/2"))
        val ready = staged.filterIsInstance<StagedClipboardItem.Ready>()
        assertEquals(2, ready.size)
        assertTrue(ready.all { it.file.isFile && it.file.length() > 0 })
    }

    @Test
    fun startupCleanupRemovesStaleStaging() {
        val dir = ClipboardStager.stagingDir(context)
        File(dir, "stale_clip_stage_1.png").writeBytes(png(64))
        ClipboardStager.cleanupStale(context)
        assertTrue(dir.listFiles().orEmpty().isEmpty())
    }
}
