package com.hkm.stickhub

import com.hkm.stickhub.data.db.StickHubDbHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseMigrationTest {

    @Test
    fun testLegacyCategoryMigrationMappings() {
        val map = StickHubDbHelper.LEGACY_CATEGORY_MIGRATION_MAP

        assertEquals("General", map["Chung"])
        assertEquals("Memes", map["Meme"])
        assertEquals("Reactions", map["Cảm xúc"])
        assertEquals("Cute", map["Dễ thương"])
    }

    @Test
    fun testDbVersionIncludesClipboardFingerprintAndManualOrderingMigration() {
        assertTrue(StickHubDbHelper.DATABASE_VERSION >= 5)
        assertEquals("content_sha256", StickHubDbHelper.COL_CONTENT_SHA256)
        assertEquals("sort_order", StickHubDbHelper.COL_SORT_ORDER)
        assertEquals("display_order", StickHubDbHelper.COL_CAT_DISPLAY_ORDER)
    }
}
