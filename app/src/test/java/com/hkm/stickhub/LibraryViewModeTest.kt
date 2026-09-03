package com.hkm.stickhub

import com.hkm.stickhub.ui.library.StickerLibraryViewMode
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryViewModeTest {

    @Test
    fun testDefaultFallbackOnNullOrBlank() {
        assertEquals(StickerLibraryViewMode.STANDARD_GRID, StickerLibraryViewMode.fromString(null))
        assertEquals(StickerLibraryViewMode.STANDARD_GRID, StickerLibraryViewMode.fromString(""))
        assertEquals(StickerLibraryViewMode.STANDARD_GRID, StickerLibraryViewMode.fromString("   "))
        assertEquals(StickerLibraryViewMode.STANDARD_GRID, StickerLibraryViewMode.fromString("unknown_mode"))
    }

    @Test
    fun testParsingAllViewModesCaseInsensitive() {
        assertEquals(StickerLibraryViewMode.COMPACT_GRID, StickerLibraryViewMode.fromString("COMPACT_GRID"))
        assertEquals(StickerLibraryViewMode.COMPACT_GRID, StickerLibraryViewMode.fromString("compact_grid"))
        assertEquals(StickerLibraryViewMode.STANDARD_GRID, StickerLibraryViewMode.fromString("STANDARD_GRID"))
        assertEquals(StickerLibraryViewMode.STANDARD_GRID, StickerLibraryViewMode.fromString("standard_grid"))
        assertEquals(StickerLibraryViewMode.LARGE_GRID, StickerLibraryViewMode.fromString("LARGE_GRID"))
        assertEquals(StickerLibraryViewMode.LARGE_GRID, StickerLibraryViewMode.fromString("large_grid"))
        assertEquals(StickerLibraryViewMode.LIST, StickerLibraryViewMode.fromString("LIST"))
        assertEquals(StickerLibraryViewMode.LIST, StickerLibraryViewMode.fromString("list"))
    }
}
