package com.hkm.stickhub

import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.repository.StickerOrderPolicy
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.ui.library.CategoryDragSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryOrderTest {

    private fun simulateReconcile(
        savedOrder: List<String>?,
        dbCategories: List<CategoryItem>
    ): List<String> {
        val allKnown = StickerRepository.SYSTEM_CATEGORIES + dbCategories.map { it.name }
        if (savedOrder.isNullOrEmpty()) {
            return allKnown
        }
        val seen = mutableSetOf<String>()
        val result = mutableListOf<String>()
        for (savedName in savedOrder) {
            val matched = allKnown.firstOrNull { it.equals(savedName, ignoreCase = true) }
            if (matched != null && seen.add(matched.lowercase())) {
                result.add(matched)
            }
        }
        for (known in allKnown) {
            if (seen.add(known.lowercase())) {
                result.add(known)
            }
        }
        return result
    }

    @Test
    fun defaultOrderStartsAllFavoritesFrequentThenDbCategories() {
        val dbCats = listOf(
            CategoryItem(id = 1, name = "General", displayOrder = 0),
            CategoryItem(id = 2, name = "Memes", displayOrder = 1)
        )
        val reconciled = simulateReconcile(null, dbCats)
        assertEquals(listOf("All", "Favorites", "Frequent", "General", "Memes"), reconciled)
    }

    @Test
    fun customReorderAllowsFavoritesFirstOrAnyPermutation() {
        val dbCats = listOf(
            CategoryItem(id = 1, name = "General", displayOrder = 0),
            CategoryItem(id = 2, name = "Memes", displayOrder = 1)
        )
        val customOrder = listOf("Favorites", "Memes", "All", "General", "Frequent")
        val reconciled = simulateReconcile(customOrder, dbCats)
        assertEquals(customOrder, reconciled)
    }

    @Test
    fun newlyAddedDbCategoryIsAppendedToExistingOrder() {
        val initialCats = listOf(
            CategoryItem(id = 1, name = "General", displayOrder = 0)
        )
        val savedOrder = listOf("Favorites", "General", "All", "Frequent")
        val reconciledInitial = simulateReconcile(savedOrder, initialCats)
        assertEquals(savedOrder, reconciledInitial)

        // User adds "Anime"
        val updatedCats = initialCats + CategoryItem(id = 2, name = "Anime", displayOrder = 1)
        val reconciledUpdated = simulateReconcile(savedOrder, updatedCats)
        assertEquals(listOf("Favorites", "General", "All", "Frequent", "Anime"), reconciledUpdated)
    }

    @Test
    fun deletedDbCategoryIsDroppedFromOrder() {
        val cats = listOf(
            CategoryItem(id = 1, name = "General", displayOrder = 0)
        )
        // Saved order previously had "Memes" which got deleted
        val savedOrder = listOf("All", "Memes", "Favorites", "Frequent", "General")
        val reconciled = simulateReconcile(savedOrder, cats)
        assertEquals(listOf("All", "Favorites", "Frequent", "General"), reconciled)
    }

    @Test
    fun systemCategoriesAreAlwaysPreservedEvenIfMissingFromSavedOrder() {
        val cats = listOf(
            CategoryItem(id = 1, name = "General", displayOrder = 0)
        )
        // Corrupted or legacy saved order missing "Frequent"
        val savedOrder = listOf("All", "Favorites", "General")
        val reconciled = simulateReconcile(savedOrder, cats)
        assertEquals(listOf("All", "Favorites", "General", "Frequent"), reconciled)
    }

    @Test
    fun matchingIsCaseInsensitiveAndDeduplicates() {
        val cats = listOf(
            CategoryItem(id = 1, name = "Memes", displayOrder = 0)
        )
        val savedOrder = listOf("all", "FAVORITES", "memes", "All", "frequent")
        val reconciled = simulateReconcile(savedOrder, cats)
        assertEquals(listOf("All", "Favorites", "Memes", "Frequent"), reconciled)
    }

    @Test
    fun dragSessionSupportsMovingSystemCategories() {
        val initial = listOf("All", "Favorites", "Frequent", "General", "Anime")
        val session = CategoryDragSession(initial)

        // Drag "Favorites" to the very first position before "All"
        assertTrue(session.start("Favorites"))
        assertTrue(session.moveTo("All"))
        assertEquals(listOf("Favorites", "All", "Frequent", "General", "Anime"), session.order)
        val finished = session.finish()
        assertEquals(listOf("Favorites", "All", "Frequent", "General", "Anime"), finished)

        // Drag "Anime" before "Favorites"
        assertTrue(session.start("Anime"))
        assertTrue(session.moveTo("Favorites"))
        assertEquals(listOf("Anime", "Favorites", "All", "Frequent", "General"), session.finish())
    }

    @Test
    fun stickerOrderPolicyMoveBoundaryConditions() {
        val list = listOf("A", "B", "C")
        // Invalid indices return original list
        assertEquals(list, StickerOrderPolicy.move(list, -1, 1))
        assertEquals(list, StickerOrderPolicy.move(list, 0, 5))
        assertEquals(list, StickerOrderPolicy.move(list, 1, 1))

        // Valid moves
        assertEquals(listOf("B", "A", "C"), StickerOrderPolicy.move(list, 0, 1))
        assertEquals(listOf("B", "C", "A"), StickerOrderPolicy.move(list, 0, 2))
        assertEquals(listOf("C", "A", "B"), StickerOrderPolicy.move(list, 2, 0))
    }
}
