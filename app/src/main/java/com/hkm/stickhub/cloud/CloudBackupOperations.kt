package com.hkm.stickhub.cloud

import android.content.Context
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerRepository
import com.hkm.stickhub.util.BackupHelper
import com.hkm.stickhub.util.BackupImportResult
import com.hkm.stickhub.util.ClipboardContentHasher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

sealed interface CloudBackupWorkState {
    data object Idle : CloudBackupWorkState
    data class Working(val label: String) : CloudBackupWorkState
    data class Ready(val recoveryCode: String) : CloudBackupWorkState
    data class UploadFinished(val metadata: CloudBackupMetadata) : CloudBackupWorkState
    data class RestoreFinished(val result: BackupImportResult) : CloudBackupWorkState
    data class Failed(val message: String) : CloudBackupWorkState
}

/** Serializes encrypted cloud backup/restore while local backup stays available. */
class CloudBackupOperations private constructor(appContext: Context) {
    private val context = appContext
    private val repository = StickerRepository.getInstance(appContext)
    private val client = CloudBackupClient()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<CloudBackupWorkState>(CloudBackupWorkState.Idle)
    val state: StateFlow<CloudBackupWorkState> = mutableState.asStateFlow()

    fun recoveryCode(): String? = CloudVaultStore.get(context)?.recoveryCode

    /**
     * Registers the vault and immediately snapshots a non-empty local library.
     * Setup must not leave the user with a recovery code that authenticates a
     * vault but has no data behind it.
     */
    fun createVault(
        stickers: List<StickerItem> = emptyList(),
        categories: List<CategoryItem> = emptyList()
    ) {
        if (mutableState.value is CloudBackupWorkState.Working) return
        mutableState.value = CloudBackupWorkState.Working("Preparing encrypted cloud vault…")
        scope.launch {
            try {
                val outcome = mutex.withLock {
                    val credentials = ensureCredentials()
                    if (CloudBackupPolicy.validateUpload(stickers.size) == null) {
                        CloudBackupWorkState.UploadFinished(uploadInternal(credentials, stickers, categories))
                    } else {
                        CloudBackupWorkState.Ready(credentials.recoveryCode)
                    }
                }
                mutableState.value = outcome
            } catch (ce: CancellationException) {
                throw ce
            } catch (error: Exception) {
                mutableState.value = CloudBackupWorkState.Failed(error.message ?: "Couldn't prepare cloud backup.")
            }
        }
    }

    fun startUpload(stickers: List<StickerItem>, categories: List<CategoryItem>) {
        if (mutableState.value is CloudBackupWorkState.Working) return
        CloudBackupPolicy.validateUpload(stickers.size)?.let { message ->
            mutableState.value = CloudBackupWorkState.Failed(message)
            return
        }
        mutableState.value = CloudBackupWorkState.Working("Encrypting and uploading backup…")
        scope.launch {
            try {
                val metadata = mutex.withLock { uploadInternal(stickers, categories) }
                mutableState.value = CloudBackupWorkState.UploadFinished(metadata)
            } catch (ce: CancellationException) {
                throw ce
            } catch (error: Exception) {
                mutableState.value = CloudBackupWorkState.Failed(error.message ?: "Cloud backup failed.")
            }
        }
    }

    fun startRestore() {
        startRestoreWithCredentials(null)
    }

    fun startRestoreWithRecoveryCode(rawCode: String) {
        if (mutableState.value is CloudBackupWorkState.Working) return
        mutableState.value = CloudBackupWorkState.Working("Verifying recovery code and restoring…")
        scope.launch {
            try {
                val credentials = CloudVaultCredentials.parseRecoveryCode(rawCode)
                val result = mutex.withLock { restoreInternal(credentials) }
                mutableState.value = CloudBackupWorkState.RestoreFinished(result)
            } catch (ce: CancellationException) {
                throw ce
            } catch (error: Exception) {
                mutableState.value = CloudBackupWorkState.Failed(error.message ?: "Cloud restore failed.")
            }
        }
    }

