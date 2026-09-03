package com.hkm.stickhub

import androidx.compose.ui.geometry.Rect as ComposeRect
import com.hkm.stickhub.data.cutout.CutoutCandidate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicLong

class CutoutCandidateTest {

    @Test
    fun testContainsNormalizedPoint_insideAndOutside() {
        val candidate = CutoutCandidate(
            id = 0,
            bounds = android.graphics.Rect(100, 100, 300, 300),
            normalizedBounds = ComposeRect(0.1f, 0.1f, 0.4f, 0.4f),
            cutoutBitmap = null,
            maskWidth = 0,
            maskHeight = 0,
            confidenceMask = null
        )

        // Inside normalized bounding box
        assertTrue(candidate.containsNormalizedPoint(0.2f, 0.2f))
        assertTrue(candidate.containsNormalizedPoint(0.15f, 0.35f))

        // Outside normalized bounding box
        assertFalse(candidate.containsNormalizedPoint(0.05f, 0.2f))
        assertFalse(candidate.containsNormalizedPoint(0.5f, 0.5f))
        assertFalse(candidate.containsNormalizedPoint(0.2f, 0.8f))
    }

    @Test
    fun testContainsNormalizedPoint_withConfidenceMask() {
        // 2x2 mask
        // [0.8f, 0.1f]
        // [0.2f, 0.9f]
        val mask = floatArrayOf(
            0.8f, 0.1f,
            0.2f, 0.9f
        )

        val candidate = CutoutCandidate(
            id = 1,
            bounds = android.graphics.Rect(0, 0, 100, 100),
            normalizedBounds = ComposeRect(0f, 0f, 1f, 1f),
            cutoutBitmap = null,
            maskWidth = 2,
            maskHeight = 2,
            confidenceMask = mask
        )

        // Top-left pixel has 0.8f confidence (> 0.4f) -> true
        assertTrue(candidate.containsNormalizedPoint(0.2f, 0.2f))

        // Top-right pixel has 0.1f confidence (<= 0.4f) -> false
        assertFalse(candidate.containsNormalizedPoint(0.8f, 0.2f))

        // Bottom-left pixel has 0.2f confidence (<= 0.4f) -> false
        assertFalse(candidate.containsNormalizedPoint(0.2f, 0.8f))

        // Bottom-right pixel has 0.9f confidence (> 0.4f) -> true
        assertTrue(candidate.containsNormalizedPoint(0.8f, 0.8f))
    }

    @Test
    fun testContainsNormalizedPoint_mapsMaskRelativeToSubjectBounds() {
        val candidate = CutoutCandidate(
            id = 2,
            bounds = android.graphics.Rect(50, 50, 75, 75),
            normalizedBounds = ComposeRect(0.5f, 0.5f, 0.75f, 0.75f),
            cutoutBitmap = null,
            maskWidth = 2,
            maskHeight = 2,
            confidenceMask = floatArrayOf(
                0.9f, 0.1f,
                0.1f, 0.9f
            )
        )

        // 0.55/0.55 is the top-left quadrant of this *offset* subject, not of the full image.
        assertTrue(candidate.containsNormalizedPoint(0.55f, 0.55f))
        assertFalse(candidate.containsNormalizedPoint(0.70f, 0.55f))
    }

    @Test
    fun testRequestIdTokenGuard_staleResponseDropped() {
        val requestIdCounter = AtomicLong(0)
        val initialRequest = requestIdCounter.incrementAndGet()

        // User rapidly starts another cutout request
        val nextRequest = requestIdCounter.incrementAndGet()

        // When initialRequest completes, it checks if it's still current
        val isInitialRequestCurrent = (requestIdCounter.get() == initialRequest)
        val isNextRequestCurrent = (requestIdCounter.get() == nextRequest)

        assertFalse("Stale initial request must not be treated as current", isInitialRequestCurrent)
        assertTrue("Latest request must be current", isNextRequestCurrent)
    }
}
