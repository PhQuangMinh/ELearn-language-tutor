package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentVocabularyTabBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VocabularyTabFragment : Fragment() {

    private var _binding: FragmentVocabularyTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val topicAdapter = VocabularyTopicAdapter { course ->
        val action = HomeFragmentDirections.actionHomeFragmentToTopicVocabularyFragment(
            topicId = course.id,
            topicName = course.title
        )
        requireParentFragment().findNavController().navigate(action)
    }

    private val flashcardAdapter = VocabularyTopicAdapter { course ->
        val action = HomeFragmentDirections.actionHomeFragmentToFlashcardFragment(
            topicId = course.id,
            topicTitle = course.title
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
        observeViewModel()
        viewModel.loadHomeData()
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
        binding.rvFlashcard.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = flashcardAdapter
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
                            topicAdapter.submitList(courses)
                            flashcardAdapter.submitList(courses)
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
        binding.rvFlashcard.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.tvTopicSection.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.tvFlashcardSection.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
