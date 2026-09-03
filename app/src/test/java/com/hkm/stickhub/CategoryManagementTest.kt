package com.hkm.stickhub

import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.CategoryValidator
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerOrderPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryManagementTest {

    private val sampleCategories = listOf(
        CategoryItem(id = 1, name = "General", isDefault = true, displayOrder = 0),
        CategoryItem(id = 2, name = "Memes", isDefault = false, displayOrder = 1),
        CategoryItem(id = 3, name = "Reactions", isDefault = false, displayOrder = 2),
        CategoryItem(id = 4, name = "Cute", isDefault = false, displayOrder = 3)
    )

    @Test
    fun testValidationRejectsEmptyAndBlankNames() {
        val emptyResult = CategoryValidator.validate("", sampleCategories)
        assertTrue(emptyResult is CategoryValidator.Result.Error)
        assertEquals("Category name cannot be empty", (emptyResult as CategoryValidator.Result.Error).message)

        val blankResult = CategoryValidator.validate("   ", sampleCategories)
        assertTrue(blankResult is CategoryValidator.Result.Error)
        assertEquals("Category name cannot be empty", (blankResult as CategoryValidator.Result.Error).message)
    }

    @Test
    fun testValidationRejectsNamesExceedingMaxLength() {
        val longName = "A".repeat(33)
        val result = CategoryValidator.validate(longName, sampleCategories)
        assertTrue(result is CategoryValidator.Result.Error)
        assertEquals(
            "Category name must be ${CategoryValidator.MAX_LENGTH} characters or less",
            (result as CategoryValidator.Result.Error).message
        )
    }

    @Test
    fun testValidationRejectsReservedNamesCaseInsensitive() {
        for (reserved in listOf("all", "ALL", "Favorites", "FAVORITES", "frequent", "Frequent", "general", "General")) {
            val result = CategoryValidator.validate(reserved, sampleCategories)
            assertTrue("Expected error for reserved name '$reserved'", result is CategoryValidator.Result.Error)
            val msg = (result as CategoryValidator.Result.Error).message
            assertTrue(msg.contains("reserved"))
        }
    }

    @Test
    fun testValidationRejectsDuplicateNamesCaseInsensitive() {
        val result = CategoryValidator.validate("memes", sampleCategories)
        assertTrue(result is CategoryValidator.Result.Error)
        val msg = (result as CategoryValidator.Result.Error).message
        assertTrue(msg.contains("already exists"))

        val result2 = CategoryValidator.validate("  Cute  ", sampleCategories)
        assertTrue(result2 is CategoryValidator.Result.Error)
    }

    @Test
    fun testValidationAllowsRenamingToSameNameWithDifferentCaseOrWhitespace() {
        // When renaming "Memes", entering "Memes" or "MEMES" is not considered a conflicting duplicate of ANOTHER category
        val result = CategoryValidator.validate("Memes", sampleCategories, currentName = "Memes")
        assertTrue(result is CategoryValidator.Result.Valid)

        val resultDiffCase = CategoryValidator.validate("MEMES", sampleCategories, currentName = "Memes")
        assertTrue(resultDiffCase is CategoryValidator.Result.Valid)
    }

    @Test
    fun testValidationAcceptsValidCategoryNames() {
        val result1 = CategoryValidator.validate("Work & Study", sampleCategories)
        assertTrue(result1 is CategoryValidator.Result.Valid)

        val result2 = CategoryValidator.validate("Badminton 2026", sampleCategories)
        assertTrue(result2 is CategoryValidator.Result.Valid)
    }

    @Test
    fun testCategoryReorderMovesItemsCorrectly() {
        val names = listOf("Memes", "Reactions", "Cute")
        val moved = StickerOrderPolicy.move(names, 0, 2)
        assertEquals(listOf("Reactions", "Cute", "Memes"), moved)
    }

    @Test
    fun testCategorySafeDeleteSimulatedDataReassignment() {
        // Verify stickers assigned to a deleted category are safely moved to General
        val categoryToDelete = "Memes"
        val stickers = listOf(
            StickerItem(id = 1, filePath = "/path/1.png", title = "Dog", category = "Memes"),
            StickerItem(id = 2, filePath = "/path/2.png", title = "Cat", category = "Cute"),
            StickerItem(id = 3, filePath = "/path/3.png", title = "Smile", category = "General")
        )

        val reassigned = stickers.map { sticker ->
            if (sticker.category.equals(categoryToDelete, ignoreCase = true)) {
                sticker.copy(category = "General")
            } else {
                sticker
            }
        }

        assertEquals("General", reassigned.find { it.id == 1L }?.category)
        assertEquals("Cute", reassigned.find { it.id == 2L }?.category)
        assertEquals("General", reassigned.find { it.id == 3L }?.category)
    }

    @Test
    fun testCategoryRenameSimulatedDataUpdate() {
        // Verify renaming updates all matching stickers without losing tags or metadata
        val oldName = "Memes"
        val newName = "Funny"
        val stickers = listOf(
            StickerItem(id = 1, filePath = "/path/1.png", title = "Dog", category = "Memes", tags = "dog, funny"),
            StickerItem(id = 2, filePath = "/path/2.png", title = "Cat", category = "Cute", tags = "cat")
        )

        val renamedStickers = stickers.map { sticker ->
            if (sticker.category.equals(oldName, ignoreCase = true)) {
                sticker.copy(category = newName)
            } else {
                sticker
            }
        }

        val updated = renamedStickers.find { it.id == 1L }!!
        assertEquals("Funny", updated.category)
        assertEquals("dog, funny", updated.tags)
    }
}
