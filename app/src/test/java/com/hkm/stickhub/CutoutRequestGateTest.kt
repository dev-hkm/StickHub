package com.hkm.stickhub

import com.hkm.stickhub.data.cutout.CutoutRequestGate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CutoutRequestGateTest {
    @Test
    fun resultBuiltBeforeResetCannotPublishAfterReset() {
        val gate = CutoutRequestGate()
        var visible = "idle"
        val old = gate.begin { visible = "analyzing old" }
        assertTrue(gate.isCurrent(old))
        gate.begin { visible = "idle" }
        assertFalse(gate.publish(old) { visible = "old candidates" })
        assertEquals("idle", visible)
    }

    @Test
    fun latestRequestKeepsItsResultWhenAnOlderCallbackArrives() {
        val gate = CutoutRequestGate()
        var visible = "idle"
        val old = gate.begin {}
        val latest = gate.begin {}
        assertTrue(gate.publish(latest) { visible = "new candidates" })
        assertFalse(gate.publish(old) { visible = "old error" })
        assertEquals("new candidates", visible)
    }
}
