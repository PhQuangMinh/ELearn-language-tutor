package com.nhom2.elearnlanguage.presentation.utils

import android.content.res.Resources

fun Int.dpToPx(): Int =
    (this * Resources.getSystem().displayMetrics.density).toInt()