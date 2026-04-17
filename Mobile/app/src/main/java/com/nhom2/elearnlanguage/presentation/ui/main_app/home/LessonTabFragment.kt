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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentLessonTabBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LessonTabFragment : Fragment() {

    private var _binding: FragmentLessonTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels({ requireParentFragment() })

    private val courseAdapter = CourseAdapter { course ->
        val action = HomeFragmentDirections.actionHomeFragmentToLessonListFragment(
            topicId = course.id,
            topicName = course.title,
            topicImageUrl = course.imageUrl
        )
        requireParentFragment().findNavController().navigate(action)
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

    override fun onResume() {
        super.onResume()
        // Re-read streak from shared in-memory cache when user returns to Home.
        viewModel.refreshCurrentStreak()
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
        val layoutManager = binding.rvCourses.layoutManager as? LinearLayoutManager

        // Trigger load-more when user scrolls the course list itself.
        binding.rvCourses.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || layoutManager == null) return

                val totalItems = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (totalItems > 0 && lastVisible >= totalItems - 3) {
                    viewModel.loadMoreCourses()
                }
            }
        })

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
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is HomeUiState.Loading -> showLoading(true)
                            is HomeUiState.Success -> {
                                showLoading(false)
                                val data = state.data
                                binding.tvGreeting.text = "Hi, ${data.fullName}!"
                                courseAdapter.submitList(data.courses)

                                // If current content does not fill the viewport yet, request next page.
                                binding.rvCourses.post {
                                    if (!binding.rvCourses.canScrollVertically(1)) {
                                        viewModel.loadMoreCourses()
                                    }
                                }
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

                launch {
                    viewModel.currentStreak.collect { streak ->
                        binding.tvCurrentStreak.text = (streak ?: 0).toString()
                    }
                }

                launch {
                    viewModel.streakStatus.collect { status ->
                        binding.ivIllustration.setImageResource(resolveStreakImageRes(status))
                    }
                }
            }
        }
    }

    private fun resolveStreakImageRes(status: StreakStatus): Int {
        val packageName = requireContext().packageName
        val candidateNames = when (status) {
            StreakStatus.ACTIVE -> listOf("streak-active", "streak_active")
            StreakStatus.INACTIVE, StreakStatus.UNKNOWN -> listOf("streak-inactive", "streak_inactive")
        }

        for (name in candidateNames) {
            val resId = resources.getIdentifier(name, "drawable", packageName)
            if (resId != 0) {
                return resId
            }
        }
        return R.drawable.main
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
