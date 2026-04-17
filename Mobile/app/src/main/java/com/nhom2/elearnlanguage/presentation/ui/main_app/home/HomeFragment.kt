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
                childFragmentManager.beginTransaction().apply {
                    fragments.forEach { (id, fr) ->
                        if (id == currentTabId) show(fr) else hide(fr)
                    }
                }.commit()
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
                val previousTabId = currentTabId
                currentTabId = itemId

                if (fragments[itemId] == null) {
                    showTab(itemId)
                } else {
                    switchToTab(itemId, previousTabId)
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

        val tx = childFragmentManager.beginTransaction()
        // Lần đầu mở một tab: phải ẩn các tab đã add trước đó, nếu không cả hai cùng visible → không thấy đổi tab.
        fragments.forEach { (id, fr) ->
            if (id != itemId) {
                tx.hide(fr)
            }
        }
        tx.add(R.id.flTabContainer, fragment, "tab_${itemId}")
        tx.show(fragment)
        tx.commit()
    }

    /**
     * @param previousTabId tab đang hiển thị *trước* khi listener gán [currentTabId] sang tab mới
     * (nếu lấy [currentTabId] sau khi đã gán thì trùng fragment đích → hide/show sai, tab không đổi).
     */
    private fun switchToTab(newItemId: Int, previousTabId: Int) {
        val newFragment = fragments[newItemId] ?: return

        childFragmentManager.beginTransaction().apply {
            val previousFragment = fragments[previousTabId]
            if (previousFragment != null) {
                hide(previousFragment)
            } else {
                fragments.forEach { (id, fr) ->
                    if (id != newItemId) hide(fr)
                }
            }
            show(newFragment)
        }.commit()
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
