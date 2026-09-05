package com.hkm.stickhub

import com.hkm.stickhub.util.AsyncActionGate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AsyncActionGateTest {
    @Test
    fun onlyOneCopyOperationCanRunUntilThePreviousOneFinishes() {
        val gate = AsyncActionGate()

        assertTrue(gate.tryAcquire())
        assertFalse(gate.tryAcquire())

        gate.release()

        assertTrue(gate.tryAcquire())
    }

    @Test
    fun releaseIsIdempotentAndNeverLeavesTheGateLocked() {
        val gate = AsyncActionGate()

        gate.tryAcquire()
        gate.release()
        gate.release()

        assertTrue(gate.tryAcquire())
    }
}
