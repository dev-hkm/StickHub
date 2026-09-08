package com.hkm.stickhub.ui.i18n

import android.content.Context

enum class AppLanguage(val storageValue: String) {
    ENGLISH("en"),
    VIETNAMESE("vi");

    companion object {
        fun fromStorage(value: String?): AppLanguage =
            entries.firstOrNull { it.storageValue == value } ?: ENGLISH
    }
}

object LanguagePreferences {
    private const val PREFS_NAME = "stickhub_language_preferences"
    private const val KEY_LANGUAGE = "app_language"

    fun get(context: Context): AppLanguage = AppLanguage.fromStorage(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, AppLanguage.ENGLISH.storageValue)
    )

    fun set(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.storageValue)
            .apply()
    }
}
