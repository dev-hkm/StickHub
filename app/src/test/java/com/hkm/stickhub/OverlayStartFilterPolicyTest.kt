package com.hkm.stickhub

import com.hkm.stickhub.service.OverlayStartFilterMode
import com.hkm.stickhub.service.OverlayStartFilterPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayStartFilterPolicyTest {

    private val sampleCategories = listOf("Anime", "Memes", "Work", "Reactions")

    @Test
    fun testResolveAllMode() {
        val result = OverlayStartFilterPolicy.resolveActiveFilter(
            mode = OverlayStartFilterMode.ALL,
            customCategory = null,
            lastUsedFilter = "Memes",
            availableCategories = sampleCategories
        )
        assertEquals("All", result)
    }

    @Test
    fun testResolveFavoritesMode() {
        val result = OverlayStartFilterPolicy.resolveActiveFilter(
            mode = OverlayStartFilterMode.FAVORITES,
            customCategory = null,
            lastUsedFilter = "All",
            availableCategories = sampleCategories
        )
        assertEquals("Favorites", result)
    }

    @Test
    fun testResolveFrequentMode() {
        val result = OverlayStartFilterPolicy.resolveActiveFilter(
            mode = OverlayStartFilterMode.FREQUENT,
            customCategory = null,
            lastUsedFilter = null,
            availableCategories = sampleCategories
        )
        assertEquals("Frequent", result)
    }

    @Test
    fun testResolveLastUsedFilterValid() {
        val resultCategory = OverlayStartFilterPolicy.resolveActiveFilter(
            mode = OverlayStartFilterMode.LAST_USED,
            customCategory = null,
            lastUsedFilter = "Memes",
            availableCategories = sampleCategories
        )
        assertEquals("Memes", resultCategory)

        val resultSystem = OverlayStartFilterPolicy.resolveActiveFilter(
            mode = OverlayStartFilterMode.LAST_USED,
            customCategory = null,
            lastUsedFilter = "Favorites",
            availableCategories = sampleCategories
        )
        assertEquals("Favorites", resultSystem)
    }

    @Test
    fun testResolveLastUsedFilterFallbackWhenMissingOrDeleted() {
        val resultNull = OverlayStartFilterPolicy.resolveActiveFilter(
            mode = OverlayStartFilterMode.LAST_USED,
            customCategory = null,
            lastUsedFilter = null,
            availableCategories = sampleCategories
        )
        assertEquals("All", resultNull)

        val resultDeleted = OverlayStartFilterPolicy.resolveActiveFilter(
            mode = OverlayStartFilterMode.LAST_USED,
            customCategory = null,
            lastUsedFilter = "DeletedCategory",
            availableCategories = sampleCategories
        )
        assertEquals("All", resultDeleted)
    }

    @Test
    fun testResolveCustomCategoryValid() {
        val result = OverlayStartFilterPolicy.resolveActiveFilter(
            mode = OverlayStartFilterMode.CUSTOM_CATEGORY,
            customCategory = "Anime",
            lastUsedFilter = "All",
            availableCategories = sampleCategories
        )
        assertEquals("Anime", result)
    }

    @Test
    fun testResolveCustomCategoryFallbackWhenRenamedOrDeleted() {
        val result = OverlayStartFilterPolicy.resolveActiveFilter(
            mode = OverlayStartFilterMode.CUSTOM_CATEGORY,
            customCategory = "NonExistentCategory",
            lastUsedFilter = "Memes",
            availableCategories = sampleCategories
        )
        assertEquals("All", result)
    }

    @Test
    fun testShouldRecordLastUsedFilterOnlyWhenChanged() {
        assertTrue(OverlayStartFilterPolicy.shouldRecordLastUsedFilter("All", "Memes"))
        assertTrue(OverlayStartFilterPolicy.shouldRecordLastUsedFilter("Favorites", "All"))
        assertFalse(OverlayStartFilterPolicy.shouldRecordLastUsedFilter("Memes", "Memes"))
        assertFalse(OverlayStartFilterPolicy.shouldRecordLastUsedFilter("all", "All"))
    }
}
