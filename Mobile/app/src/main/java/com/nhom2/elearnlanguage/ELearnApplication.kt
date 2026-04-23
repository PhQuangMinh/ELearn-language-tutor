package com.nhom2.elearnlanguage

import android.app.Application
import com.nhom2.elearnlanguage.data.source.local.ThemeManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ELearnApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.applySavedTheme(this)
    }
}
