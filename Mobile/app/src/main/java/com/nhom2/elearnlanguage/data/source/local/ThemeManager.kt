package com.nhom2.elearnlanguage.data.source.local

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object ThemeManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_DARK_MODE = "dark_mode"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isDarkMode(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_DARK_MODE, enabled) }
        applyDarkMode(enabled)
    }

    fun applySavedTheme(context: Context) {
        applyDarkMode(isDarkMode(context))
    }

    private fun applyDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
