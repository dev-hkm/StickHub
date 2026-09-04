package com.hkm.stickhub

import com.hkm.stickhub.data.cutout.MaskContourExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaskContourExtractorTest {
    @Test
    fun rectangularMaskProducesOnlyOuterEdges() {
        val segments = MaskContourExtractor.extract(
            mask = FloatArray(3 * 3) { 0.9f },
            width = 3,
            height = 3,
            startX = 10,
            startY = 20,
            toNormalizedX = { it / 100f },
            toNormalizedY = { it / 100f }
        )

        assertEquals(12, segments.size)
        assertTrue(segments.all { it.start.x in 0.1f..0.14f || it.end.x in 0.1f..0.14f || it.start.x in 0.12f..0.14f || it.end.x in 0.12f..0.14f })
        assertTrue(segments.any { it.start.y == 0.20f && it.end.y == 0.20f })
        assertTrue(segments.any { it.start.y == 0.23f && it.end.y == 0.23f })
    }

    @Test
    fun lowConfidencePixelsDoNotCreateAnOutline() {
        val segments = MaskContourExtractor.extract(
            mask = FloatArray(2 * 2) { 0.1f },
            width = 2,
            height = 2,
            startX = 0,
            startY = 0,
            toNormalizedX = { it },
            toNormalizedY = { it }
        )
        assertTrue(segments.isEmpty())
    }
}
