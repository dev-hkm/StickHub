package com.hkm.stickhub.util

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Small concurrency gate for UI actions that must not be submitted twice.
 * The owner is responsible for releasing it from a `finally` block.
 */
class AsyncActionGate {
    private val acquired = AtomicBoolean(false)

    fun tryAcquire(): Boolean = acquired.compareAndSet(false, true)

    fun release() {
        acquired.set(false)
    }
}
