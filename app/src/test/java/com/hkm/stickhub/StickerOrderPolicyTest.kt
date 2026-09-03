package com.hkm.stickhub

import com.hkm.stickhub.data.repository.StickerOrderPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StickerOrderPolicyTest {

    @Test
    fun movePreservesEveryIdAndMovesForward() {
        val source = listOf(1L, 2L, 3L, 4L, 5L, 6L)

        val result = StickerOrderPolicy.move(source, fromIndex = 0, toIndex = 5)

        assertEquals(listOf(2L, 3L, 4L, 5L, 6L, 1L), result)
        assertTrue(StickerOrderPolicy.isExactPermutation(source, result))
    }

    @Test
    fun movePreservesEveryIdAndMovesBackward() {
        val source = listOf(1L, 2L, 3L, 4L, 5L, 6L)

        val result = StickerOrderPolicy.move(source, fromIndex = 5, toIndex = 0)

        assertEquals(listOf(6L, 1L, 2L, 3L, 4L, 5L), result)
        assertTrue(StickerOrderPolicy.isExactPermutation(source, result))
    }

    @Test
    fun invalidMoveDoesNotMutateTheSourceOrder() {
        val source = listOf(1L, 2L, 3L)

        assertEquals(source, StickerOrderPolicy.move(source, fromIndex = -1, toIndex = 2))
        assertEquals(source, StickerOrderPolicy.move(source, fromIndex = 0, toIndex = 8))
        assertFalse(StickerOrderPolicy.isExactPermutation(source, listOf(1L, 1L, 2L)))
    }
}
