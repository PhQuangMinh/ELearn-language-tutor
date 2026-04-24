package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentHomeBinding
import com.nhom2.elearnlanguage.presentation.ui.main_app.home.profile.ProfileFragment
import com.nhom2.elearnlanguage.presentation.utils.applyBottomSystemBarInsetPadding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    companion object {
        private const val KEY_LESSON_COMPLETED_EVENT_ID = "lessonCompletedEventId"
        private const val KEY_CURRENT_TAB = "currentTabId"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var currentTabId: Int = R.id.tab_lesson
    private val fragments = mutableMapOf<Int, Fragment>()

    private val viewModel: HomeViewModel by viewModels()
    private var lastHandledLessonCompletedEventId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bottomNav.applyBottomSystemBarInsetPadding()
        
        // Khôi phục tab đã lưu trước đó
        if (savedInstanceState != null) {
            currentTabId = savedInstanceState.getInt(KEY_CURRENT_TAB, R.id.tab_lesson)
        }
        
        setupBottomNav()
        
        // Hiển thị tab đã lưu
        binding.root.post {
            binding.bottomNav.selectedItemId = currentTabId
            if (fragments[currentTabId] == null) {
                showTab(currentTabId, null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Only refresh cached home data when we know a lesson completion occurred.
        val handle = findNavController().currentBackStackEntry?.savedStateHandle
        val eventId = handle?.get<Long>(KEY_LESSON_COMPLETED_EVENT_ID) ?: 0L
        if (eventId > 0L && eventId != lastHandledLessonCompletedEventId) {
            lastHandledLessonCompletedEventId = eventId
            viewModel.loadHomeData(forceRefresh = true)
            viewModel.refreshCurrentStreak()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val itemId = item.itemId
            val previousTabId = currentTabId
            if (itemId == previousTabId) {
                return@setOnItemSelectedListener true
            }
            if (itemId == R.id.tab_lesson || itemId == R.id.tab_vocabulary ||
                itemId == R.id.tab_speaking || itemId == R.id.tab_profile
            ) {
                if (fragments[itemId] == null) {
                    showTab(itemId, previousTabId)
                } else {
                    switchToTab(previousTabId, itemId)
                }
                currentTabId = itemId
            }
            true
        }
    }

    private fun showTab(itemId: Int, previousTabId: Int? = null) {
        val fragment = when (itemId) {
            R.id.tab_lesson -> LessonTabFragment()
            R.id.tab_vocabulary -> VocabularyTabFragment()
            R.id.tab_speaking -> SpeakingTabFragment()
            R.id.tab_profile -> ProfileFragment()
            else -> return
        }
        fragments[itemId] = fragment

        val transaction = childFragmentManager.beginTransaction()
        val previousFragment = previousTabId?.let { fragments[it] }
        if (previousFragment != null) {
            transaction.hide(previousFragment)
        }

        transaction
            .add(R.id.flTabContainer, fragment, "tab_${itemId}")
            .commit()
    }

    private fun switchToTab(previousTabId: Int, newTabId: Int) {
        val currentFragment = fragments[previousTabId]
        val newFragment = fragments[newTabId]
        
        if (newFragment == null) return

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
