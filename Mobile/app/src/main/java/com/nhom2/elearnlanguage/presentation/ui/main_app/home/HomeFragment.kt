package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var currentTabId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNav()
        if (savedInstanceState == null) {
            binding.root.post {
                showTab(R.id.tab_lesson)
                binding.bottomNav.selectedItemId = R.id.tab_lesson
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val itemId = item.itemId
            if (itemId == currentTabId) {
                return@setOnItemSelectedListener true
            }
            if (itemId == R.id.tab_lesson || itemId == R.id.tab_vocabulary ||
                itemId == R.id.tab_speaking || itemId == R.id.tab_profile
            ) {
                showTab(itemId)
                currentTabId = itemId
            }
            true
        }
    }

    private fun showTab(itemId: Int) {
        val fragment = when (itemId) {
            R.id.tab_lesson -> LessonTabFragment()
            R.id.tab_vocabulary -> VocabularyTabFragment()
            R.id.tab_speaking -> SpeakingTabFragment()
            R.id.tab_profile -> ProfileTabFragment()
            else -> return
        }
        currentTabId = itemId
        childFragmentManager.beginTransaction()
            .replace(R.id.flTabContainer, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
