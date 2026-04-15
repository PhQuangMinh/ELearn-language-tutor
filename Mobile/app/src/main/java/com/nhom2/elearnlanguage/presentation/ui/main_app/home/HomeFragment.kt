package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentHomeBinding
import com.nhom2.elearnlanguage.presentation.ui.main_app.home.profile.ProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var currentTabId: Int = R.id.tab_lesson
    private val fragments = mutableMapOf<Int, Fragment>()

    companion object {
        private const val KEY_CURRENT_TAB = "currentTabId"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Khôi phục tab đã lưu trước đó
        if (savedInstanceState != null) {
            currentTabId = savedInstanceState.getInt(KEY_CURRENT_TAB, R.id.tab_lesson)
        }
        
        setupBottomNav()
        
        // Hiển thị tab đã lưu
        binding.root.post {
            binding.bottomNav.selectedItemId = currentTabId
            if (fragments[currentTabId] == null) {
                showTab(currentTabId)
            } else {
                switchToTab(currentTabId)
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
                currentTabId = itemId
                
                if (fragments[itemId] == null) {
                    showTab(itemId)
                } else {
                    switchToTab(itemId)
                }
            }
            true
        }
    }

    private fun showTab(itemId: Int) {
        val fragment = when (itemId) {
            R.id.tab_lesson -> LessonTabFragment()
            R.id.tab_vocabulary -> VocabularyTabFragment()
            R.id.tab_speaking -> SpeakingTabFragment()
            R.id.tab_profile -> ProfileFragment()
            else -> return
        }
        fragments[itemId] = fragment
        currentTabId = itemId
        
        childFragmentManager.beginTransaction()
            .add(R.id.flTabContainer, fragment, "tab_${itemId}")
            .commit()
    }

    private fun switchToTab(itemId: Int) {
        val currentFragment = fragments[currentTabId]
        val newFragment = fragments[itemId]
        
        if (newFragment == null) return
        
        currentTabId = itemId
        
        val transaction = childFragmentManager.beginTransaction()
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.show(newFragment).commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_TAB, currentTabId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
