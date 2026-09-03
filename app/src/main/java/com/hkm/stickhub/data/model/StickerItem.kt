package com.hkm.stickhub.data.model

data class StickerItem(
    val id: Long = 0,
    val filePath: String,
    val title: String = "",
    val category: String = "General",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val usageCount: Int = 0
)
