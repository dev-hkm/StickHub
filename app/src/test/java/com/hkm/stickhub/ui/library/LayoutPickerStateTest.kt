package com.hkm.stickhub.ui.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutPickerStateTest {

    @Test
    fun testSelectNewModeSetsPendingAndDefersCommitUntilDismissComplete() {
        val state = LayoutPickerState(StickerLibraryViewMode.STANDARD_GRID)
        state.open()
        assertTrue(state.isVisible)
        assertFalse(state.isDismissing)
        assertEquals(StickerLibraryViewMode.STANDARD_GRID, state.committedMode)

        // Select new mode: COMPACT_GRID
        val shouldMutate = state.select(StickerLibraryViewMode.COMPACT_GRID)
        assertTrue("Selecting a different mode must signal mutation", shouldMutate)
        assertEquals(StickerLibraryViewMode.COMPACT_GRID, state.pendingMode)
        assertTrue(state.isDismissing)
        // Grid mode is NOT committed yet while sheet is animating exit!
        assertEquals(StickerLibraryViewMode.STANDARD_GRID, state.committedMode)

        // Dismiss animation finishes
        val didChange = state.onDismissComplete()
        assertTrue(didChange)
        assertFalse(state.isVisible)
        assertFalse(state.isDismissing)
        assertNull(state.pendingMode)
        assertEquals(StickerLibraryViewMode.COMPACT_GRID, state.committedMode)
    }

    @Test
    fun testSelectSameModeDoesNotMutateOrTriggerChange() {
        val state = LayoutPickerState(StickerLibraryViewMode.LARGE_GRID)
        state.open()

        val shouldMutate = state.select(StickerLibraryViewMode.LARGE_GRID)
        assertFalse("Selecting active mode must not signal mutation", shouldMutate)
        assertNull("Pending mode must remain null", state.pendingMode)
        assertTrue("Dismissing should still begin", state.isDismissing)
        assertEquals(StickerLibraryViewMode.LARGE_GRID, state.committedMode)

        val didChange = state.onDismissComplete()
        assertFalse("No change committed", didChange)
        assertEquals(StickerLibraryViewMode.LARGE_GRID, state.committedMode)
    }

    @Test
    fun testInputIsLockedWhileDismissing() {
        val state = LayoutPickerState(StickerLibraryViewMode.STANDARD_GRID)
        state.open()

        state.select(StickerLibraryViewMode.LIST)
        assertTrue(state.isDismissing)

        // Attempt second selection while already dismissing
        val secondSelect = state.select(StickerLibraryViewMode.COMPACT_GRID)
        assertFalse("Second select during dismiss must be ignored", secondSelect)
        assertEquals(StickerLibraryViewMode.LIST, state.pendingMode)
    }
}
