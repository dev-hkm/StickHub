package com.hkm.stickhub.data.cutout

/** Reset and publication share a lock, so stale callbacks cannot race a newer session. */
class CutoutRequestGate {
    private var generation = 0L

    @Synchronized
    fun begin(onBegin: () -> Unit): Long {
        generation += 1
        onBegin()
        return generation
    }

    @Synchronized
    fun isCurrent(requestId: Long): Boolean = generation == requestId

    @Synchronized
    fun publish(requestId: Long, publish: () -> Unit): Boolean {
        if (requestId != generation) return false
        publish()
        return true
    }
}
