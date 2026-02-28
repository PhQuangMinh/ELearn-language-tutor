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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentListenQuestionBinding
import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.domain.model.lesson.QuestionType
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.AnswerSlotAdapter
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.ProgressSegmentAdapter
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.WordChoiceAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListenQuestionFragment : Fragment() {

    private var _binding: FragmentListenQuestionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LessonViewModel by activityViewModels()
    private val args: ListenQuestionFragmentArgs by navArgs()
    private lateinit var progressAdapter: ProgressSegmentAdapter
    private lateinit var answerSlotAdapter: AnswerSlotAdapter
    private lateinit var wordChoiceAdapter: WordChoiceAdapter
    private var hasInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListenQuestionBinding.inflate(inflater, container, false)
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

        binding.btnCheckAnswer.setOnClickListener {
            moveNextAndNavigateByType()
        }

        binding.btnNextQuestion.setOnClickListener {
            moveNextAndNavigateByType()
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

        binding.tvQuestionTitle.text = currentQuestion.content

        setupArrangeFromContent(currentQuestion)
    }

    private fun setupArrangeFromContent(question: Question) {
        val words = question.getWordsForBank()

        if (words.isEmpty()) {
            return
        }

        val answerSlots = words.mapIndexed { index, word ->
            AnswerSlotAdapter.AnswerSlotItem(index = index, correctWord = word)
        }
        val shuffledWordItems = words.shuffled().map { word ->
            WordChoiceAdapter.WordChoiceItem(word = word)
        }

        answerSlotAdapter = AnswerSlotAdapter(
            initialData = answerSlots,
            onSlotClicked = { _, removedWord ->
                wordChoiceAdapter.markWordAsAvailable(removedWord)
            }
        )
        binding.fbBlankWords.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = answerSlotAdapter
        }

        wordChoiceAdapter = WordChoiceAdapter(
            initialWords = shuffledWordItems,
            onWordClicked = { word ->
                val emptyIndex = answerSlotAdapter.getAnswerText().indexOfFirst { it.isBlank() }
                if (emptyIndex != -1) {
                    answerSlotAdapter.updateSlot(emptyIndex, word)
                }
            }
        )
        binding.fbWords.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = wordChoiceAdapter
        }

    }

    private fun routeToCurrentQuestionTypeIfNeeded(type: QuestionType): Boolean {
        return when (type) {
            QuestionType.LISTEN_AND_ARRANGE_SENTENCE -> false
            QuestionType.ONE_SELECTION -> {
                findNavController().navigate(
                    R.id.action_listenQuestionFragment_to_multipleChoiceQuestionFragment,
                    bundleOf("lessonId" to args.lessonId)
                )
                true
            }
            QuestionType.TRANSLATE_AND_ARRANGE_SENTENCE -> {
                findNavController().navigate(
                    R.id.action_listenQuestionFragment_to_arrangeWordsQuestionFragment,
                    bundleOf("lessonId" to args.lessonId)
                )
                true
            }
        }
    }

    private fun isCurrentFragmentType(type: QuestionType): Boolean {
        return type == QuestionType.LISTEN_AND_ARRANGE_SENTENCE
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