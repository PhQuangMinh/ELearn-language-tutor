package com.nhom2.elearnlanguage.presentation.ui.mainscreen.lessonlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentLessonListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LessonListFragment : Fragment() {

    private var _binding: FragmentLessonListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LessonListViewModel by viewModels()
    private val args: LessonListFragmentArgs by navArgs()

    private lateinit var adapter: LessonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LessonAdapter { lesson ->
            Toast.makeText(requireContext(), "Lesson: ${lesson.title}", Toast.LENGTH_SHORT).show()
        }

        binding.rvLessons.adapter = adapter

        binding.backButton.root.setOnClickListener {
            findNavController().navigateUp()
        }

        setUpObservers()
        viewModel.load(topicId = args.topicId, topicName = args.topicName)
    }

    private fun setUpObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LessonListUiState.Idle -> {
                            setLoading(false)
                            binding.tvError.visibility = View.GONE
                        }
                        is LessonListUiState.Loading -> {
                            setLoading(true)
                            binding.tvError.visibility = View.GONE
                        }
                        is LessonListUiState.Success -> {
                            setLoading(false)

                            binding.tvTopicName.text = state.topicName
                            binding.ivTopicImage.load(state.topicImageUrl) {
                                placeholder(R.drawable.app_logo)
                                error(R.drawable.app_logo)
                                crossfade(true)
                            }

                            adapter.submitList(state.lessons)
                        }
                        is LessonListUiState.Error -> {
                            setLoading(false)
                            binding.tvError.visibility = View.VISIBLE
                            binding.tvError.text = state.message ?: getString(R.string.error_generic)
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvLessons.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

