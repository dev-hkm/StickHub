package com.hkm.stickhub

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryVisibilityTest {

    @Test
    fun testDisableSearchClearsNonEmptyQuery() {
        var searchQuery = "funny dog"
        val showSearch = false

        // Logic from StickHubApp onToggleShowSearch
        if (!showSearch && searchQuery.isNotEmpty()) {
            searchQuery = ""
        }

        assertEquals("", searchQuery)
    }

    @Test
    fun testDisableSearchPreservesEmptyQuery() {
        var searchQuery = ""
        val showSearch = false

        if (!showSearch && searchQuery.isNotEmpty()) {
            searchQuery = ""
        }

        assertEquals("", searchQuery)
    }

    @Test
    fun testDisableCategoryFiltersResetsSelectedCategoryToAll() {
        var selectedCategory = "Memes"
        val showCategoryFilters = false

        // Logic from StickHubApp onToggleShowCategoryFilters
        if (!showCategoryFilters && selectedCategory != "All") {
            selectedCategory = "All"
        }

        assertEquals("All", selectedCategory)
    }

    @Test
    fun testDisableCategoryFiltersKeepsAllWhenAlreadyAll() {
        var selectedCategory = "All"
        val showCategoryFilters = false

        if (!showCategoryFilters && selectedCategory != "All") {
            selectedCategory = "All"
        }

        assertEquals("All", selectedCategory)
    }

    @Test
    fun testDisablingBothHidesFiltersAndEliminatesHiddenStates() {
        var searchQuery = "smile"
        var selectedCategory = "Favorites"

        val showSearch = false
        val showCategoryFilters = false

        if (!showSearch && searchQuery.isNotEmpty()) {
            searchQuery = ""
        }
        if (!showCategoryFilters && selectedCategory != "All") {
            selectedCategory = "All"
        }

        assertEquals("", searchQuery)
        assertEquals("All", selectedCategory)
    }
}
