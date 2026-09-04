package com.hkm.stickhub

import android.database.sqlite.SQLiteDatabase
import com.hkm.stickhub.data.db.StickHubDbHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseUpgradeIntegrationTest {
    @Test fun retainedDatabasesFromEveryVersionKeepStickerIdsAndMetadata() {
        val context = RuntimeEnvironment.getApplication()
        for (oldVersion in 1..5) {
            context.deleteDatabase(StickHubDbHelper.DATABASE_NAME)
            val file = context.getDatabasePath(StickHubDbHelper.DATABASE_NAME)
            file.parentFile!!.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
                val hash = if (oldVersion >= 3) ", content_sha256 TEXT" else ""
                val order = if (oldVersion >= 4) ", sort_order INTEGER NOT NULL DEFAULT 0" else ""
                val categoryOrder = if (oldVersion >= 5) ", display_order INTEGER NOT NULL DEFAULT 0" else ""
                db.execSQL("CREATE TABLE stickers (id INTEGER PRIMARY KEY AUTOINCREMENT, file_path TEXT NOT NULL, title TEXT DEFAULT '', category TEXT DEFAULT 'General', tags TEXT DEFAULT '', is_favorite INTEGER DEFAULT 0, created_at INTEGER NOT NULL, usage_count INTEGER DEFAULT 0$hash$order)")
                db.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL, is_default INTEGER DEFAULT 0$categoryOrder)")
                db.execSQL("INSERT INTO categories (name,is_default) VALUES ('Custom',0)")
                db.execSQL("INSERT INTO stickers (id,file_path,title,category,tags,is_favorite,created_at,usage_count) VALUES (42,'retained.png','Keep me','Custom','one,two',1,123456,7)")
                if (oldVersion >= 4) db.execSQL("UPDATE stickers SET sort_order=99")
                if (oldVersion >= 3) db.execSQL("UPDATE stickers SET content_sha256='retained-hash'")
                db.version = oldVersion
            }
            StickHubDbHelper(context).use { helper ->
                val db = helper.writableDatabase
                assertEquals(StickHubDbHelper.DATABASE_VERSION, db.version)
                db.rawQuery("SELECT * FROM stickers", null).use { rows ->
                    assertTrue(rows.moveToFirst())
                    assertEquals(42L, rows.getLong(rows.getColumnIndexOrThrow("id")))
                    assertEquals("Keep me", rows.getString(rows.getColumnIndexOrThrow("title")))
                    assertEquals("retained.png", rows.getString(rows.getColumnIndexOrThrow("file_path")))
                    assertEquals("Custom", rows.getString(rows.getColumnIndexOrThrow("category")))
                    assertEquals("one,two", rows.getString(rows.getColumnIndexOrThrow("tags")))
                    assertEquals(1, rows.getInt(rows.getColumnIndexOrThrow("is_favorite")))
                    assertEquals(123456L, rows.getLong(rows.getColumnIndexOrThrow("created_at")))
                    assertEquals(7, rows.getInt(rows.getColumnIndexOrThrow("usage_count")))
                    assertEquals(if (oldVersion >= 4) 99L else 123456L, rows.getLong(rows.getColumnIndexOrThrow("sort_order")))
                    if (oldVersion >= 3) assertEquals("retained-hash", rows.getString(rows.getColumnIndexOrThrow("content_sha256")))
                    assertFalse(rows.moveToNext())
                }
            }
        }
    }
}
