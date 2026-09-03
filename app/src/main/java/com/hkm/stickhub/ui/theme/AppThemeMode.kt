package com.hkm.stickhub.ui.theme

/**
 * Supported app-level theme override modes.
 */
enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromString(value: String?): AppThemeMode {
            if (value.isNullOrBlank()) return SYSTEM
            return entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) } ?: SYSTEM
        }
    }
}
