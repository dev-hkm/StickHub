package com.hkm.stickhub.data.model

data class CategoryItem(
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    val displayOrder: Int = 0
) {
    companion object {
        /** Canonical home category; always exists (recreated on demand). */
        const val FALLBACK_NAME = "General"

        /**
         * Pure rule for where stickers land when [target] is deleted:
         * "General" when it survives, otherwise the first remaining
         * category by display order, otherwise a fresh "General".
         * Single source of truth — repository and UI share it.
         */
        fun pickDeleteFallback(
            all: List<CategoryItem>,
            target: String
        ): String {
            val remaining = all
                .filter { !it.name.equals(target, ignoreCase = true) }
                .sortedBy { it.displayOrder }
            remaining.firstOrNull { it.name.equals(FALLBACK_NAME, ignoreCase = true) }?.let {
                return it.name
            }
            return remaining.firstOrNull()?.name ?: FALLBACK_NAME
        }
    }
}
