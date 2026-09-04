package com.hkm.stickhub

import com.hkm.stickhub.service.OverlayThumbnailPolicy
import org.junit.Assert.*
import org.junit.Test

class OverlayThumbnailPolicyTest {
    @Test
    fun panoramicSourcesAreDownsampledByTheirLongestAxis() {
        assertEquals(64, OverlayThumbnailPolicy.sampleSize(16384, 128, 160))
        assertEquals(64, OverlayThumbnailPolicy.sampleSize(128, 16384, 160))
    }

    @Test
    fun regularThumbnailsStayWithinTwiceTheirRequestedLongestSide() {
        for (size in listOf(512, 1024, 4096)) {
            val sample = OverlayThumbnailPolicy.sampleSize(size, size, 160)
            assertTrue(size / sample < 320)
            assertTrue(size / sample >= 160)
        }
    }

    @Test
    fun missingBoundsAndSmallImagesUseSafeSampling() {
        assertEquals(1, OverlayThumbnailPolicy.sampleSize(-1, -1, 160))
        assertEquals(1, OverlayThumbnailPolicy.sampleSize(64, 32, 160))
        assertTrue(OverlayThumbnailPolicy.sampleSize(Int.MAX_VALUE, 1, 1) > 0)
    }
}