    private fun startRestoreWithCredentials(credentials: CloudVaultCredentials?) {
        if (mutableState.value is CloudBackupWorkState.Working) return
        mutableState.value = CloudBackupWorkState.Working("Downloading and restoring backup…")
        scope.launch {
            try {
                val result = mutex.withLock { restoreInternal(credentials) }
                mutableState.value = CloudBackupWorkState.RestoreFinished(result)
            } catch (ce: CancellationException) {
                throw ce
            } catch (error: Exception) {
                mutableState.value = CloudBackupWorkState.Failed(error.message ?: "Cloud restore failed.")
            }
        }
    }

    fun acknowledge() {
        if (mutableState.value !is CloudBackupWorkState.Working) {
            mutableState.value = CloudBackupWorkState.Idle
        }
    }

    fun shutdown() = scope.cancel()

    private suspend fun ensureCredentials(): CloudVaultCredentials {
        CloudVaultStore.get(context)?.let { return it }
        val generated = CloudVaultCredentials.generate()
        check(client.registerVault(generated)) { "Cloud vault registration failed." }
        CloudVaultStore.save(context, generated)
        return generated
    }

    private suspend fun uploadInternal(
        stickers: List<StickerItem>,
        categories: List<CategoryItem>
    ): CloudBackupMetadata = withContext(Dispatchers.IO) {
        uploadInternal(ensureCredentials(), stickers, categories)
    }

    private suspend fun uploadInternal(
        credentials: CloudVaultCredentials,
        stickers: List<StickerItem>,
        categories: List<CategoryItem>
    ): CloudBackupMetadata = withContext(Dispatchers.IO) {
        val plainFile = File(context.cacheDir, "cloud_plain_${UUID.randomUUID()}.stickhub")
        val encryptedFile = File(context.cacheDir, "cloud_encrypted_${UUID.randomUUID()}.bin")
        try {
            CloudTransferService.begin(context)
            mutableState.value = CloudBackupWorkState.Working("Preparing backup on device…")
            check(BackupHelper.exportBackupToFile(context, plainFile, stickers, categories)) {
                "Couldn't create a local backup snapshot."
            }
            mutableState.value = CloudBackupWorkState.Working("Encrypting backup…")
            CloudBackupStream.encrypt(plainFile, encryptedFile, credentials)
            plainFile.delete()
            val checksum = encryptedFile.inputStream().use(ClipboardContentHasher::sha256)
            client.uploadFile(credentials, encryptedFile, UUID.randomUUID().toString(), checksum) { done, total ->
                mutableState.value = CloudBackupWorkState.Working("Uploading backup: $done / $total parts")
            }
        } finally {
            plainFile.delete()
            encryptedFile.delete()
            CloudTransferService.end(context)
        }
    }

    private suspend fun restoreInternal(credentialsOverride: CloudVaultCredentials?): BackupImportResult = withContext(Dispatchers.IO) {
        val credentials = credentialsOverride ?: CloudVaultStore.get(context)
            ?: error("Create a cloud vault before restoring.")
        val archive = File(context.cacheDir, "cloud_restore_${UUID.randomUUID()}.stickhub")
        val encryptedFile = File(context.cacheDir, "cloud_download_${UUID.randomUUID()}.bin")
        try {
            CloudTransferService.begin(context)
            val metadata = client.downloadFile(credentials, encryptedFile)
            val actualChecksum = encryptedFile.inputStream().use(ClipboardContentHasher::sha256)
            check(actualChecksum.equals(metadata.checksum, ignoreCase = true)) { "Cloud backup integrity check failed." }
            mutableState.value = CloudBackupWorkState.Working("Decrypting and restoring backup…")
            CloudBackupStream.decrypt(encryptedFile, archive, credentials)
            encryptedFile.delete()
            val result = BackupHelper.importBackupDetailed(context, android.net.Uri.fromFile(archive), repository)
            if (credentialsOverride != null && result is BackupImportResult.Success) {
                CloudVaultStore.save(context, credentials)
            }
            result
        } finally {
            archive.delete()
            encryptedFile.delete()
            CloudTransferService.end(context)
        }
    }

    companion object {
        @Volatile
        private var shared: CloudBackupOperations? = null

        fun getInstance(context: Context): CloudBackupOperations =
            shared ?: synchronized(this) {
                shared ?: CloudBackupOperations(context.applicationContext).also { shared = it }
            }
    }
}
