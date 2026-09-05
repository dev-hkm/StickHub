package com.hkm.stickhub

import com.hkm.stickhub.service.OverlayCategoryPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayCategoryPolicyTest {
    @Test
    fun normalizesLegacyOrderWithoutDuplicateOrGhostChips() {
        val result = OverlayCategoryPolicy.normalize(
            orderedNames = listOf("Memes", "", "all", "Ghost", "MEMES", "  Anime  "),
            availableNames = listOf("General", "Memes", "Anime")
        )

        assertEquals(listOf("Memes", "All", "Anime", "Favorites", "Frequent", "General"), result)
    }

    @Test
    fun invalidSelectionFallsBackToAllAndValidSelectionUsesCanonicalCase() {
        val available = listOf("General", "Anime")
        assertEquals("Anime", OverlayCategoryPolicy.resolveSelection(" anime ", available))
        assertEquals("All", OverlayCategoryPolicy.resolveSelection("deleted-category", available))
    }
}
