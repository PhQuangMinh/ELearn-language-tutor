package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import com.nhom2.elearnlanguage.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.databinding.FragmentSpeakingTabBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SpeakingTabFragment : Fragment() {

    private var _binding: FragmentSpeakingTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels({ requireParentFragment() })

    private val topicAdapter = VocabularyTopicAdapter { topic ->
        val action = HomeFragmentDirections.actionHomeFragmentToLessonListFragment(
            topicId = topic.id,
            topicName = topic.title,
            topicImageUrl = topic.imageUrl,
            fromSpeaking = true
        )
        requireParentFragment().findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeakingTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = topicAdapter
        }

        val layoutManager = binding.rvCourses.layoutManager as? LinearLayoutManager
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
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is HomeUiState.Loading -> showLoading(true)
                        is HomeUiState.Success -> {
                            showLoading(false)
                            topicAdapter.submitList(state.data.courses)
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
