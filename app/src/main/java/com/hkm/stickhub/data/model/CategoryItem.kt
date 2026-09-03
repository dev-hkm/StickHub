package com.hkm.stickhub.data.model

data class CategoryItem(
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    val displayOrder: Int = 0
)
