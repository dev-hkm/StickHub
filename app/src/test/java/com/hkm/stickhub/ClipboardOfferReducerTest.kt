package com.hkm.stickhub

import android.net.Uri
import com.hkm.stickhub.util.BatchOrigin
import com.hkm.stickhub.util.CandidateSource
import com.hkm.stickhub.util.ClipboardBatchFactory
import com.hkm.stickhub.util.ClipboardBatchSnapshot
import com.hkm.stickhub.util.ClipboardCandidate
import com.hkm.stickhub.util.ClipboardOfferReducer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * C5/C6/C7: clipboard lifecycle determinism, driven without any clock or
 * coroutine so out-of-order completions are exact.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ClipboardOfferReducerTest {

    private val reducer = ClipboardOfferReducer()

    private fun cand(n: Int) = ClipboardCandidate(
        uri = Uri.parse("content://x/$n"),
        stableKey = "content://x/$n",
        source = CandidateSource.DIRECT_URI,
        itemIndex = n,
        mimeHints = listOf("image/*")
    )

    private fun snap(generation: Long, stamp: Long, vararg ids: Int): ClipboardBatchSnapshot {
        val keys = ids.map { "content://x/$it" }
        return ClipboardBatchSnapshot(
            generation = generation,
            sourceItemCount = ids.size,
            candidates = ids.map { cand(it) },
            rejected = emptyList(),
            stamp = stamp,
            fingerprint = ClipboardBatchFactory.fingerprint(stamp, keys),
            origin = BatchOrigin.CLIPBOARD
        )
    }

    @Test
    fun lateSlowScanNeverOverwritesFasterNewerBatch() {
        // Scan A (gen 1, one image) starts first; scan B (gen 2, three images)
        // starts later but completes first; A completes last and must lose.
        var state = ClipboardOfferReducer.State()
        val (s1, e1) = reducer.reduce(state, ClipboardOfferReducer.Event.ScanArrived(snap(1, 100, 1)))
        assertTrue(e1 is ClipboardOfferReducer.Effect.Show)
        state = s1
        val (s2, e2) = reducer.reduce(state, ClipboardOfferReducer.Event.ScanArrived(snap(2, 200, 1, 2, 3)))
        assertTrue(e2 is ClipboardOfferReducer.Effect.Show)
        state = s2
        val (s3, e3) = reducer.reduce(state, ClipboardOfferReducer.Event.ScanArrived(snap(1, 100, 1)))
        assertTrue(e3 is ClipboardOfferReducer.Effect.Ignore)
        assertEquals(listOf("content://x/1", "content://x/2", "content://x/3"),
            s3.current!!.candidates.map { it.stableKey })
    }

    @Test
    fun sameTimestampWithDifferentCandidatesIsStillNew() {
        var state = ClipboardOfferReducer.State()
        val (s1, _) = reducer.reduce(state, ClipboardOfferReducer.Event.ScanArrived(snap(1, 500, 1)))
        val (s2, e2) = reducer.reduce(s1, ClipboardOfferReducer.Event.ScanArrived(snap(2, 500, 1, 2)))
        assertTrue(e2 is ClipboardOfferReducer.Effect.Show)
        assertEquals(2, s2.current!!.candidates.size)
    }

    @Test
    fun identicalRescanIsIgnored() {
        var state = ClipboardOfferReducer.State()
        val (s1, _) = reducer.reduce(state, ClipboardOfferReducer.Event.ScanArrived(snap(1, 500, 1, 2)))
        val (_, e2) = reducer.reduce(s1, ClipboardOfferReducer.Event.ScanArrived(snap(2, 500, 1, 2)))
        assertTrue(e2 is ClipboardOfferReducer.Effect.Ignore)
    }

    @Test
    fun changeWhileReviewOpenIsHeldNotLost() {
        var state = ClipboardOfferReducer.State()
        val (s1, _) = reducer.reduce(state, ClipboardOfferReducer.Event.ScanArrived(snap(1, 100, 1)))
        val (s2, _) = reducer.reduce(s1, ClipboardOfferReducer.Event.ReviewOpened)
        // A new clipboard lands mid-review.
        val (s3, e3) = reducer.reduce(s2, ClipboardOfferReducer.Event.ScanArrived(snap(2, 200, 7, 8)))
        assertTrue(e3 is ClipboardOfferReducer.Effect.HoldPending)
        // The open sheet is untouched...
        assertEquals(listOf("content://x/1"), s3.current!!.candidates.map { it.stableKey })
        assertEquals(listOf("content://x/7", "content://x/8"), s3.pending!!.candidates.map { it.stableKey })
        // ...and the held offer surfaces once the sheet closes.
        val (s4, e4) = reducer.reduce(s3, ClipboardOfferReducer.Event.ReviewClosed)
        assertTrue(e4 is ClipboardOfferReducer.Effect.Show)
        assertEquals(listOf("content://x/7", "content://x/8"), s4.current!!.candidates.map { it.stableKey })
        assertNull(s4.pending)
    }
}
