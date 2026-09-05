package com.hkm.stickhub.ime

import android.content.Context
import android.view.inputmethod.InputMethodManager

/**
 * Thin system glue reporting StickHub Keyboard's enablement state for
 * Settings. Two binder reads, no caching: callers re-read on resume.
 */
object StickerImeStatus {

    data class Status(
        val enabled: Boolean,
        val active: Boolean
    )

    fun imeId(context: Context): String {
        return "${context.packageName}/.ime.StickerInputMethodService"
    }

    fun read(context: Context): Status {
        return try {
            val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                as? InputMethodManager ?: return Status(false, false)
            val id = imeId(context)
            val enabled = try {
                manager.enabledInputMethodList.any { it.id == id }
            } catch (_: Exception) {
                false
            }
            // Reading the secure default IME can throw for non-system callers
            // on some builds; treat unreadable as simply "not active".
            val active = try {
                android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.DEFAULT_INPUT_METHOD
                ) == id
            } catch (_: Exception) {
                false
            }
            Status(enabled, active)
        } catch (_: Exception) {
            Status(false, false)
        }
    }
}
