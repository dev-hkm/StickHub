package com.hkm.stickhub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.hkm.stickhub.data.db.StickHubDbHelper
import com.hkm.stickhub.data.provider.StickerContentProvider
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.util.BackupHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataSafetyRegressionTest {
    private lateinit var context: Context
    private lateinit var repository: StickerRepository

    @Before
    fun setUp() {
        runBlocking {
            context = RuntimeEnvironment.getApplication()
            repository = StickerRepository(context)
            repository.refresh()
        }
    }

    @Test fun cancelledStreamImportNeverLeavesRowsWithoutTheirFiles() = runBlocking {
        val worker = launch {
            val job = coroutineContext[Job]!!
            val stream = object : ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)) {
                override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                    return super.read(buffer, offset, length).also { if (it == -1) job.cancel() }
                }
            }
            repository.saveStickerFromStream(stream)
        }
        worker.join()
        repository.refresh()
        assertTrue(repository.stickersFlow.value.all { File(it.filePath).isFile })
        assertTrue(repository.stickersDir.listFiles().orEmpty().none { it.name.startsWith("tmp_") })
    }

    @Test fun snapshotFailureAfterInsertDoesNotDeleteCommittedImage() = runBlocking {
        val helper = StickHubDbHelper(context)
        helper.writableDatabase.execSQL("DROP TABLE categories")
        val saved = repository.saveStickerBitmap(bitmap(Color.RED), "Committed")
        assertNotNull("The durable save succeeded even if snapshot publication failed", saved)
        assertTrue(File(saved!!.filePath).isFile)
        helper.close()
    }

    @Test fun failedOverwritePreservesOriginalBytesAndPath() = runBlocking {
        val saved = repository.saveStickerBitmap(bitmap(Color.RED), "Original")!!
        val original = File(saved.filePath).readBytes()
        val helper = StickHubDbHelper(context)
        helper.writableDatabase.execSQL("CREATE TRIGGER reject_edit BEFORE UPDATE ON stickers BEGIN SELECT RAISE(ABORT, 'Injected update failure'); END")
        assertFalse(repository.overwriteStickerBitmap(saved.id, bitmap(Color.BLUE)))
        assertArrayEquals(original, File(saved.filePath).readBytes())
        repository.refresh()
        assertEquals(saved.filePath, repository.stickersFlow.value.single().filePath)
        helper.close()
    }

    @Test fun saveAfterRenamingGeneralUsesAnExistingCategory() = runBlocking {
        assertTrue(repository.renameCategory("General", "Personal"))
        val saved = repository.saveStickerBitmap(bitmap(Color.RED), "New")!!
        assertTrue(repository.categoriesFlow.value.any { it.name == saved.category })
    }

    @Test fun backupRoundTripPreservesDuplicateMetadataAndManualOrder() = runBlocking {
        val first = repository.saveStickerBitmap(bitmap(Color.RED), "First")!!
        val second = repository.saveStickerBitmap(bitmap(Color.RED), "Second")!!
        repository.persistStickerOrder(listOf(first.id, second.id))
        val backup = File(context.cacheDir, "roundtrip.stickhub")
        assertTrue(BackupHelper.exportBackup(context, Uri.fromFile(backup), repository.stickersFlow.value, repository.categoriesFlow.value))
        repository.batchDelete(listOf(first.id, second.id))
        assertEquals(2, BackupHelper.importBackup(context, Uri.fromFile(backup), repository))
        assertEquals(listOf("First", "Second"), repository.stickersFlow.value.map { it.title })
        assertEquals(0, BackupHelper.importBackup(context, Uri.fromFile(backup), repository))
        assertEquals(2, repository.stickersFlow.value.size)
    }

    @Test fun restoreKeepsWebpExtensionAndMimeType() = runBlocking {
        val source = File(context.cacheDir, "source.webp")
        source.writeBytes(byteArrayOf(82, 73, 70, 70, 8, 0, 0, 0, 87, 69, 66, 80))
        val saved = repository.restoreSticker(source, "WebP", "General", "", false, 123L, 2)!!
        assertEquals("webp", File(saved.filePath).extension)
        val uri = StickerContentProvider.getStickerUri(context, File(saved.filePath))
        assertEquals("image/webp", StickerContentProvider().getType(uri))
    }

    @Test fun backupWithMissingLastImageDoesNotPartiallyImport() = runBlocking {
        val metadata = JSONObject().put("version", 2).put("categories", JSONArray().put(JSONObject().put("name", "Imported")))
            .put("stickers", JSONArray().put(stickerJson("first.png")).put(stickerJson("missing.png")))
        val archive = zip("missing.stickhub", mapOf("metadata.json" to metadata.toString().toByteArray(), "stickers/first.png" to byteArrayOf(1)))
        assertEquals(0, BackupHelper.importBackup(context, Uri.fromFile(archive), repository))
        repository.refresh()
        assertTrue(repository.stickersFlow.value.isEmpty())
        assertTrue(repository.categoriesFlow.value.none { it.name == "Imported" })
    }

    @Test fun metadataLargerThanOneMegabyteIsRejectedBeforeMutation() = runBlocking {
        val metadata = JSONObject().put("version", 2).put("padding", "x".repeat(1024 * 1024))
            .put("stickers", JSONArray().put(stickerJson("one.png")))
        val archive = zip("metadata.stickhub", mapOf("metadata.json" to metadata.toString().toByteArray(), "stickers/one.png" to byteArrayOf(1)))
        assertEquals(0, BackupHelper.importBackup(context, Uri.fromFile(archive), repository))
        assertTrue(repository.stickersFlow.value.isEmpty())
    }

    @Test fun exportSupportsRoundTripOfFiveHundredStickers() = runBlocking {
        val source = File(context.cacheDir, "small.png").apply { writeBytes(byteArrayOf(1)) }
        repeat(500) { index ->
            source.writeBytes("sticker-$index".toByteArray())
            repository.restoreSticker(source, "Sticker $index", "General", "", false, index + 1L, 0)
        }
        repository.refresh()
        assertEquals(500, repository.stickersFlow.value.size)
        val archive = File(context.cacheDir, "five-hundred.stickhub")
        assertTrue(BackupHelper.exportBackup(context, Uri.fromFile(archive), repository.stickersFlow.value, repository.categoriesFlow.value))
        repository.batchDelete(repository.stickersFlow.value.map { it.id })
        assertEquals(500, BackupHelper.importBackup(context, Uri.fromFile(archive), repository))
    }

    @Test fun providerReportsGifAndHeicMimeTypes() {
        val provider = StickerContentProvider()
        assertEquals("image/gif", provider.getType(Uri.parse("content://com.hkm.stickhub.stickerprovider/stickers/example.gif")))
        assertEquals("image/heic", provider.getType(Uri.parse("content://com.hkm.stickhub.stickerprovider/stickers/example.heic")))
    }

    @Test fun exportWithMissingImageDoesNotReportSuccess() = runBlocking {
        val saved = repository.saveStickerBitmap(bitmap(Color.RED), "Missing")!!
        File(saved.filePath).delete()
        val archive = File(context.cacheDir, "must-not-succeed.stickhub")
        assertFalse(BackupHelper.exportBackup(context, Uri.fromFile(archive), repository.stickersFlow.value, repository.categoriesFlow.value))
    }

    @Test fun databaseFailureDuringRestoreRollsBackEarlierRowsAndFiles() = runBlocking {
        val helper = StickHubDbHelper(context)
        helper.writableDatabase.execSQL("CREATE TRIGGER reject_second BEFORE INSERT ON stickers WHEN NEW.title = 'second.png' BEGIN SELECT RAISE(ABORT, 'Injected restore failure'); END")
        val metadata = JSONObject().put("version", 2).put("categories", JSONArray().put(JSONObject().put("name", "Imported")))
            .put("stickers", JSONArray().put(stickerJson("first.png")).put(stickerJson("second.png")))
        val archive = zip("rollback.stickhub", mapOf("metadata.json" to metadata.toString().toByteArray(), "stickers/first.png" to byteArrayOf(1), "stickers/second.png" to byteArrayOf(2)))
        assertEquals(0, BackupHelper.importBackup(context, Uri.fromFile(archive), repository))
        repository.refresh()
        assertTrue(repository.stickersFlow.value.isEmpty())
        assertTrue(repository.categoriesFlow.value.none { it.name == "Imported" })
        assertTrue(repository.stickersDir.listFiles().orEmpty().isEmpty())
        helper.close()
    }

    private fun bitmap(color: Int) = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
    private fun stickerJson(fileName: String) = JSONObject().put("fileName", fileName).put("title", fileName).put("category", "Imported").put("createdAt", 1)
    private fun zip(name: String, entries: Map<String, ByteArray>): File {
        val archive = File(context.cacheDir, name)
        ZipOutputStream(archive.outputStream()).use { zip ->
            entries.forEach { (entry, bytes) ->
                zip.putNextEntry(ZipEntry(entry))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return archive
    }
}
