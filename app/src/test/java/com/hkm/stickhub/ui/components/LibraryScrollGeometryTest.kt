package com.hkm.stickhub.ui.components

import org.junit.Assert.*
import org.junit.Test

class LibraryScrollGeometryTest {
    @Test fun gridCountsRowsInsteadOfCells() {
        val grid = LibraryScrollGeometry(301, 3, 200, 100, 10, 600)
        assertEquals(11200f, grid.contentSize, .01f)
        assertEquals(0 to 0, grid.target(0f))
        val end = grid.target(1f)
        assertEquals(grid.maxOffset, grid.offset(end.first, end.second), 1f)
    }

    @Test fun everyLayoutHasMonotonicRoundTripsAndReachableEnd() {
        for (columns in 1..4) {
            val grid = LibraryScrollGeometry(334, columns, 237, 151, 13, 721, 90)
            var previous = -1f
            for (step in 0..100) {
                val target = grid.target(step / 100f)
                assertTrue(target.first in 0..333)
                val offset = grid.offset(target.first, target.second)
                assertTrue(offset >= previous)
                assertEquals(step / 100f * grid.maxOffset, offset, 1.01f)
                previous = offset
            }
        }
    }

    @Test fun emptyAndShortLibrariesNeverGenerateInvalidTargets() {
        for (count in 0..4) {
            val grid = LibraryScrollGeometry(count, 3, 0, 100, 0, 900)
            assertEquals(0f, grid.maxOffset, 0f)
            assertEquals(0 to 0, grid.target(Float.NaN))
            assertEquals(0 to 0, grid.target(1f))
        }
    }
}
