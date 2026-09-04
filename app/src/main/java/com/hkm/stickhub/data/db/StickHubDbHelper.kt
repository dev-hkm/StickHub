package com.hkm.stickhub.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StickHubDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "stickhub.db"
        const val DATABASE_VERSION = 5

        const val TABLE_STICKERS = "stickers"
        const val COL_ID = "id"
        const val COL_FILE_PATH = "file_path"
        const val COL_TITLE = "title"
        const val COL_CATEGORY = "category"
        const val COL_TAGS = "tags"
        const val COL_IS_FAVORITE = "is_favorite"
        const val COL_CREATED_AT = "created_at"
        const val COL_USAGE_COUNT = "usage_count"
        const val COL_CONTENT_SHA256 = "content_sha256"
        const val COL_SORT_ORDER = "sort_order"

        const val TABLE_CATEGORIES = "categories"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "name"
        const val COL_CAT_IS_DEFAULT = "is_default"
        const val COL_CAT_DISPLAY_ORDER = "display_order"

        val LEGACY_CATEGORY_MIGRATION_MAP = mapOf(
            "Chung" to "General",
            "Meme" to "Memes",
            "Cảm xúc" to "Reactions",
            "Dễ thương" to "Cute"
        )

        /** True when [column] already exists on [table] (makes ADD COLUMN re-runnable). */
        fun hasColumn(db: SQLiteDatabase, table: String, column: String): Boolean {
            db.rawQuery("PRAGMA table_info($table)", null).use { cursor ->
                val nameIdx = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIdx) == column) return true
                }
            }
            return false
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createStickersTable = """
            CREATE TABLE $TABLE_STICKERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FILE_PATH TEXT NOT NULL,
                $COL_TITLE TEXT DEFAULT '',
                $COL_CATEGORY TEXT DEFAULT 'General',
                $COL_TAGS TEXT DEFAULT '',
                $COL_IS_FAVORITE INTEGER DEFAULT 0,
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_USAGE_COUNT INTEGER DEFAULT 0,
                $COL_CONTENT_SHA256 TEXT,
                $COL_SORT_ORDER INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createStickersTable)

        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CAT_NAME TEXT UNIQUE NOT NULL,
                $COL_CAT_IS_DEFAULT INTEGER DEFAULT 0,
                $COL_CAT_DISPLAY_ORDER INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createCategoriesTable)

        // Seed default categories (General is default, others are custom categories)
        val defaultCats = listOf("General", "Memes", "Reactions", "Cute", "Work")
        defaultCats.forEachIndexed { index, cat ->
            val cv = ContentValues().apply {
                put(COL_CAT_NAME, cat)
                put(COL_CAT_IS_DEFAULT, if (cat == "General") 1 else 0)
                put(COL_CAT_DISPLAY_ORDER, index)
            }
            db.insert(TABLE_CATEGORIES, null, cv)
        }

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_cat ON $TABLE_STICKERS ($COL_CATEGORY)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_created ON $TABLE_STICKERS ($COL_CREATED_AT DESC)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_content_hash ON $TABLE_STICKERS ($COL_CONTENT_SHA256)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_sort_order ON $TABLE_STICKERS ($COL_SORT_ORDER DESC)")
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // onOpen only creates safe indexes, does not mutate user data
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_cat ON $TABLE_STICKERS ($COL_CATEGORY)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_created ON $TABLE_STICKERS ($COL_CREATED_AT DESC)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_content_hash ON $TABLE_STICKERS ($COL_CONTENT_SHA256)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_sort_order ON $TABLE_STICKERS ($COL_SORT_ORDER DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.beginTransaction()
            try {
                for ((legacy, target) in LEGACY_CATEGORY_MIGRATION_MAP) {
                    db.execSQL("UPDATE $TABLE_STICKERS SET $COL_CATEGORY = ? WHERE $COL_CATEGORY = ?", arrayOf(target, legacy))
                    db.execSQL("INSERT OR IGNORE INTO $TABLE_CATEGORIES ($COL_CAT_NAME, $COL_CAT_IS_DEFAULT) VALUES (?, 1)", arrayOf(target))
                    db.execSQL("DELETE FROM $TABLE_CATEGORIES WHERE $COL_CAT_NAME = ?", arrayOf(legacy))
                }
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_cat ON $TABLE_STICKERS ($COL_CATEGORY)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_created ON $TABLE_STICKERS ($COL_CREATED_AT DESC)")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        if (oldVersion < 3) {
            db.beginTransaction()
            try {
                // Idempotent: a killed upgrade must be re-runnable without
                // "duplicate column" crash-looping the DB open.
                if (!hasColumn(db, TABLE_STICKERS, COL_CONTENT_SHA256)) {
                    db.execSQL("ALTER TABLE $TABLE_STICKERS ADD COLUMN $COL_CONTENT_SHA256 TEXT")
                }
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_content_hash ON $TABLE_STICKERS ($COL_CONTENT_SHA256)")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        if (oldVersion < 4) {
            db.beginTransaction()
            try {
                if (!hasColumn(db, TABLE_STICKERS, COL_SORT_ORDER)) {
                    db.execSQL("ALTER TABLE $TABLE_STICKERS ADD COLUMN $COL_SORT_ORDER INTEGER NOT NULL DEFAULT 0")
                    // Preserve the existing newest-first order for every retained user sticker.
                    db.execSQL("UPDATE $TABLE_STICKERS SET $COL_SORT_ORDER = $COL_CREATED_AT")
                }
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_stickers_sort_order ON $TABLE_STICKERS ($COL_SORT_ORDER DESC)")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
        if (oldVersion < 5) {
            db.beginTransaction()
            try {
                if (!hasColumn(db, TABLE_CATEGORIES, COL_CAT_DISPLAY_ORDER)) {
                    db.execSQL("ALTER TABLE $TABLE_CATEGORIES ADD COLUMN $COL_CAT_DISPLAY_ORDER INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("UPDATE $TABLE_CATEGORIES SET $COL_CAT_DISPLAY_ORDER = $COL_CAT_ID WHERE $COL_CAT_NAME != 'General'")
                }
                // General is the only default category
                db.execSQL("UPDATE $TABLE_CATEGORIES SET $COL_CAT_IS_DEFAULT = 1, $COL_CAT_DISPLAY_ORDER = 0 WHERE $COL_CAT_NAME = 'General'")
                db.execSQL("UPDATE $TABLE_CATEGORIES SET $COL_CAT_IS_DEFAULT = 0 WHERE $COL_CAT_NAME != 'General'")
                db.execSQL("UPDATE $TABLE_CATEGORIES SET $COL_CAT_DISPLAY_ORDER = $COL_CAT_ID WHERE $COL_CAT_NAME != 'General'")
                db.execSQL("INSERT OR IGNORE INTO $TABLE_CATEGORIES ($COL_CAT_NAME, $COL_CAT_IS_DEFAULT, $COL_CAT_DISPLAY_ORDER) VALUES ('General', 1, 0)")
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
