package com.hkm.stickhub.util

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build

/** Which envelope a harvested URI was found in. */
enum class CandidateSource {
    DIRECT_URI,
    TEXT_LINE,
    HTML_SRC,
    INTENT_DATA,
    INTENT_STREAM,
    INTENT_CLIPDATA
}

/**
 * One image URI Android actually exposed, with the exact provenance needed to
 * gate and fingerprint it. Pure data — harvesting never touches a stream.
 */
data class ClipboardCandidate(
    val uri: Uri,
    val stableKey: String,
    val source: CandidateSource,
    val itemIndex: Int,
    val mimeHints: List<String>
)

/**
 * Shared discovery for the clipboard primary ClipData, ACTION_SEND and
 * ACTION_SEND_MULTIPLE. Surfaces every exposed image URI in stable
 * first-seen order.
 *
 * Rules:
 * - Every top-level ClipData item is read, never just item 0.
 * - `Intent.clipData` is parsed recursively with depth + cycle guards.
 * - Exact-URI dedupe keeps first-seen ordering; different URIs are never
 *   merged, even under one authority.
 * - StickHub's own provider is dropped absolutely (never re-import our echo).
 * - No `coerceToText()` on items that already carry a URI or Intent — only
 *   literal text/html payloads are read, so no URI is ever opened as text.
 * - No stream is opened here at all: discovery is pure URI harvesting.
 */
object ClipboardUriHarvester {

    fun harvestClipData(clip: ClipData, maxDepth: Int = 4): List<ClipboardCandidate> {
        val seen = LinkedHashMap<String, ClipboardCandidate>()
        val visited = mutableSetOf<Int>()
        harvestClipInto(clip, depth = 0, maxDepth = maxDepth, visited = visited, out = seen)
        return seen.values.toList()
    }

    fun harvestIntent(intent: Intent, maxDepth: Int = 4): List<ClipboardCandidate> {
        val seen = LinkedHashMap<String, ClipboardCandidate>()
        val visited = mutableSetOf<Int>()
        harvestIntentInto(intent, itemIndex = -1, depth = 0, maxDepth = maxDepth, visited = visited, out = seen)
        return seen.values.toList()
    }

    private fun harvestClipInto(
        clip: ClipData,
        depth: Int,
        maxDepth: Int,
        visited: MutableSet<Int>,
        out: LinkedHashMap<String, ClipboardCandidate>
    ) {
        if (depth > maxDepth) return
        if (!visited.add(System.identityHashCode(clip))) return
        val declared = clipDescriptionMimes(clip)
        for (i in 0 until clip.itemCount) {
            val item = try {
                clip.getItemAt(i)
            } catch (_: Exception) {
                continue
            } ?: continue
            harvestItemInto(item, itemIndex = i, declaredMimes = declared,
                depth = depth, maxDepth = maxDepth, visited = visited, out = out)
        }
    }

    private fun harvestIntentInto(
        intent: Intent,
        itemIndex: Int,
        depth: Int,
        maxDepth: Int,
        visited: MutableSet<Int>,
        out: LinkedHashMap<String, ClipboardCandidate>
    ) {
        if (depth > maxDepth) return
        if (!visited.add(System.identityHashCode(intent))) return
        val typeHint = try {
            intent.type?.takeIf { it.isNotBlank() }?.let { listOf(it) } ?: emptyList()
        } catch (_: Exception) {
            emptyList<String>()
        }
        // A nested ClipData is the authoritative ordered batch. EXTRA_STREAM
        // and data are compatibility mirrors in many ACTION_SEND_MULTIPLE
        // producers, so harvest the ordered batch before those fallbacks.
        val nested = try {
            intent.clipData
        } catch (_: Exception) {
            null
        }
        if (nested != null) {
            harvestClipInto(nested, depth + 1, maxDepth, visited, out)
        }
        try {
            intent.data?.let { addCandidate(out, it, CandidateSource.INTENT_DATA, itemIndex, typeHint) }
        } catch (_: Exception) {
        }
        extractStreamUris(intent).forEach { addCandidate(out, it, CandidateSource.INTENT_STREAM, itemIndex, typeHint) }
        // Free text on a share intent is a last resort, never authoritative.
        try {
            intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        } catch (_: Exception) {
            null
        }?.lineSequence()?.forEach { line ->
            sanitizeUriLine(line)?.let { addCandidate(out, it, CandidateSource.TEXT_LINE, itemIndex, typeHint) }
        }
    }

