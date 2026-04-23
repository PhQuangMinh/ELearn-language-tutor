package com.nhom2.elearnlanguage.presentation.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.data.source.local.OnboardingPreferenceManager
import com.nhom2.elearnlanguage.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private var isPagerCallbackRegistered = false

    private val pages by lazy {
        listOf(
            OnboardingPageUi(
                imageRes = R.raw.onboard1,
                titleRes = R.string.onboarding_title_1,
                descriptionRes = R.string.onboarding_desc_1,
                ctaRes = R.string.onboarding_cta_1
            ),
            OnboardingPageUi(
                imageRes = R.raw.onboard2,
                titleRes = R.string.onboarding_title_2,
                descriptionRes = R.string.onboarding_desc_2,
                ctaRes = R.string.onboarding_cta_2
            ),
            OnboardingPageUi(
                imageRes = R.raw.onboard3,
                titleRes = R.string.onboarding_title_3,
                descriptionRes = R.string.onboarding_desc_3,
                ctaRes = R.string.onboarding_cta_3
            )
        )
    }

    private val pagerCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateUi(position)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (OnboardingPreferenceManager.isOnboardingCompleted(requireContext())) {
            navigateToLogin()
            return
        }

        setUpPager()
        setUpActions()
        updateUi(0)
    }

    private fun setUpPager() {
        binding.vpOnboarding.adapter = OnboardingPagerAdapter(pages)
        binding.vpOnboarding.registerOnPageChangeCallback(pagerCallback)
        isPagerCallbackRegistered = true
    }

    private fun setUpActions() {
        binding.btnOnboardingCta.setOnClickListener {
            val currentItem = binding.vpOnboarding.currentItem
            val lastIndex = pages.lastIndex
            if (currentItem < lastIndex) {
                binding.vpOnboarding.currentItem = currentItem + 1
                return@setOnClickListener
            }

            OnboardingPreferenceManager.setOnboardingCompleted(requireContext(), true)
            navigateToLogin()
        }
    }

    private fun updateUi(position: Int) {
        val page = pages[position]
        binding.btnOnboardingCta.setText(page.ctaRes)
        updateIndicators(position)
    }

    private fun updateIndicators(activePosition: Int) {
        val active = ContextCompat.getDrawable(requireContext(), R.drawable.bg_onboarding_indicator_active)
        val inactive = ContextCompat.getDrawable(requireContext(), R.drawable.bg_onboarding_indicator_inactive)
        val indicators = listOf(binding.indicator1, binding.indicator2, binding.indicator3)

        indicators.forEachIndexed { index, view ->
            view.background = if (index == activePosition) active else inactive
        }
    }

    private fun navigateToLogin() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.onboardingFragment, true)
            .build()
        findNavController().navigate(R.id.loginFragment, null, navOptions)
    }

    override fun onDestroyView() {
        if (isPagerCallbackRegistered) {
            binding.vpOnboarding.unregisterOnPageChangeCallback(pagerCallback)
            isPagerCallbackRegistered = false
        }
        _binding = null
        super.onDestroyView()
    }
}
