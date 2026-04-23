package com.nhom2.elearnlanguage.data.source.local

import android.content.Context
import androidx.core.content.edit

object OnboardingPreferenceManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isOnboardingCompleted(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        prefs(context).edit { putBoolean(KEY_ONBOARDING_COMPLETED, completed) }
    }
}
