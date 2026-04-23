package com.nhom2.elearnlanguage.presentation.ui.main_app.lessonlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
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
    companion object {
        private const val KEY_LESSON_COMPLETED_EVENT_ID = "lessonCompletedEventId"
    }

    private var _binding: FragmentLessonListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LessonListViewModel by hiltNavGraphViewModels(R.id.nav_main)
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

        adapter = LessonAdapter(
            showCompletionState = !args.fromSpeaking
        ) { lesson ->
            if (args.fromSpeaking) {
                val action = LessonListFragmentDirections
                    .actionLessonListFragmentToAiConversationFragment(
                        lessonId = lesson.id
                    )
                findNavController().navigate(action)
            } else {
                val action = LessonListFragmentDirections.actionLessonListFragmentToQuestionFragment(
                    lessonId = lesson.id
                )
                findNavController().navigate(action)
            }
        }

        binding.rvLessons.adapter = adapter

        binding.backButton.root.setOnClickListener {
            findNavController().navigateUp()
        }

        setUpObservers()

        // Load initial list (cached in VM unless we force refresh).
        viewModel.load(
            topicId = args.topicId,
            topicName = args.topicName,
            topicImageUrl = args.topicImageUrl
        )

        // Refresh exactly when a lesson was completed (invalidate cache → refetch).
        val handle = findNavController().currentBackStackEntry?.savedStateHandle
        handle
            ?.getLiveData<Long>(KEY_LESSON_COMPLETED_EVENT_ID)
            ?.observe(viewLifecycleOwner) { eventId ->
                if (eventId != null && eventId > 0L) {
                    viewModel.load(
                        topicId = args.topicId,
                        topicName = args.topicName,
                        topicImageUrl = args.topicImageUrl,
                        forceRefresh = true
                    )
                }
            }
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

