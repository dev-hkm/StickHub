package com.hkm.stickhub.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.hkm.stickhub.data.provider.WhatsAppStickerProvider

/**
 * Outbound half of WhatsApp's third-party sticker contract (see
 * WhatsApp/stickers sample): fire ENABLE_STICKER_PACK and WhatsApp shows its
 * own confirmation before pulling the pack into its tray. Plus the
 * whitelist-check read that reports whether a pack is already added.
 */
object WhatsAppPackIntents {
    const val ACTION_ENABLE_STICKER_PACK = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
    const val EXTRA_PACK_ID = "sticker_pack_id"
    const val EXTRA_PACK_AUTHORITY = "sticker_pack_authority"
    const val EXTRA_PACK_NAME = "sticker_pack_name"

    private const val WHITELIST_AUTHORITY = "com.whatsapp.provider.sticker_whitelist_check"
    private const val WHITELIST_BUSINESS_AUTHORITY = "com.whatsapp.w4b.provider.sticker_whitelist_check"
    private const val COL_RESULT = "result"

    fun enableIntent(pack: WhatsAppStickerPack): android.content.Intent {
        return android.content.Intent(ACTION_ENABLE_STICKER_PACK).apply {
            putExtra(EXTRA_PACK_ID, pack.identifier)
            putExtra(EXTRA_PACK_AUTHORITY, WhatsAppStickerProvider.AUTHORITY)
            putExtra(EXTRA_PACK_NAME, pack.name)
        }
    }

    fun whitelistUri(packId: String, business: Boolean = false): Uri {
        val authority = if (business) WHITELIST_BUSINESS_AUTHORITY else WHITELIST_AUTHORITY
        return Uri.parse(
            "content://$authority/is_whitelisted" +
                "?authority='${WhatsAppStickerProvider.AUTHORITY}'&identifier='$packId'"
        )
    }

    /**
     * True when the pack is already in the user's WhatsApp, false when it is
     * not, null when unknowable (WhatsApp absent, too old, or query failed).
     * Never throws; safe to call off the main thread.
     */
    fun isPackAdded(context: Context, packId: String): Boolean? {
        return isPackAddedIn(context, whitelistUri(packId, business = false))
            ?: isPackAddedIn(context, whitelistUri(packId, business = true))
    }

    private fun isPackAddedIn(context: Context, uri: Uri): Boolean? {
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(uri, null, null, null, null)
                ?: return null
            if (!cursor.moveToFirst()) return null
            val index = cursor.getColumnIndex(COL_RESULT)
            if (index < 0) return null
            when (cursor.getInt(index)) {
                1 -> true
                0 -> false
                else -> null
            }
        } catch (_: Exception) {
            null
        } finally {
            try {
                cursor?.close()
            } catch (_: Exception) {
            }
        }
    }
}
