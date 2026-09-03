package com.hkm.stickhub.ui.library

/**
 * Guards the first database refresh for a library composition.
 *
 * StickerRepository intentionally exposes an empty snapshot before its first disk read. The
 * library must therefore claim and run this refresh instead of interpreting that snapshot as a
 * genuinely empty collection.
 */
class LibraryStartupRefreshPolicy {
    private var initialRefreshClaimed = false

    fun claimInitialRefresh(): Boolean {
        if (initialRefreshClaimed) return false
        initialRefreshClaimed = true
        return true
    }
}

sealed interface LibrarySnapshotState {
    data object Loading : LibrarySnapshotState
    data object Ready : LibrarySnapshotState
    data class Failed(val reason: String) : LibrarySnapshotState
}
