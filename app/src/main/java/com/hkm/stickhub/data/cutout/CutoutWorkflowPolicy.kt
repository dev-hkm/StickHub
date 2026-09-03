package com.hkm.stickhub.data.cutout

/**
 * Small, Android-free transition policy for the cutout startup/install flow.
 *
 * Keeping these transitions explicit makes it impossible for an unreadable image or an
 * acknowledged-but-still-downloading ML Kit module to be rendered as an endless loader.
 */
sealed interface CutoutStartDecision {
    data object StartProcessing : CutoutStartDecision
    data class DecodeFailed(val message: String) : CutoutStartDecision
}

sealed interface CutoutInstallDecision {
    data object WaitForCompletion : CutoutInstallDecision
    data object ProceedToAnalysis : CutoutInstallDecision
    data class Failed(val message: String) : CutoutInstallDecision
}

enum class CutoutInstallStatus {
    Pending,
    Downloading,
    DownloadPaused,
    Installing,
    Completed,
    Failed,
    Canceled,
    Unknown
}

object CutoutWorkflowPolicy {
    private const val DecodeFailureMessage =
        "We couldn't read that image. Pick a supported photo and try again."

    fun afterDecode(bitmapWasDecoded: Boolean): CutoutStartDecision {
        return if (bitmapWasDecoded) {
            CutoutStartDecision.StartProcessing
        } else {
            CutoutStartDecision.DecodeFailed(DecodeFailureMessage)
        }
    }

    /**
     * installModules() only confirms that a request was accepted. It is safe to analyze only
     * when the response says the module was already present or a listener reports completion.
     */
    fun afterInstallRequestAcknowledged(modulesAlreadyInstalled: Boolean): CutoutInstallDecision {
        return if (modulesAlreadyInstalled) {
            CutoutInstallDecision.ProceedToAnalysis
        } else {
            CutoutInstallDecision.WaitForCompletion
        }
    }

    fun afterInstallStatus(status: CutoutInstallStatus): CutoutInstallDecision {
        return when (status) {
            CutoutInstallStatus.Completed -> CutoutInstallDecision.ProceedToAnalysis
            CutoutInstallStatus.Failed -> CutoutInstallDecision.Failed(
                "The on-device cutter model could not be installed. Check your connection and retry."
            )
            CutoutInstallStatus.Canceled -> CutoutInstallDecision.Failed(
                "The on-device cutter model download was canceled. Please retry."
            )
            CutoutInstallStatus.Unknown -> CutoutInstallDecision.Failed(
                "Google Play services returned an unknown model-install state. Please retry."
            )
            CutoutInstallStatus.Pending,
            CutoutInstallStatus.Downloading,
            CutoutInstallStatus.DownloadPaused,
            CutoutInstallStatus.Installing -> CutoutInstallDecision.WaitForCompletion
        }
    }
}
