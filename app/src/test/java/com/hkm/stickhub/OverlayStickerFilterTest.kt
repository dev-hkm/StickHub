package com.hkm.stickhub

import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.service.OverlayStickerFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayStickerFilterTest {

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