    private fun harvestItemInto(
        item: ClipData.Item,
        itemIndex: Int,
        declaredMimes: List<String>,
        depth: Int,
        maxDepth: Int,
        visited: MutableSet<Int>,
        out: LinkedHashMap<String, ClipboardCandidate>
    ) {
        val itemUri = try {
            item.uri
        } catch (_: Exception) {
            null
        }
        val itemIntent = try {
            item.intent
        } catch (_: Exception) {
            null
        }
        if (itemUri != null && !isEnvelopePseudoUri(itemUri)) {
            addCandidate(out, itemUri, CandidateSource.DIRECT_URI, itemIndex, declaredMimes)
        }
        // Literal payloads only — never coerce a URI/Intent item to text, which
        // would synthesize fake candidates (e.g. "intent:#Intent;...") and could
        // push a short-lived URI through a text open path.
        val literalText = try {
            item.text?.toString()
        } catch (_: Exception) {
            null
        }
        if (literalText != null) {
            literalText.lineSequence().forEach { line ->
                sanitizeUriLine(line)?.let { addCandidate(out, it, CandidateSource.TEXT_LINE, itemIndex, declaredMimes) }
            }
        }
        // NOTE: no coerceToText() fallback here on purpose. Coercing a URI or
        // Intent item synthesizes fake candidates (e.g. "intent:#Intent;...")
        // and could push a short-lived URI through a text open path.
        try {
            item.htmlText?.toString()
        } catch (_: Exception) {
            null
        }?.let { html ->
            HtmlSrcPattern.findAll(html).forEach { match ->
                sanitizeUriLine(match.value)?.let { addCandidate(out, it, CandidateSource.HTML_SRC, itemIndex, declaredMimes) }
            }
        }
        if (itemIntent != null) {
            harvestIntentInto(itemIntent, itemIndex, depth + 1, maxDepth, visited, out)
        }
    }

    @Suppress("DEPRECATION")
    private fun extractStreamUris(intent: Intent): List<Uri> {
        return try {
            val out = mutableListOf<Uri>()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { out.add(it) }
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { out.addAll(it) }
                    val generic = intent.getParcelableArrayExtra(Intent.EXTRA_STREAM, android.os.Parcelable::class.java)
                    generic?.forEach { if (it is Uri) out.add(it) }
                } else {
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { out.add(it) }
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { out.addAll(it) }
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayExtra(Intent.EXTRA_STREAM)
                        ?.forEach { if (it is Uri) out.add(it) }
                }
            } catch (_: Exception) {
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun addCandidate(
        out: LinkedHashMap<String, ClipboardCandidate>,
        uri: Uri,
        source: CandidateSource,
        itemIndex: Int,
        mimeHints: List<String>
    ) {
        if (ClipboardImportPolicy.isOwnStickerSource(uri.scheme, uri.authority)) return
        val key = uri.toString()
        if (out.containsKey(key)) return
        out[key] = ClipboardCandidate(uri, key, source, itemIndex, mimeHints.toList())
    }

    private fun clipDescriptionMimes(clip: ClipData): List<String> {
        return try {
            val description = clip.description ?: return emptyList()
            buildList {
                for (i in 0 until description.mimeTypeCount) {
                    try {
                        description.getMimeType(i)?.takeIf { it.isNotBlank() }?.let { add(it) }
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun sanitizeUriLine(raw: String): Uri? {
        val trimmed = raw.trim().trimEnd(',', ';', '.', '!', '?', ')', '"', '\'')
        if (trimmed.isEmpty()) return null
        return try {
            Uri.parse(trimmed)
        } catch (_: Exception) {
            null
        }
    }

    private fun isEnvelopePseudoUri(uri: Uri): Boolean {
        return try {
            uri.scheme.equals("intent", ignoreCase = true)
        } catch (_: Exception) {
            true
        }
    }

    private val HtmlSrcPattern = Regex("""(content|file)://[^\s"'<>]+""")
}
