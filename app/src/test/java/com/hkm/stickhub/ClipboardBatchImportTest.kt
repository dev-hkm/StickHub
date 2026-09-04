package com.hkm.stickhub

import android.content.Context
import android.net.Uri
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.util.BatchOrigin
import com.hkm.stickhub.util.CandidateSource
import com.hkm.stickhub.util.ClipboardBatchFactory
import com.hkm.stickhub.util.ClipboardCandidate
import com.hkm.stickhub.util.ClipboardStager
import com.hkm.stickhub.util.StagedClipboardItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.io.InputStream

/**
 * C9: the batch repository API imports staged files with per-item outcomes,
 * a single lock, a single refresh, and retry that only touches failures.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClipboardBatchImportTest {

    private lateinit var context: Context
    private lateinit var repository: StickerRepository

    private fun png(seed: Byte) =
        byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, seed) + ByteArray(2048)

    private class FakeOpener(val data: MutableMap<String, ByteArray>) : ClipboardStager.UriOpener {
        override fun open(uri: Uri): InputStream? = data[uri.toString()]?.inputStream()
    }

    private fun cand(n: Int) = ClipboardCandidate(
        uri = Uri.parse("content://pics/$n"),
        stableKey = "content://pics/$n",
        source = CandidateSource.DIRECT_URI,
        itemIndex = n,
        mimeHints = emptyList()
    )

    @Before fun setUp() = runBlocking {
        context = RuntimeEnvironment.getApplication()
        repository = StickerRepository(context)
        repository.refresh()
        assertTrue(repository.stickersFlow.value.isEmpty())
    }

    @Test
    fun mixedBatchReportsEveryOutcomeAndRefreshesOnce() = runBlocking {
        val opener = FakeOpener(mutableMapOf(
            "content://pics/1" to png(1),
            "content://pics/2" to png(2),
            "content://pics/3" to png(3)
        ))
        // Pre-seed an identical copy of image 1: it must come back Duplicate.
        repository.saveStickerFromStream(png(1).inputStream())
        var refreshes = 0
        repository.refreshListener = { refreshes++ }

        val snapshot = ClipboardBatchFactory.build(
            generation = 1, origin = BatchOrigin.CLIPBOARD, sourceItemCount = 3,
            stamp = 1L, harvested = listOf(cand(1), cand(2), cand(3)), resolveMimeType = { null }
        )
        val staged = ClipboardStager(context, opener).stage(snapshot)
            .filterIsInstance<StagedClipboardItem.Ready>()
        assertEquals(3, staged.size)

        val result = repository.importStagedClipboardBatch(staged)
        assertEquals(2, result.saved.size)
        assertEquals(1, result.duplicates.size)
        assertTrue(result.failed.isEmpty())
        assertEquals("one refresh per batch, not per sticker", 1, refreshes)

        // Consumed staged temps are gone; library files exist.
        assertTrue(staged.all { !File(it.file.absolutePath).exists() })
        assertTrue(result.saved.all { File(it.filePath).isFile })
        repository.refreshListener = null
    }

    @Test
    fun vanishedStagedFileFailsAloneAndRetrySucceeds() = runBlocking {
        val opener = FakeOpener(mutableMapOf("content://pics/7" to png(7)))
        val snapshot = ClipboardBatchFactory.build(
            generation = 1, origin = BatchOrigin.CLIPBOARD, sourceItemCount = 1,
            stamp = 1L, harvested = listOf(cand(7)), resolveMimeType = { null }
        )
        val ready = ClipboardStager(context, opener).stage(snapshot)
            .filterIsInstance<StagedClipboardItem.Ready>()
            .single()
        // The staged temp vanishes before commit (cleaner race, disk pressure...).
        assertTrue(ready.file.delete())

        val failed = repository.importStagedClipboardBatch(listOf(ready))
        assertTrue(failed.saved.isEmpty())
        assertEquals(1, failed.failed.size)

        // Retry with a re-staged file touches only the failure and saves it.
        val restored = ClipboardStager(context, opener).stage(snapshot)
            .filterIsInstance<StagedClipboardItem.Ready>()
            .single()
        val retried = repository.importStagedClipboardBatch(listOf(restored))
        assertEquals(1, retried.saved.size)
        assertTrue(retried.failed.isEmpty())
        assertTrue(File(retried.saved.single().filePath).isFile)
    }
}
