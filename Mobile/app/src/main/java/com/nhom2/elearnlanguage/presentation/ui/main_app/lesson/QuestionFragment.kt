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
import com.nhom2.elearnlanguage.databinding.FragmentQuestionBinding
import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.domain.model.lesson.QuestionType
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.AnswerSlotAdapter
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.ProgressSegmentAdapter
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.WordChoiceAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import com.nhom2.elearnlanguage.presentation.utils.dpToPx

@AndroidEntryPoint
class QuestionFragment : Fragment() {

    private var _binding: FragmentQuestionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LessonViewModel by activityViewModels()
    private val args: QuestionFragmentArgs by navArgs()
    private lateinit var progressAdapter: ProgressSegmentAdapter
    
    // Adapters for arrange/listen questions
    private var answerSlotAdapter: AnswerSlotAdapter? = null
    private var wordChoiceAdapter: WordChoiceAdapter? = null
    
    private var hasInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
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
                        if (!::progressAdapter.isInitialized) {
                            setupProgressBar()
                        }
                    }
                    displayCurrentQuestion(currentQuestion)
                }
            }
        }

        // Observe progress changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentQuestionIndex.collect {
                val currentQuestion = viewModel.getCurrentQuestion() ?: return@collect
                // Ensure progress bar is setup when returning to this fragment
                if (!::progressAdapter.isInitialized) {
                    setupProgressBar()
                }
                updateProgress()
                displayCurrentQuestion(currentQuestion)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.root.setOnClickListener {
            findNavController().popBackStack()
        }

        // Multiple choice buttons
        with(binding) {
            btn1.setOnClickListener {
                if (binding.multipleChoiceContainer.isVisible) {
                    moveNextQuestion()
                }
            }
            btn2.setOnClickListener {
                if (binding.multipleChoiceContainer.isVisible) {
                    moveNextQuestion()
                }
            }
            btn3.setOnClickListener {
                if (binding.multipleChoiceContainer.isVisible) {
                    moveNextQuestion()
                }
            }
            btn4.setOnClickListener {
                if (binding.multipleChoiceContainer.isVisible) {
                    moveNextQuestion()
                }
            }

            // Arrange/Listen question buttons
            btnCheckAnswer.setOnClickListener {
                moveNextQuestion()
            }

            btnNextQuestion.setOnClickListener {
                moveNextQuestion()
            }

            btnNextQuestionCorrect.setOnClickListener {
                moveNextQuestion()
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

    private fun displayCurrentQuestion(currentQuestion: Question) {
        when (currentQuestion.type) {
            QuestionType.ONE_SELECTION -> displayMultipleChoice(currentQuestion)
            QuestionType.LISTEN_AND_ARRANGE_SENTENCE -> displayListenQuestion(currentQuestion)
            QuestionType.TRANSLATE_AND_ARRANGE_SENTENCE -> displayArrangeQuestion(currentQuestion)
        }
    }

    private fun displayMultipleChoice(currentQuestion: Question) {
        // Hide other question types
        binding.multipleChoiceContainer.visibility = View.VISIBLE
        binding.arrangeContainer.visibility = View.GONE

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

    private fun displayListenQuestion(currentQuestion: Question) {
        // Hide multiple choice
        binding.multipleChoiceContainer.visibility = View.GONE
        binding.arrangeContainer.visibility = View.VISIBLE

        binding.tvQuestionTitle.text = currentQuestion.content
        
        // Hide speech bubble and arrange avatar for listen question
        binding.tvSentence.visibility = View.GONE
        binding.ivAvatar.visibility = View.GONE
        binding.ivAvatarListen.visibility = View.VISIBLE
        
        // Load avatar image if media exists
        binding.ivAvatarListen.setImageResource(R.drawable.volume_up_24px)
        val params = binding.ivAvatarListen.layoutParams
        params.width = 48.dpToPx()
        params.height = 48.dpToPx()
        binding.ivAvatarListen.layoutParams = params

        setupArrangeUI(currentQuestion)
    }

    private fun displayArrangeQuestion(currentQuestion: Question) {
        // Hide multiple choice
        binding.multipleChoiceContainer.visibility = View.GONE
        binding.arrangeContainer.visibility = View.VISIBLE

        binding.tvQuestionTitle.text = resources.getString(R.string.arrange_question_title)
        
        // Show speech bubble and arrange avatar for arrange question
        binding.tvSentence.visibility = View.VISIBLE
        binding.tvSentence.text = currentQuestion.content
        binding.ivAvatar.visibility = View.VISIBLE
        binding.ivAvatarListen.visibility = View.GONE
        
        // Load avatar image if media exists
        if (currentQuestion.media != null) {
            binding.ivAvatar.load(currentQuestion.media.url) {
                crossfade(true)
                error(R.drawable.visibility_24px)
            }
        } else {
            binding.ivAvatar.setImageResource(R.drawable.visibility_24px)
        }

        setupArrangeUI(currentQuestion)
    }

    private fun setupArrangeUI(question: Question) {
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
                wordChoiceAdapter?.markWordAsAvailable(removedWord)
            }
        )
        binding.fbBlankWords.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = answerSlotAdapter
        }

        wordChoiceAdapter = WordChoiceAdapter(
            initialWords = shuffledWordItems,
            onWordClicked = { word ->
                val emptyIndex = answerSlotAdapter?.getAnswerText()?.indexOfFirst { it.isBlank() } ?: -1
                if (emptyIndex != -1) {
                    answerSlotAdapter?.updateSlot(emptyIndex, word)
                }
            }
        )
        binding.fbWords.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = wordChoiceAdapter
        }
    }

    private fun moveNextQuestion() {
        val nextQuestion = viewModel.getNextQuestion() ?: return
        viewModel.moveToNextQuestion()
        updateProgress()
        displayCurrentQuestion(nextQuestion)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasInitialized = false
        _binding = null
    }
}
