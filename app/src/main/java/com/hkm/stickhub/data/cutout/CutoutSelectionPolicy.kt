package com.hkm.stickhub.data.cutout

import androidx.compose.ui.geometry.Offset
import kotlin.math.max

/** Pure hit-testing rules for choosing one ML Kit subject from the source photo. */
object CutoutSelectionPolicy {
    /** Confidence below this value is treated as background for a manual press. */
    const val MIN_CONFIDENCE = 0.4f
    private const val MANUAL_FALLBACK_MIN_CONFIDENCE = 0.12f

    /**
     * Returns the candidate whose confidence is strongest at [point]. If masks
     * overlap, a smaller subject wins an exact confidence tie, then the stable
     * candidate id makes the result deterministic across recompositions.
     */
    fun selectAtNormalizedPoint(
        candidates: List<CutoutCandidate>,
        point: Offset
    ): CutoutCandidate? = selectAtNormalizedPoint(candidates, point.x, point.y)

    fun selectAtNormalizedPoint(
        candidates: List<CutoutCandidate>,
        normalizedX: Float,
        normalizedY: Float
    ): CutoutCandidate? {
        if (candidates.isEmpty() || !normalizedX.isFinite() || !normalizedY.isFinite()) {
            return null
        }
        if (normalizedX !in 0f..1f || normalizedY !in 0f..1f) return null

        return candidates.asSequence()
            .mapNotNull { candidate ->
                val confidence = candidate.confidenceAtNormalizedPoint(normalizedX, normalizedY)
                    ?: return@mapNotNull null
                if (!confidence.isFinite() || confidence < MIN_CONFIDENCE) return@mapNotNull null
                val bounds = candidate.normalizedBounds
                val area = max(0f, bounds.width) * max(0f, bounds.height)
                Hit(candidate, confidence, area)
            }
            .sortedWith(
                compareByDescending<Hit> { it.confidence }
                    .thenBy { it.area }
                    .thenBy { it.candidate.id }
            )
            .firstOrNull()
            ?.candidate
    }

    /**
     * Resolves a deliberate manual long press. The strict confidence path is
     * preferred; the relaxed path only helps when a finger lands on a thin or
     * antialiased edge of a valid subject mask.
     */
    fun selectForManualLongPress(
        candidates: List<CutoutCandidate>,
        point: Offset
    ): CutoutCandidate? {
        selectAtNormalizedPoint(candidates, point)?.let { return it }
        val normalizedX = point.x
        val normalizedY = point.y
        if (candidates.isEmpty() || !normalizedX.isFinite() || !normalizedY.isFinite()) return null
        if (normalizedX !in 0f..1f || normalizedY !in 0f..1f) return null

        return candidates.asSequence()
            .mapNotNull { candidate ->
                val confidence = candidate.confidenceAtNormalizedPoint(normalizedX, normalizedY)
                    ?: return@mapNotNull null
                if (!confidence.isFinite() || confidence < MANUAL_FALLBACK_MIN_CONFIDENCE) {
                    return@mapNotNull null
                }
                val bounds = candidate.normalizedBounds
                val area = max(0f, bounds.width) * max(0f, bounds.height)
                Hit(candidate, confidence, area)
            }
            .sortedWith(
                compareByDescending<Hit> { it.confidence }
                    .thenBy { it.area }
                    .thenBy { it.candidate.id }
            )
            .firstOrNull()
            ?.candidate
    }

    /** Converts a touch in the letterboxed preview into source-image space. */
    fun normalizedPointForImageTouch(
        touch: Offset,
        imageLeft: Float,
        imageTop: Float,
        imageWidth: Float,
        imageHeight: Float
    ): Offset? {
        if (!imageLeft.isFinite() || !imageTop.isFinite() ||
            !imageWidth.isFinite() || !imageHeight.isFinite() ||
            imageWidth <= 0f || imageHeight <= 0f
        ) {
            return null
        }
        val normalizedX = (touch.x - imageLeft) / imageWidth
        val normalizedY = (touch.y - imageTop) / imageHeight
        if (normalizedX !in 0f..1f || normalizedY !in 0f..1f) return null
        return Offset(normalizedX, normalizedY)
    }

    private data class Hit(
        val candidate: CutoutCandidate,
        val confidence: Float,
        val area: Float
    )
}
