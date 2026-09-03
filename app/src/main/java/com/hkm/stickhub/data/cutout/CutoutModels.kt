package com.hkm.stickhub.data.cutout

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.ui.geometry.Rect as ComposeRect

data class CutoutCandidate(
    val id: Int,
    val bounds: Rect,
    val normalizedBounds: ComposeRect,
    val cutoutBitmap: Bitmap? = null,
    val maskWidth: Int,
    val maskHeight: Int,
    val confidenceMask: FloatArray? = null
) {
    fun containsNormalizedPoint(x: Float, y: Float): Boolean {
        if (!normalizedBounds.contains(androidx.compose.ui.geometry.Offset(x, y))) {
            return false
        }
        // If confidence mask is available, test exact mask pixel
        if (confidenceMask != null && maskWidth > 0 && maskHeight > 0) {
            val relativeX = (x - normalizedBounds.left) / normalizedBounds.width
            val relativeY = (y - normalizedBounds.top) / normalizedBounds.height
            val maskX = (relativeX * maskWidth).toInt().coerceIn(0, maskWidth - 1)
            val maskY = (relativeY * maskHeight).toInt().coerceIn(0, maskHeight - 1)
            val index = maskY * maskWidth + maskX
            if (index in confidenceMask.indices) {
                return confidenceMask[index] > 0.4f
            }
        }
        return true
    }
}

sealed interface CutoutState {
    data object Idle : CutoutState
    data object Decoding : CutoutState
    data object CheckingModel : CutoutState
    data class DownloadingModel(
        val progressPercent: Int = 0,
        val statusText: String = "Preparing ML Kit model..."
    ) : CutoutState
    data object Analyzing : CutoutState
    data class TransparentDetected(val bitmap: Bitmap) : CutoutState
    data class CandidatesReady(
        val sourceBitmap: Bitmap,
        val candidates: List<CutoutCandidate>
    ) : CutoutState
    data class NoSubjectFound(val sourceBitmap: Bitmap) : CutoutState
    data class GooglePlayServicesUnavailable(val message: String) : CutoutState
    data class Failed(val reason: String) : CutoutState
}
