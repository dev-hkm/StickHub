package com.hkm.stickhub

import com.hkm.stickhub.ui.library.CategoryDragSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryDragSessionTest {
    @Test
    fun oneGestureCanCrossSeveralCategoriesAndCommitOnlyAtRelease() {
        val drag = CategoryDragSession(listOf("A", "B", "C", "D"))
        assertTrue(drag.start("A"))
        assertTrue(drag.moveTo("B"))
        assertTrue(drag.moveTo("C"))
        assertTrue(drag.moveTo("D"))
        assertEquals("A", drag.draggedKey)
        assertEquals(listOf("B", "C", "D", "A"), drag.finish())
        assertNull(drag.draggedKey)
    }

    @Test
    fun cancellationRestoresOrderWithoutACommit() {
        val original = listOf("A", "B", "C")
        val drag = CategoryDragSession(original)
        drag.start("A")
        drag.moveTo("C")
        drag.cancel()
        assertEquals(original, drag.order)
        assertNull(drag.finish())
    }

    @Test
    fun holdWithoutMovingDoesNotWriteOrder() {
        val drag = CategoryDragSession(listOf("A", "B"))
        drag.start("A")
        assertNull(drag.finish())
    }

    @Test
    fun externalDeleteOfDraggedChipEndsDragSilently() {
        val drag = CategoryDragSession(listOf("A", "B", "C"))
        drag.start("B")
        drag.moveTo("C")
        drag.syncExternal(listOf("A", "C"))
        assertNull(drag.draggedKey)
        assertEquals(listOf("A", "C"), drag.order)
        assertNull(drag.finish())
    }

    @Test
    fun externalChangesPreserveActiveDragBaselineForCancel() {
        val original = listOf("A", "B", "C")
        val drag = CategoryDragSession(original)
        drag.start("A")
        drag.moveTo("C")
        drag.syncExternal(listOf("A", "B", "C", "D"))
        assertEquals(listOf("B", "C", "A", "D"), drag.order)
        drag.cancel()
        assertEquals(original, drag.order)
    }

    @Test
    fun idleSyncAdoptsFlowOrder() {
        val drag = CategoryDragSession(listOf("A", "B"))
        drag.syncExternal(listOf("B", "C"))
        assertEquals(listOf("B", "C"), drag.order)
    }
}
