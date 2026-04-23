package com.nhom2.elearnlanguage.presentation.ui.onboarding

import androidx.annotation.RawRes
import androidx.annotation.StringRes

data class OnboardingPageUi(
    @RawRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val ctaRes: Int
)
