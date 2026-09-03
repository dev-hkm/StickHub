package com.hkm.stickhub.ui.library

class LayoutPickerState(
    initialMode: StickerLibraryViewMode
) {
    var committedMode: StickerLibraryViewMode = initialMode
        private set

    var pendingMode: StickerLibraryViewMode? = null
        private set

    var isDismissing: Boolean = false
        private set

    var isVisible: Boolean = false
        private set

    fun open() {
        if (!isDismissing) {
            pendingMode = null
            isVisible = true
        }
    }

    /**
     * Called when a mode is tapped.
     * Returns true if a mutation should occur (mode changed).
     */
    fun select(mode: StickerLibraryViewMode): Boolean {
        if (isDismissing) return false
        if (mode == committedMode) {
            isDismissing = true
            return false
        }
        pendingMode = mode
        isDismissing = true
        return true
    }

    fun startDismiss() {
        isDismissing = true
    }

    /**
     * Called after sheet hide completes and the sheet is fully hidden.
     */
    fun onDismissComplete(): Boolean {
        val selected = pendingMode
        val changed = (selected != null && selected != committedMode)
        if (changed) {
            committedMode = selected!!
        }
        pendingMode = null
        isDismissing = false
        isVisible = false
        return changed
    }
}
