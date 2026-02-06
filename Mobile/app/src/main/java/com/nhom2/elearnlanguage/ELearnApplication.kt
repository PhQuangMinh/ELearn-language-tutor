package com.nhom2.elearnlanguage

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ELearnApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
