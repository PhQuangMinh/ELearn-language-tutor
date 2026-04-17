package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.nhom2.elearnlanguage.databinding.FragmentVocabularyTabBinding
import com.nhom2.elearnlanguage.presentation.ui.main_app.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VocabularyTabFragment : Fragment() {

    private var _binding: FragmentVocabularyTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels({ requireParentFragment() })
    private var allCourses: List<com.nhom2.elearnlanguage.domain.model.CourseProgress> = emptyList()

    private val topicAdapter = VocabularyTopicAdapter { course ->
        val action = HomeFragmentDirections.actionHomeFragmentToTopicVocabularyFragment(
            topicId = course.id,
            topicName = course.title
        )
        requireParentFragment().findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVocabularyTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheet()
        setupRecyclerViews()
        setupLoadMoreOnScroll()
        observeViewModel()
    }

    private fun renderCourses(courses: List<com.nhom2.elearnlanguage.domain.model.CourseProgress>) {
        topicAdapter.submitList(courses)
    }

    private fun setupBottomSheet() {
        val behavior = BottomSheetBehavior.from(binding.bottomSheetContent)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isGestureInsetBottomIgnored = true
        behavior.expandedOffset = 0
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.headerSection.alpha = 0f
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.headerSection.alpha = 1f
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.headerSection.alpha = 1f - slideOffset.coerceIn(0f, 1f)
            }
        })
    }

    private fun setupRecyclerViews() {
        binding.rvTopic.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = topicAdapter
        }
    }

    private fun setupLoadMoreOnScroll() {
        val layoutManager = binding.rvTopic.layoutManager as? LinearLayoutManager

        // Trigger load-more when user scrolls the topic list itself.
        binding.rvTopic.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

        // rvTopic nằm trong NestedScrollView (bottomSheetContent) nên scroll event thực tế thường là của scrollView.
        binding.bottomSheetContent.setOnScrollChangeListener { v, _, scrollY, _, _ ->
            val scrollView = v as? NestedScrollView ?: return@setOnScrollChangeListener
            val child = scrollView.getChildAt(0) ?: return@setOnScrollChangeListener

            val distanceToBottom = child.measuredHeight - scrollView.height - scrollY
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
                            val courses = state.data.courses
                            allCourses = courses
                            renderCourses(courses)

                            // If current content does not fill the viewport yet, request next page.
                            binding.rvTopic.post {
                                if (!binding.rvTopic.canScrollVertically(1)) {
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
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.tvError.visibility = View.GONE
        binding.rvTopic.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.tvTopicSection.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
