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
import coil.load
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentArrangeWordsQuestionBinding
import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.domain.model.lesson.QuestionType
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.AnswerSlotAdapter
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.ProgressSegmentAdapter
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.WordChoiceAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArrangeWordsQuestionFragment : Fragment() {
    
    private var _binding: FragmentArrangeWordsQuestionBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LessonViewModel by activityViewModels()
    private val args: ArrangeWordsQuestionFragmentArgs by navArgs()
    private lateinit var progressAdapter: ProgressSegmentAdapter

    // Adapters
    private lateinit var answerSlotAdapter: AnswerSlotAdapter
    private lateinit var wordChoiceAdapter: WordChoiceAdapter
    private var hasInitialized = false
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArrangeWordsQuestionBinding.inflate(inflater, container, false)
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

        binding.tvSentence.text = currentQuestion.content
        
        // Load avatar image if media exists
        if (currentQuestion.media != null) {
            binding.ivAvatar.load(currentQuestion.media.url) {
                crossfade(true)
                error(R.drawable.volume_up_24px)
            }
        } else {
            binding.ivAvatar.setImageResource(R.drawable.volume_up_24px)
        }
        
        setupUI(currentQuestion)
    }

    private fun setupUI(question: Question) {
        val correctWords = question.getWordsForBank()
        val answerSlots = correctWords.mapIndexed { index, word ->
            AnswerSlotAdapter.AnswerSlotItem(index = index, correctWord = word)
        }
        val wordItems = correctWords.shuffled().map { word ->
            WordChoiceAdapter.WordChoiceItem(word = word)
        }

        setupAnswerSlotsRecyclerView(answerSlots)
        setupWordChoicesRecyclerView(wordItems)
    }
    
    private fun setupAnswerSlotsRecyclerView(answerSlots: List<AnswerSlotAdapter.AnswerSlotItem>) {
        answerSlotAdapter = AnswerSlotAdapter(
            answerSlots,
            onSlotClicked = { _, removedWord ->
                // Re-enable the corresponding word button
                wordChoiceAdapter.markWordAsAvailable(removedWord)
            }
        )
        
        binding.fbBlankWords.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = answerSlotAdapter
        }
    }
    
    private fun setupWordChoicesRecyclerView(wordItems: List<WordChoiceAdapter.WordChoiceItem>) {
        wordChoiceAdapter = WordChoiceAdapter(
            wordItems,
            onWordClicked = { word ->
                // Find the first empty slot and fill it
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

    private fun setupListeners() {
        binding.btnCheckAnswer.setOnClickListener {
            moveNextAndNavigateByType()
        }
        
        binding.btnNextQuestion.setOnClickListener {
            moveNextAndNavigateByType()
        }
        
        binding.btnBack.root.setOnClickListener {
            viewModel.moveToPreviousQuestion()
            findNavController().popBackStack()
        }
    }

    private fun routeToCurrentQuestionTypeIfNeeded(type: QuestionType): Boolean {
        return when (type) {
            QuestionType.TRANSLATE_AND_ARRANGE_SENTENCE -> false
            QuestionType.ONE_SELECTION -> {
                findNavController().navigate(
                    R.id.action_arrangeWordsQuestionFragment_to_multipleChoiceQuestionFragment,
                    bundleOf("lessonId" to args.lessonId)
                )
                true
            }
            QuestionType.LISTEN_AND_ARRANGE_SENTENCE -> {
                findNavController().navigate(
                    R.id.action_arrangeWordsQuestionFragment_to_listenQuestionFragment,
                    bundleOf("lessonId" to args.lessonId)
                )
                true
            }
        }
    }

    private fun isCurrentFragmentType(type: QuestionType): Boolean {
        return type == QuestionType.TRANSLATE_AND_ARRANGE_SENTENCE
    }

    private fun moveNextAndNavigateByType() {
        val nextQuestion = viewModel.getNextQuestion() ?: return
        viewModel.moveToNextQuestion()
        if (!routeToCurrentQuestionTypeIfNeeded(nextQuestion.type)) {
            updateProgress()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasInitialized = false
        _binding = null
    }
}