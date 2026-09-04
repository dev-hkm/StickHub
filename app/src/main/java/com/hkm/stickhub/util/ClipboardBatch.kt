package com.hkm.stickhub.util

import android.net.Uri

/** Where a batch snapshot came from. */
enum class BatchOrigin {
    CLIPBOARD,
    SHARE
}

/** Why a harvested candidate was refused at discovery (safe reason codes). */
enum class RejectReason {
    UNSUPPORTED_SCHEME,
    NON_IMAGE_MIME
}

/** A harvested candidate refused at discovery, with its source position. */
data class RejectedCandidate(
    val itemIndex: Int,
    val reason: RejectReason
)

/**
 * One frozen clipboard/share observation. The fingerprint binds the ordered
 * candidate keys, the count and the timestamp together, so neither a bare
 * timestamp nor a bare list can alias two different observations into one.
 */
data class ClipboardBatchSnapshot(
    val generation: Long,
    val sourceItemCount: Int,
    val candidates: List<ClipboardCandidate>,
    val rejected: List<RejectedCandidate>,
    val stamp: Long,
    val fingerprint: String,
    val origin: BatchOrigin
) {
    val uris: List<Uri> get() = candidates.map { it.uri }
}

/** One inbound Android share event. The id makes repeated identical shares visible to Compose. */
data class IncomingShareBatch(
    val id: Long,
    val snapshot: ClipboardBatchSnapshot
)

/**
 * Discovery gate (no streams opened): scheme allow-list plus MIME hints.
 * Unknown types (null/empty/application-octet-stream) stay INCLUDED — the
 * staged bytes are the final authority, never a blind resolver answer.
 */
object ClipboardBatchFactory {

    fun fingerprint(stamp: Long, orderedKeys: List<String>): String {
        // Kept in memory only. Avoid a hash collision turning a genuinely new
        // batch into a repeated clipboard callback.
        return "$stamp|${orderedKeys.size}|${orderedKeys.joinToString("\u0001")}"
    }

    fun build(
        generation: Long,
        origin: BatchOrigin,
        sourceItemCount: Int,
        stamp: Long,
        harvested: List<ClipboardCandidate>,
        resolveMimeType: (Uri) -> String?
    ): ClipboardBatchSnapshot {
        val candidates = mutableListOf<ClipboardCandidate>()
        val rejected = mutableListOf<RejectedCandidate>()
        for (candidate in harvested) {
            val uri = candidate.uri
            val scheme = try {
                uri.scheme
            } catch (_: Exception) {
                null
            }
            if (!scheme.equals("content", ignoreCase = true) &&
                !scheme.equals("file", ignoreCase = true)
            ) {
                // Own-provider URIs were already dropped by the harvester; anything
                // else outside content/file is reported, never imported.
                rejected.add(RejectedCandidate(candidate.itemIndex, RejectReason.UNSUPPORTED_SCHEME))
                continue
            }
            val resolved = try {
                resolveMimeType(uri)
            } catch (_: Exception) {
                null
            }
            val hintedImage = candidate.mimeHints.any(::isImageMime)
            if (isDefinitelyNotImage(resolved) && !hintedImage) {
                rejected.add(RejectedCandidate(candidate.itemIndex, RejectReason.NON_IMAGE_MIME))
                continue
            }
            candidates.add(candidate)
        }
        return ClipboardBatchSnapshot(
            generation = generation,
            sourceItemCount = sourceItemCount,
            candidates = candidates,
            rejected = rejected,
            stamp = stamp,
            fingerprint = fingerprint(stamp, candidates.map { it.stableKey }),
            origin = origin
        )
    }

    private fun isImageMime(mimeType: String?): Boolean {
        return mimeType?.trim()?.lowercase()?.startsWith("image/") == true
    }

    private fun isDefinitelyNotImage(resolved: String?): Boolean {
        if (resolved.isNullOrBlank()) return false
        val lower = resolved.trim().lowercase()
        if (lower == "application/octet-stream") return false
        return !lower.startsWith("image/")
    }
}

/**
 * Deterministic offer arbitration. Pure state machine — no clocks, no jobs —
 * so out-of-order scan completions and sheet-open races are exactly testable.
 *
 * - A scan older than the newest seen generation is stale, always ignored.
 * - An identical fingerprint is a repeated callback, ignored.
 * - While a review sheet is open the current offer is frozen; the newcomer is
 *   held as pending and surfaces on close. Pending is never silently marked
 *   seen-and-dropped.
 */
class ClipboardOfferReducer {

    data class State(
        val lastGeneration: Long = -1L,
        val current: ClipboardBatchSnapshot? = null,
        val pending: ClipboardBatchSnapshot? = null,
        val reviewOpen: Boolean = false
    )

    sealed interface Effect {
        data object Ignore : Effect
        data class Show(val snapshot: ClipboardBatchSnapshot) : Effect
        data class HoldPending(val snapshot: ClipboardBatchSnapshot) : Effect
    }

    sealed interface Event {
        data class ScanArrived(val snapshot: ClipboardBatchSnapshot) : Event
        data object ReviewOpened : Event
        data object ReviewClosed : Event
    }

    fun reduce(state: State, event: Event): Pair<State, Effect> {
        return when (event) {
            is Event.ScanArrived -> reduceScan(state, event.snapshot)
            Event.ReviewOpened -> state.copy(reviewOpen = true) to Effect.Ignore
            Event.ReviewClosed -> {
                val nowOpen = state.copy(reviewOpen = false)
                val held = nowOpen.pending
                if (held != null && held.fingerprint != nowOpen.current?.fingerprint) {
                    nowOpen.copy(current = held, pending = null) to Effect.Show(held)
                } else {
                    nowOpen.copy(pending = null) to Effect.Ignore
                }
            }
        }
    }

    private fun reduceScan(state: State, snapshot: ClipboardBatchSnapshot): Pair<State, Effect> {
        if (snapshot.generation <= state.lastGeneration) {
            return state to Effect.Ignore
        }
        val advanced = state.copy(lastGeneration = snapshot.generation)
        val sameAsCurrent = snapshot.fingerprint == advanced.current?.fingerprint
        val sameAsPending = snapshot.fingerprint == advanced.pending?.fingerprint
        if (sameAsCurrent || sameAsPending) return advanced to Effect.Ignore
        return if (advanced.reviewOpen) {
            advanced.copy(pending = snapshot) to Effect.HoldPending(snapshot)
        } else {
            advanced.copy(current = snapshot, pending = null) to Effect.Show(snapshot)
        }
    }
}
