package com.hkm.stickhub.data.cutout

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.ui.geometry.Rect as ComposeRect

/** One normalized endpoint of the confidence-mask contour. */
data class CutoutContourPoint(
    val x: Float,
    val y: Float
)

/** A small line segment on the visible edge of a detected subject. */
data class CutoutContourSegment(
    val start: CutoutContourPoint,
    val end: CutoutContourPoint
)

data class CutoutCandidate(
    val id: Int,
    val bounds: Rect,
    val normalizedBounds: ComposeRect,
    val cutoutBitmap: Bitmap? = null,
    val maskWidth: Int,
    val maskHeight: Int,
    val confidenceMask: FloatArray? = null,
    val outlineSegments: List<CutoutContourSegment> = emptyList()
) {
    /** Returns the confidence at a point in source-image normalized space. */
    fun confidenceAtNormalizedPoint(x: Float, y: Float): Float? {
        if (!normalizedBounds.contains(androidx.compose.ui.geometry.Offset(x, y))) return null
        if (confidenceMask != null && maskWidth > 0 && maskHeight > 0) {
            val relativeX = (x - normalizedBounds.left) / normalizedBounds.width
            val relativeY = (y - normalizedBounds.top) / normalizedBounds.height
            val maskX = (relativeX * maskWidth).toInt().coerceIn(0, maskWidth - 1)
            val maskY = (relativeY * maskHeight).toInt().coerceIn(0, maskHeight - 1)
            val index = maskY * maskWidth + maskX
            if (index in confidenceMask.indices) return confidenceMask[index]
        }
        return 1f
    }

    fun containsNormalizedPoint(x: Float, y: Float): Boolean {
        return (confidenceAtNormalizedPoint(x, y) ?: 0f) > 0.4f
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
