package com.hkm.stickhub.data.repository

/** Pure, testable list rules used by the drag-to-reorder UI and database persistence. */
object StickerOrderPolicy {
    fun <T> move(items: List<T>, fromIndex: Int, toIndex: Int): List<T> {
        if (fromIndex !in items.indices || toIndex !in items.indices || fromIndex == toIndex) {
            return items
        }
        return items.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    fun isExactPermutation(expected: List<Long>, candidate: List<Long>): Boolean {
        return expected.size == candidate.size &&
            expected.size == expected.toSet().size &&
            candidate.size == candidate.toSet().size &&
            expected.toSet() == candidate.toSet()
    }
}
