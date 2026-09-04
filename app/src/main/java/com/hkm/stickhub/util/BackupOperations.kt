package com.hkm.stickhub.util

import android.content.Context
import android.net.Uri
import com.hkm.stickhub.data.model.CategoryItem
import com.hkm.stickhub.data.model.StickerItem
import com.hkm.stickhub.data.repository.StickerRepository
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

/** Terminal backup states surfaced to the UI. */
sealed interface BackupWorkState {
    data object Idle : BackupWorkState
    data class Running(val label: String) : BackupWorkState
    data class ImportFinished(val imported: Int, val alreadyPresent: Int) : BackupWorkState
    data class ExportFinished(val ok: Boolean) : BackupWorkState
    data class Failed(val message: String) : BackupWorkState
}

/**
 * Application-owned backup runner. Import/export are long operations that
 * must survive rotation: this holder outlives any Activity, owns one
 * cancellable scope (never GlobalScope), serializes concurrent runs behind
 * a mutex, and publishes honest terminal states the UI consumes once.
 * Holds the application context only.
 */
class BackupOperations private constructor(appContext: Context) {
    private val context = appContext
    private val repository = StickerRepository.getInstance(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val opMutex = Mutex()

    private val mutableState = MutableStateFlow<BackupWorkState>(BackupWorkState.Idle)
    val state: StateFlow<BackupWorkState> = mutableState.asStateFlow()

    fun startExport(outputUri: Uri, stickers: List<StickerItem>, categories: List<CategoryItem>) {
        if (mutableState.value is BackupWorkState.Running) return
        mutableState.value = BackupWorkState.Running("Backing up…")
        scope.launch {
            val ok = try {
                opMutex.withLock {
                    BackupHelper.exportBackup(context, outputUri, stickers, categories)
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            mutableState.value = BackupWorkState.ExportFinished(ok)
        }
    }

    fun startImport(inputUri: Uri) {
        if (mutableState.value is BackupWorkState.Running) return
        mutableState.value = BackupWorkState.Running("Restoring backup…")
        scope.launch {
            try {
                val result = opMutex.withLock {
                    BackupHelper.importBackupDetailed(context, inputUri, repository)
                }
                mutableState.value = when (result) {
                    is BackupImportResult.Success ->
                        BackupWorkState.ImportFinished(result.imported, result.alreadyPresent)
                    is BackupImportResult.Invalid -> BackupWorkState.Failed(result.reason)
                    is BackupImportResult.Failed -> BackupWorkState.Failed(result.reason)
                }
            } catch (ce: CancellationException) {
                // A cancelled run reports nothing; the next explicit run starts clean.
                mutableState.value = BackupWorkState.Idle
                throw ce
            } catch (e: Exception) {
                e.printStackTrace()
                mutableState.value =
                    BackupWorkState.Failed(e.localizedMessage ?: "Backup operation failed.")
            }
        }
    }

    /** UI calls this after consuming a terminal state so rotations don't replay it. */
    fun acknowledge() {
        if (mutableState.value !is BackupWorkState.Running) {
            mutableState.value = BackupWorkState.Idle
        }
    }

    fun shutdown() {
        scope.cancel()
    }

    companion object {
        @Volatile
        private var shared: BackupOperations? = null

        fun getInstance(context: Context): BackupOperations =
            shared ?: synchronized(this) {
                shared ?: BackupOperations(context.applicationContext).also { shared = it }
            }
    }
}
