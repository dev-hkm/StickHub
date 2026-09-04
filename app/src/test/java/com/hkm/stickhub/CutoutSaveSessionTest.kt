package com.hkm.stickhub

import com.hkm.stickhub.data.cutout.CutoutSaveSession
import com.hkm.stickhub.data.cutout.CutoutSaveState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CutoutSaveSessionTest {
    @Test
    fun repeatedTapsWhileSavingOnlyWriteOnce() = runBlocking {
        val session = CutoutSaveSession()
        val started = CompletableDeferred<Unit>()
        val finish = CompletableDeferred<Unit>()
        var writes = 0
        val first = async {
            session.save {
                writes++
                started.complete(Unit)
                finish.await()
                true
            }
        }
        started.await()
        assertEquals(CutoutSaveState.Saving, session.state.value)
        assertFalse(session.save { writes++; true })
        finish.complete(Unit)
        assertTrue(first.await())
        assertFalse(session.save { writes++; true })
        assertEquals(1, writes)
    }

    @Test
    fun failedWriteAllowsRetryWithoutDiscardingTheSession() = runBlocking {
        val session = CutoutSaveSession()
        assertFalse(session.save { false })
        assertEquals(CutoutSaveState.Failed, session.state.value)
        assertTrue(session.save { true })
        assertEquals(CutoutSaveState.Saved, session.state.value)
    }

    @Test
    fun thrownStorageErrorAllowsRetry() = runBlocking {
        val session = CutoutSaveSession()
        assertFalse(session.save { throw java.io.IOException("Disk full") })
        assertEquals(CutoutSaveState.Failed, session.state.value)
        assertTrue(session.save { true })
    }
}
