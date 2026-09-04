package com.hkm.stickhub.data.cutout

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CutoutSaveState { Idle, Saving, Failed, Saved }

/** A save belongs to one sheet session. A successful session cannot write twice. */
class CutoutSaveSession {
    private val mutableState = MutableStateFlow(CutoutSaveState.Idle)
    val state = mutableState.asStateFlow()

    suspend fun save(write: suspend () -> Boolean): Boolean {
        val previous = mutableState.value
        if (previous != CutoutSaveState.Idle && previous != CutoutSaveState.Failed) return false
        if (!mutableState.compareAndSet(previous, CutoutSaveState.Saving)) return false
        return try {
            write().also { saved ->
                mutableState.value = if (saved) CutoutSaveState.Saved else CutoutSaveState.Failed
            }
        } catch (cancelled: CancellationException) {
            mutableState.value = CutoutSaveState.Idle
            throw cancelled
        } catch (_: Exception) {
            mutableState.value = CutoutSaveState.Failed
            false
        }
    }
}
