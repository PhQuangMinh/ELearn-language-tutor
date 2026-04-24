package com.nhom2.elearnlanguage.presentation.utils

import android.content.res.Resources
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun Int.dpToPx(): Int =
    (this * Resources.getSystem().displayMetrics.density).toInt()

fun View.applyBottomSystemBarInsetPadding() {
    val initialPaddingLeft = paddingLeft
    val initialPaddingTop = paddingTop
    val initialPaddingRight = paddingRight
    val initialPaddingBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(
            initialPaddingLeft,
            initialPaddingTop,
            initialPaddingRight,
            initialPaddingBottom + systemBars.bottom,
        )
        insets
    }

    ViewCompat.requestApplyInsets(this)
}