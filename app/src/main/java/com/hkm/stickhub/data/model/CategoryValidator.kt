package com.hkm.stickhub.data.model

object CategoryValidator {
    const val MAX_LENGTH = 32
    val RESERVED_NAMES = setOf("all", "favorites", "frequent", "general")

    sealed interface Result {
        object Valid : Result
        data class Error(val message: String) : Result
    }

    fun validate(
        rawName: String,
        existingCategories: List<CategoryItem>,
        currentName: String? = null
    ): Result {
        val trimmed = rawName.trim()
        if (trimmed.isEmpty()) {
            return Result.Error("Category name cannot be empty")
        }
        if (trimmed.length > MAX_LENGTH) {
            return Result.Error("Category name must be $MAX_LENGTH characters or less")
        }
        if (RESERVED_NAMES.contains(trimmed.lowercase())) {
            return Result.Error("'$trimmed' is a reserved category name")
        }
        val isDuplicate = existingCategories.any { cat ->
            cat.name.equals(trimmed, ignoreCase = true) &&
                (currentName == null || !currentName.equals(cat.name, ignoreCase = true))
        }
        if (isDuplicate) {
            return Result.Error("Category '$trimmed' already exists")
        }
        return Result.Valid
    }
}
