package com.nhom2.elearnlanguage.presentation.ui.main_app.lesson

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentMultipleChoiceQuestionBinding
import com.nhom2.elearnlanguage.domain.model.lesson.QuestionType
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.ProgressSegmentAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MultipleChoiceQuestionFragment : Fragment() {

    private var _binding: FragmentMultipleChoiceQuestionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LessonViewModel by activityViewModels()
    private val args: MultipleChoiceQuestionFragmentArgs by navArgs()
    private lateinit var progressAdapter: ProgressSegmentAdapter
    private var hasInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMultipleChoiceQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Load questions if not already loaded
        if (viewModel.questionsState.value is LessonQuestionsState.Initial) {
            viewModel.loadLessonQuestions(args.lessonId)
        }
        
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.questionsState.collect { state ->
                if (state is LessonQuestionsState.Success) {
                    val currentQuestion = viewModel.getCurrentQuestion() ?: return@collect
                    // Setup progress bar on initial load
                    if (!hasInitialized) {
                        hasInitialized = true
                        if (routeToCurrentQuestionTypeIfNeeded(currentQuestion.type)) return@collect
                        if (!::progressAdapter.isInitialized) {
                            setupProgressBar()
                        }
                    }
                    displayCurrentQuestion()
                }
            }
        }

        // Observe progress changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentQuestionIndex.collect {
                val currentQuestion = viewModel.getCurrentQuestion() ?: return@collect
                if (!isCurrentFragmentType(currentQuestion.type)) return@collect
                // Ensure progress bar is setup when returning to this fragment
                if (!::progressAdapter.isInitialized) {
                    setupProgressBar()
                }
                updateProgress()
                displayCurrentQuestion()
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.root.setOnClickListener {
            viewModel.moveToPreviousQuestion()
            findNavController().popBackStack()
        }

        binding.btnNextQuestionCorrect.setOnClickListener {
            moveNextAndNavigateByType()
        }
        
        // Click any choice button to move to next question
        with (binding) {
            btn1.setOnClickListener {
                moveNextAndNavigateByType()
            }
            btn2.setOnClickListener {
                moveNextAndNavigateByType()
            }
            btn3.setOnClickListener {
                moveNextAndNavigateByType()
            }
            btn4.setOnClickListener {
                moveNextAndNavigateByType()
            }
        }
    }

    private fun setupProgressBar() {
        val totalQuestions = viewModel.getTotalQuestions()
        progressAdapter = ProgressSegmentAdapter(totalQuestions)
        
        binding.rvProgressBar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = progressAdapter
        }
        
        updateProgress()
    }

    private fun updateProgress() {
        if (::progressAdapter.isInitialized) {
            progressAdapter.updateProgress(viewModel.getProgressCount())
        }
    }

    private fun displayCurrentQuestion() {
        val currentQuestion = viewModel.getCurrentQuestion() ?: return
        
        // Display question content
        binding.tvContent.text = currentQuestion.content
        
        // Load image if media exists
        if (currentQuestion.media != null) {
            binding.ivQuestion.load(currentQuestion.media.url) {
                crossfade(true)
                error(R.drawable.visibility_24px)
            }
        } else {
            binding.ivQuestion.setImageResource(R.drawable.visibility_24px)
        }
        
        // Set answer button texts from real data
        if (currentQuestion.answers.size >= 4) {
            binding.btn1.text = currentQuestion.answers[0].content
            binding.btn2.text = currentQuestion.answers[1].content
            binding.btn3.text = currentQuestion.answers[2].content
            binding.btn4.text = currentQuestion.answers[3].content
        }
    }

    private fun routeToCurrentQuestionTypeIfNeeded(type: QuestionType): Boolean {
        return when (type) {
            QuestionType.ONE_SELECTION -> false
            QuestionType.LISTEN_AND_ARRANGE_SENTENCE -> {
                findNavController().navigate(
                    R.id.action_multipleChoiceQuestionFragment_to_listenQuestionFragment,
                    bundleOf("lessonId" to args.lessonId)
                )
                true
            }
            QuestionType.TRANSLATE_AND_ARRANGE_SENTENCE -> {
                findNavController().navigate(
                    R.id.action_multipleChoiceQuestionFragment_to_arrangeWordsQuestionFragment,
                    bundleOf("lessonId" to args.lessonId)
                )
                true
            }
        }
    }

    private fun isCurrentFragmentType(type: QuestionType): Boolean {
        return type == QuestionType.ONE_SELECTION
    }

    private fun moveNextAndNavigateByType() {
        val nextQuestion = viewModel.getNextQuestion() ?: return
        viewModel.moveToNextQuestion()
        if (!routeToCurrentQuestionTypeIfNeeded(nextQuestion.type)) {
            updateProgress()
            displayCurrentQuestion()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasInitialized = false
        _binding = null
    }
}