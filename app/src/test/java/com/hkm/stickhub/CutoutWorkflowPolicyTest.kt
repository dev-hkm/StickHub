package com.hkm.stickhub

import com.hkm.stickhub.data.cutout.CutoutStartDecision
import com.hkm.stickhub.data.cutout.CutoutInstallDecision
import com.hkm.stickhub.data.cutout.CutoutInstallStatus
import com.hkm.stickhub.data.cutout.CutoutWorkflowPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CutoutWorkflowPolicyTest {

    @Test
    fun unreadableInputTransitionsToExplicitFailureInsteadOfIndefinitePreparing() {
        val decision = CutoutWorkflowPolicy.afterDecode(bitmapWasDecoded = false)

        assertTrue(decision is CutoutStartDecision.DecodeFailed)
        assertEquals("We couldn't read that image. Pick a supported photo and try again.", (decision as CutoutStartDecision.DecodeFailed).message)
    }

    @Test
    fun decodedInputContinuesToModelProcessing() {
        assertEquals(CutoutStartDecision.StartProcessing, CutoutWorkflowPolicy.afterDecode(bitmapWasDecoded = true))
    }

    @Test
    fun installRequestAcknowledgementDoesNotStartAnalysisBeforeCompletion() {
        assertEquals(
            CutoutInstallDecision.WaitForCompletion,
            CutoutWorkflowPolicy.afterInstallRequestAcknowledged(modulesAlreadyInstalled = false)
        )
        assertEquals(
            CutoutInstallDecision.ProceedToAnalysis,
            CutoutWorkflowPolicy.afterInstallStatus(CutoutInstallStatus.Completed)
        )
    }

    @Test
    fun failedOrCanceledInstallAlwaysHasAnExplicitTerminalError() {
        assertTrue(
            CutoutWorkflowPolicy.afterInstallStatus(CutoutInstallStatus.Failed) is CutoutInstallDecision.Failed
        )
        assertTrue(
            CutoutWorkflowPolicy.afterInstallStatus(CutoutInstallStatus.Canceled) is CutoutInstallDecision.Failed
        )
    }
}
