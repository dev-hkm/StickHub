package com.hkm.stickhub.service

/**
 * Sanitizes the category names used by the view-system overlay.
 *
 * Category data can be observed while the library is being refreshed or after
 * a legacy preferences migration. The overlay must never render duplicate
 * keys, blank chips, or a selected filter that no longer exists.
 */
object OverlayCategoryPolicy {
    private val systemCategories = listOf("All", "Favorites", "Frequent")

    fun normalize(orderedNames: List<String>, availableNames: List<String>): List<String> {
        val known = buildList {
            addAll(systemCategories)
            addAll(availableNames)
        }.filter { it.isNotBlank() }

        val canonical = known
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .toList()
        val byKey = canonical.associateBy { it.lowercase() }
        val result = ArrayList<String>(canonical.size)
        val seen = HashSet<String>()

        fun appendIfKnown(raw: String) {
            val key = raw.trim().lowercase()
            val value = byKey[key] ?: return
            if (seen.add(key)) result += value
        }

        orderedNames.forEach(::appendIfKnown)
        canonical.forEach(::appendIfKnown)
        return result
    }

    fun resolveSelection(requested: String, availableNames: List<String>): String {
        val normalized = normalize(emptyList(), availableNames)
        val requestedKey = requested.trim().lowercase()
        return normalized.firstOrNull { it.lowercase() == requestedKey } ?: "All"
    }
}
