package com.hkm.stickhub

import android.graphics.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import com.hkm.stickhub.data.cutout.CutoutCandidate
import com.hkm.stickhub.data.cutout.CutoutSelectionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CutoutSelectionPolicyTest {

    @Test
    fun selectsCandidateWithStrongestConfidenceAtTouchPoint() {
        val broad = candidate(
            id = 1,
            bounds = ComposeRect(0.20f, 0.20f, 0.80f, 0.80f),
            maskWidth = 1,
            maskHeight = 1,
            confidenceMask = floatArrayOf(0.72f)
        )
        val precise = candidate(
            id = 2,
            bounds = ComposeRect(0.35f, 0.35f, 0.65f, 0.65f),
            maskWidth = 1,
            maskHeight = 1,
            confidenceMask = floatArrayOf(0.96f)
        )

        assertEquals(
            precise,
            CutoutSelectionPolicy.selectAtNormalizedPoint(
                listOf(broad, precise),
                Offset(0.5f, 0.5f)
            )
        )
    }

    @Test
    fun rejectsBackgroundAndLowConfidenceTouches() {
        val subject = candidate(
            id = 3,
            bounds = ComposeRect(0.10f, 0.10f, 0.90f, 0.90f),
            maskWidth = 2,
            maskHeight = 1,
            confidenceMask = floatArrayOf(0.95f, 0.1f)
        )

        assertNull(
            CutoutSelectionPolicy.selectAtNormalizedPoint(
                listOf(subject),
                Offset(0.75f, 0.5f)
            )
        )
        assertNull(
            CutoutSelectionPolicy.selectAtNormalizedPoint(
                listOf(subject),
                Offset(0.95f, 0.5f)
            )
        )
    }

    @Test
    fun tiesPreferSmallerSubjectThenStableId() {
        val larger = candidate(
            id = 1,
            bounds = ComposeRect(0.10f, 0.10f, 0.90f, 0.90f),
            maskWidth = 1,
            maskHeight = 1,
            confidenceMask = floatArrayOf(0.8f)
        )
        val smaller = candidate(
            id = 2,
            bounds = ComposeRect(0.30f, 0.30f, 0.70f, 0.70f),
            maskWidth = 1,
            maskHeight = 1,
            confidenceMask = floatArrayOf(0.8f)
        )

        assertEquals(
            smaller,
            CutoutSelectionPolicy.selectAtNormalizedPoint(
                listOf(larger, smaller),
                Offset(0.5f, 0.5f)
            )
        )
    }

    @Test
    fun mapsTouchToNormalizedImageSpaceAndRejectsLetterbox() {
        assertEquals(
            Offset(0.5f, 0.25f),
            CutoutSelectionPolicy.normalizedPointForImageTouch(
                touch = Offset(150f, 75f),
                imageLeft = 50f,
                imageTop = 25f,
                imageWidth = 200f,
                imageHeight = 200f
            )
        )
        assertNull(
            CutoutSelectionPolicy.normalizedPointForImageTouch(
                touch = Offset(40f, 75f),
                imageLeft = 50f,
                imageTop = 25f,
                imageWidth = 200f,
                imageHeight = 200f
            )
        )
    }

    private fun candidate(
        id: Int,
        bounds: ComposeRect,
        maskWidth: Int,
        maskHeight: Int,
        confidenceMask: FloatArray
    ) = CutoutCandidate(
        id = id,
        bounds = Rect(0, 0, 100, 100),
        normalizedBounds = bounds,
        maskWidth = maskWidth,
        maskHeight = maskHeight,
        confidenceMask = confidenceMask
    )
}
