package com.hkm.stickhub

import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.service.OverlayStickerFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayStickerFilterTest {

    @Test
    fun frequentRanksByUsageAndPreservesLibraryOrderForTies() {
        val stickers = listOf(
            StickerItem(id = 1, filePath = "/1.png", title = "First", usageCount = 2),
            StickerItem(id = 2, filePath = "/2.png", title = "Second", usageCount = 8),
            StickerItem(id = 3, filePath = "/3.png", title = "Third", usageCount = 8),
            StickerItem(id = 4, filePath = "/4.png", title = "Unused", usageCount = 0)
        )
        assertEquals(listOf(2L, 3L, 1L), OverlayStickerFilter.filter(stickers, "Frequent", "").map { it.id })
        assertEquals(stickers, OverlayStickerFilter.filter(stickers, "All", ""))
    }

    @Test
    fun matchingAllStickersDoesNotApplyAnArbitraryPopupLimit() {
        val stickers = (1L..40L).map { id ->
            StickerItem(
                id = id,
                filePath = "/tmp/$id.png",
                title = "Sticker $id",
                category = "General"
            )
        }

        val result = OverlayStickerFilter.filter(
            stickers = stickers,
            selectedCategory = "All",
            searchQuery = ""
        )

        assertEquals(40, result.size)
        assertEquals(stickers.map { it.id }, result.map { it.id })
    }
}
