package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentLessonTabBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LessonTabFragment : Fragment() {

    private var _binding: FragmentLessonTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val courseAdapter = CourseAdapter { course ->
        Toast.makeText(requireContext(), course.title, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet()
        setupRecyclerViews()
        setupLoadMoreOnScroll()
        observeViewModel()
    }

    private fun setupBottomSheet() {
        val behavior = BottomSheetBehavior.from(binding.bottomSheetContent)
        // Trạng thái ban đầu: COLLAPSED (như trong ảnh mẫu)
        // Collapsed: header ~35-40%, sheet ~60-65% (đủ hiển thị 3 lesson cards)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isGestureInsetBottomIgnored = true
        
        // Khi expanded: sheet che hết header (expandedOffset = 0)
        behavior.expandedOffset = 0

        // Mờ dần header khi kéo sheet lên, ẩn hoàn toàn khi expanded
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Khi expanded, ẩn header hoàn toàn
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.headerSection.alpha = 0f
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.headerSection.alpha = 1f
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // slideOffset: 0.0 = collapsed, 1.0 = expanded
                // Header mờ dần khi sheet kéo lên, ẩn hoàn toàn khi expanded
                val headerAlpha = 1f - slideOffset.coerceIn(0f, 1f)
                binding.headerSection.alpha = headerAlpha
            }
        })
    }

    private fun setupRecyclerViews() {
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = courseAdapter
        }
    }

    private fun setupLoadMoreOnScroll() {
        // Vì rvCourses đang nằm trong NestedScrollView (bottomSheetContent) nên scroll event thực tế là của scrollView.
        binding.bottomSheetContent.setOnScrollChangeListener { v, _, scrollY, _, _ ->
            val scrollView = v as? NestedScrollView ?: return@setOnScrollChangeListener
            val child = scrollView.getChildAt(0) ?: return@setOnScrollChangeListener

            val distanceToBottom = child.measuredHeight - scrollView.height - scrollY
            // Khi còn <= 200px tới đáy thì load thêm
            if (distanceToBottom <= 200) {
                viewModel.loadMoreCourses()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is HomeUiState.Loading -> showLoading(true)
                        is HomeUiState.Success -> {
                            showLoading(false)
                            val data = state.data
                            binding.tvGreeting.text = "Hi, ${data.fullName}!"
                            courseAdapter.submitList(data.courses)
                        }
                        is HomeUiState.Error -> {
                            showLoading(false)
                            binding.tvError.visibility = View.VISIBLE
                            binding.tvError.text = state.message
                                ?: getString(R.string.error_load_home)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvError.visibility = View.GONE
        binding.rvCourses.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
