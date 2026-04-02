package com.nhom2.elearnlanguage.presentation.ui.main_app.lesson

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.QuestionFragmentDirections
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentQuestionBinding
import com.nhom2.elearnlanguage.domain.model.lesson.MediaType
import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.domain.model.lesson.QuestionType
import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitRequest
import com.nhom2.elearnlanguage.data.dto.lesson.QuestionAnswerItem
import com.nhom2.elearnlanguage.data.dto.lesson.SubmittedAnswer
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.AnswerSlotAdapter
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.ProgressSegmentAdapter
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.WordChoiceAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import com.nhom2.elearnlanguage.presentation.utils.dpToPx
import androidx.core.content.ContextCompat
import android.view.animation.OvershootInterpolator
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class QuestionFragment : Fragment() {
    companion object {
        private const val TAG = "QuestionFragment"
    }

    private var _binding: FragmentQuestionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LessonViewModel by activityViewModels()
    private val args: QuestionFragmentArgs by navArgs()
    private lateinit var progressAdapter: ProgressSegmentAdapter
    
    // Adapters for arrange/listen questions
    private var answerSlotAdapter: AnswerSlotAdapter? = null
    private var wordChoiceAdapter: WordChoiceAdapter? = null
    
    private var hasInitialized = false

    private var mediaPlayer: MediaPlayer? = null
    private var playingUrl: String? = null
    private var isPreparingAudio: Boolean = false
    private var hasPrefetchedAudio: Boolean = false

    private var sfxPool: SoundPool? = null
    private var sfxCorrectSoundId: Int? = null
    private var sfxIncorrectSoundId: Int? = null
    private var sfxCorrectLoaded: Boolean = false
    private var sfxIncorrectLoaded: Boolean = false
    private var isSubmitting: Boolean = false
    private var hasHandledNoQuestionsState: Boolean = false
    private var hasHandledLoadErrorState: Boolean = false
    private var correctAnswerCount: Int = 0
    private val correctnessByQuestionId: MutableMap<Int, Boolean> = mutableMapOf()

    private val lessonAudioCacheDir: File by lazy {
        File(requireContext().cacheDir, "audio_cache/lesson_${args.lessonId}")
    }

    private val answersByQuestionId: LinkedHashMap<Int, QuestionAnswerItem> = linkedMapOf()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    private var lessonStartedAt: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lessonStartedAt = nowIsoLocalDateTime()
        renderLoadingState(isLoading = true)
        // Luôn load lại câu hỏi khi vào lesson để bắt đầu từ câu đầu tiên
        viewModel.loadLessonQuestions(args.lessonId)

        initFeedbackSfx()
        
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.questionsState.collect { state ->
                when (state) {
                    is LessonQuestionsState.Initial -> Unit
                    is LessonQuestionsState.Loading -> {
                        renderLoadingState(isLoading = true)
                    }
                    is LessonQuestionsState.Error -> {
                        renderLoadingState(isLoading = false)
                        handleLoadErrorState(state.message)
                    }
                    is LessonQuestionsState.Success -> {
                        if (state.questions.isEmpty()) {
                            handleNoQuestionsState()
                            return@collect
                        }

                        val currentQuestion = viewModel.getCurrentQuestion() ?: return@collect
                        renderLoadingState(isLoading = false)

                        if (!hasPrefetchedAudio) {
                            hasPrefetchedAudio = true
                            prefetchLessonAudio(state.questions)
                        }
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
        }

        // Observe progress changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentQuestionIndex.collect {
                val questionState = viewModel.questionsState.value
                if (questionState !is LessonQuestionsState.Success || questionState.questions.isEmpty()) {
                    return@collect
                }
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

    private fun renderLoadingState(isLoading: Boolean) {
        if (_binding == null) return
        binding.pbQuestionLoading.isVisible = isLoading
        binding.scrollQuestionContent.isVisible = !isLoading
        binding.bottomPanel.isVisible = !isLoading
    }

    private fun handleNoQuestionsState() {
        if (hasHandledNoQuestionsState || _binding == null) return
        hasHandledNoQuestionsState = true
        Toast.makeText(requireContext(), getString(R.string.lesson_no_questions), Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun handleLoadErrorState(message: String) {
        if (hasHandledLoadErrorState || _binding == null) return
        hasHandledLoadErrorState = true
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.btnBack.root.setOnClickListener {
            findNavController().popBackStack()
        }

        // Multiple choice buttons
        with(binding) {
            btn1.setOnClickListener {
                if (binding.multipleChoiceContainer.isVisible) {
                    selectMultipleChoice(btn1)
                }
            }
            btn2.setOnClickListener {
                if (binding.multipleChoiceContainer.isVisible) {
                    selectMultipleChoice(btn2)
                }
            }
            btn3.setOnClickListener {
                if (binding.multipleChoiceContainer.isVisible) {
                    selectMultipleChoice(btn3)
                }
            }
            btn4.setOnClickListener {
                if (binding.multipleChoiceContainer.isVisible) {
                    selectMultipleChoice(btn4)
                }
            }

            // Arrange/Listen question buttons
            btnCheckAnswer.setOnClickListener {
                onCheckAnswerClicked()
            }

            btnNextQuestion.setOnClickListener {
                onNextQuestionClicked()
            }

            btnTryAgain.setOnClickListener {
                onTryAgainClicked()
            }

            btnNextQuestionCorrect.setOnClickListener {
                onNextQuestionClicked()
            }
        }
    }

    private fun clearMultipleChoiceSelection() {
        resetMultipleChoiceButtonStyles()
    }

    private fun selectMultipleChoice(selectedView: View) {
        // Ensure we clear any "checked" coloring from a previous check
        resetMultipleChoiceButtonStyles()
        binding.btn1.isSelected = binding.btn1 === selectedView
        binding.btn2.isSelected = binding.btn2 === selectedView
        binding.btn3.isSelected = binding.btn3 === selectedView
        binding.btn4.isSelected = binding.btn4 === selectedView
    }

    private fun resetMultipleChoiceButtonStyles() {
        val buttons = listOf(binding.btn1, binding.btn2, binding.btn3, binding.btn4)
        buttons.forEach { btn ->
            btn.isSelected = false
            btn.setBackgroundResource(R.drawable.button_choice_selector)
        }
    }

    private fun onCheckAnswerClicked() {
        val currentQuestion = viewModel.getCurrentQuestion()
        if (currentQuestion == null) {
            Log.d(TAG, "checkAnswer: currentQuestion=null")
            return
        }

        val isCorrect = when (currentQuestion.type) {
            QuestionType.ONE_SELECTION -> checkOneSelection(currentQuestion)
            QuestionType.LISTEN_AND_ARRANGE_SENTENCE,
            QuestionType.TRANSLATE_AND_ARRANGE_SENTENCE -> checkArrangedSentence(currentQuestion)
        }

        Log.d(TAG, "checkAnswer result=${if (isCorrect) "CORRECT" else "WRONG"} questionId=${currentQuestion.id} type=${currentQuestion.type}")
        val previousResult = correctnessByQuestionId[currentQuestion.id]
        if (previousResult != isCorrect) {
            if (isCorrect) {
                correctAnswerCount++
            } else if (previousResult == true) {
                correctAnswerCount = (correctAnswerCount - 1).coerceAtLeast(0)
            }
            correctnessByQuestionId[currentQuestion.id] = isCorrect
        }
        saveUserAnswer(question = currentQuestion)
        if (currentQuestion.type == QuestionType.ONE_SELECTION) {
            applyOneSelectionCheckedStyle(isCorrect)
        }
        showFeedback(isCorrect = isCorrect, question = currentQuestion)
    }

    private fun saveUserAnswer(question: Question) {
        val entry = when (question.type) {
            QuestionType.ONE_SELECTION -> {
                val selectedIndex = getSelectedOneSelectionIndex()
                val selectedAnswer = selectedIndex?.let { question.answers.getOrNull(it) }
                QuestionAnswerItem(
                    id = question.id,
                    answer = SubmittedAnswer(
                        id = selectedAnswer?.id ?: -1,
                        content = selectedAnswer?.content.orEmpty()
                    )
                )
            }

            QuestionType.LISTEN_AND_ARRANGE_SENTENCE,
            QuestionType.TRANSLATE_AND_ARRANGE_SENTENCE -> {
                val userSentence = getUserArrangedSentence()
                QuestionAnswerItem(
                    id = question.id,
                    answer = SubmittedAnswer(
                        id = -1,
                        content = userSentence
                    )
                )
            }
        }

        answersByQuestionId[question.id] = entry
    }

    private fun getUserArrangedSentence(): String {
        val userWords = answerSlotAdapter?.getAnswerText().orEmpty()
        return userWords
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onNextQuestionClicked() {
        if (viewModel.isLastQuestion()) {
            if (isSubmitting) return
            isSubmitting = true

            binding.btnNextQuestion.isEnabled = false
            binding.btnNextQuestion.isClickable = false
            binding.pbSubmit.isVisible = true

            val payload = buildAggregatedAnswersPayload()
            Log.d(TAG, "Aggregated answers:\n${gson.toJson(payload)}")

            viewLifecycleOwner.lifecycleScope.launch {
                var submittedOk = false
                try {
                    val result = viewModel.submitLessonAnswers(args.lessonId, payload)
                    val submitResult = result.getOrNull()
                    if (result.isSuccess && submitResult != null) {
                        Log.d(
                            TAG,
                            "submitLesson success: currentStreak=${submitResult.currentStreak}, streakExtended=${submitResult.streakExtended}"
                        )
                        submittedOk = true
                        val total = viewModel.getTotalQuestions()
                        val action = QuestionFragmentDirections.actionQuestionFragmentToLessonCompleteFragment(
                            correctCount = correctAnswerCount,
                            totalCount = total,
                            currentStreak = submitResult.currentStreak,
                            streakExtended = submitResult.streakExtended
                        )
                        findNavController().navigate(action)
                    } else {
                        Log.d(TAG, "submitLessonAnswers failed: ${result.exceptionOrNull()?.message}")
                    }
                } finally {
                    // Delete cached audio when user completes the lesson
                    deleteLessonAudioCache()

                    // Stop loading state (allow retry on failure)
                    isSubmitting = false
                    if (_binding != null) {
                        binding.pbSubmit.isVisible = false
                        binding.btnNextQuestion.isEnabled = !submittedOk
                        binding.btnNextQuestion.isClickable = !submittedOk
                    }
                }
            }
            return
        }

        hideFeedback(animated = false)
        moveNextQuestion()
    }

    private fun onTryAgainClicked() {
        val currentQuestion = viewModel.getCurrentQuestion() ?: return
        hideFeedback(animated = false)
        displayCurrentQuestion(currentQuestion)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildAggregatedAnswersPayload(): LessonSubmitRequest {
        val startedAt = lessonStartedAt ?: nowIsoLocalDateTime().also { lessonStartedAt = it }
        val endedAt = nowIsoLocalDateTime()
        return LessonSubmitRequest(
            startedAt = startedAt,
            endedAt = endedAt,
            questionAnswers = answersByQuestionId.values.toList()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowIsoLocalDateTime(): String {
        // Parseable by Java LocalDateTime.parse(...), e.g. 2026-03-01T15:04:05.123
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    private fun applyOneSelectionCheckedStyle(isCorrect: Boolean) {
        val selectedIndex = getSelectedOneSelectionIndex() ?: return
        val selectedButton = when (selectedIndex) {
            0 -> binding.btn1
            1 -> binding.btn2
            2 -> binding.btn3
            else -> binding.btn4
        }
        selectedButton.setBackgroundResource(
            if (isCorrect) R.drawable.button_selected_with_shadow else R.drawable.button_wrong_with_shadow
        )
    }

    private fun getSelectedOneSelectionIndex(): Int? {
        return when {
            binding.btn1.isSelected -> 0
            binding.btn2.isSelected -> 1
            binding.btn3.isSelected -> 2
            binding.btn4.isSelected -> 3
            else -> null
        }
    }

    private fun showFeedback(isCorrect: Boolean, question: Question) {
        stopAudioIfPlaying()
        val correctAnswerContent = question.getCorrectAnswer()?.content?.trim().orEmpty()
        val answerText = buildString {
            append(getString(R.string.feedback_answer_prefix))
            if (correctAnswerContent.isNotBlank()) {
                append(" ")
                append(correctAnswerContent)
            }
        }

        // Block interactions with question content while feedback is visible
        binding.interactionBlocker.isVisible = true

        // Swap bottom UI: hide check button, show feedback
        binding.btnCheckAnswer.isVisible = false
        binding.feedbackContainer.isVisible = true

        binding.tvFeedbackTitle.text = getString(
            if (isCorrect) R.string.feedback_correct_title else R.string.feedback_wrong_title
        )
        binding.tvFeedbackAnswer.text = answerText

        val backgroundRes = if (isCorrect) R.drawable.feedback_correct_background else R.drawable.feedback_wrong_background
        binding.feedbackContainer.setBackgroundResource(backgroundRes)

        val accentColor = ContextCompat.getColor(
            requireContext(),
            if (isCorrect) R.color.status_green else R.color.error_100
        )
        binding.tvFeedbackTitle.setTextColor(accentColor)
        binding.tvFeedbackAnswer.setTextColor(accentColor)

        binding.btnNextQuestion.text =
            getString(if (viewModel.isLastQuestion()) R.string.complete else R.string.next_question)
        binding.btnNextQuestion.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (isCorrect) R.color.status_green else R.color.error_100
        )
        binding.btnTryAgain.isVisible = question.repeatable

        playFeedbackSfx(isCorrect)

        // Animate "slide up"
        binding.feedbackContainer.animate().cancel()
        binding.feedbackContainer.alpha = 0f
        binding.feedbackContainer.post {
            val startY = binding.feedbackContainer.height.toFloat().coerceAtLeast(1f)
            binding.feedbackContainer.translationY = startY
            binding.feedbackContainer.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(320L)
                .setInterpolator(OvershootInterpolator(0.9f))
                .start()
        }
    }

    private fun hideFeedback(animated: Boolean) {
        binding.interactionBlocker.isVisible = false
        binding.btnCheckAnswer.isVisible = true
        binding.pbSubmit.isVisible = false
        binding.btnNextQuestion.isEnabled = true
        binding.btnNextQuestion.isClickable = true
        binding.btnTryAgain.isVisible = false
        isSubmitting = false

        if (!binding.feedbackContainer.isVisible) return

        binding.feedbackContainer.animate().cancel()
        if (!animated) {
            binding.feedbackContainer.isVisible = false
            binding.feedbackContainer.translationY = 0f
            binding.feedbackContainer.alpha = 1f
            return
        }

        val endY = binding.feedbackContainer.height.toFloat().coerceAtLeast(1f)
        binding.feedbackContainer.animate()
            .translationY(endY)
            .alpha(0f)
            .setDuration(200L)
            .withEndAction {
                if (_binding != null) {
                    binding.feedbackContainer.isVisible = false
                    binding.feedbackContainer.translationY = 0f
                    binding.feedbackContainer.alpha = 1f
                }
            }
            .start()
    }

    private fun checkOneSelection(question: Question): Boolean {
        val correctAnswerId = question.answers.firstOrNull { it.correct }?.id
        val selectedAnswerId = getSelectedOneSelectionAnswerId(question)

        val isCorrect = correctAnswerId != null && selectedAnswerId != null && correctAnswerId == selectedAnswerId
        Log.d(
            TAG,
            "ONE_SELECTION check: selectedAnswerId=$selectedAnswerId correctAnswerId=$correctAnswerId result=$isCorrect"
        )
        return isCorrect
    }

    private fun getSelectedOneSelectionAnswerId(question: Question): Int? {
        val selectedIndex = getSelectedOneSelectionIndex() ?: return null

        return question.answers.getOrNull(selectedIndex)?.id
    }

    private fun checkArrangedSentence(question: Question): Boolean {
        val userWords = answerSlotAdapter?.getAnswerText().orEmpty()
        val hasEmptySlot = userWords.any { it.isBlank() }
        val userSentence = userWords.joinToString(" ").trim().replace(Regex("\\s+"), " ")
        val correctSentence = question.getCorrectAnswer()?.content?.trim()?.replace(Regex("\\s+"), " ")

        val isCorrect = !hasEmptySlot && question.checkArrangedSentence(userWords)
        Log.d(
            TAG,
            "ARRANGE check: hasEmptySlot=$hasEmptySlot user='$userSentence' correct='${correctSentence ?: ""}' result=$isCorrect"
        )
        return isCorrect
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
        binding.feedbackContainer.visibility = View.GONE
        hideFeedback(animated = false)
        clearMultipleChoiceSelection()

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
        binding.feedbackContainer.visibility = View.GONE
        hideFeedback(animated = false)
        stopAudioIfPlaying()

        binding.tvQuestionTitle.text = currentQuestion.content
        
        // Hide speech bubble and arrange avatar for listen question
        binding.tvSentence.visibility = View.GONE
        binding.ivAvatar.visibility = View.GONE
        binding.ivAvatarListen.visibility = View.VISIBLE
        
        // Tap to play/pause audio from media.url
        binding.ivAvatarListen.setImageResource(R.drawable.volume_up_24px)
        val params = binding.ivAvatarListen.layoutParams
        params.width = 48.dpToPx()
        params.height = 48.dpToPx()
        binding.ivAvatarListen.layoutParams = params

        val audioUrl = currentQuestion.media?.url
        binding.ivAvatarListen.setOnClickListener {
            if (audioUrl.isNullOrBlank()) {
                Log.d(TAG, "listen: media.url is null/blank")
                return@setOnClickListener
            }
            val source = getCachedAudioPathOrUrl(currentQuestion)
            toggleAudio(source)
        }

        setupArrangeUI(currentQuestion)
    }

    private fun displayArrangeQuestion(currentQuestion: Question) {
        // Hide multiple choice
        binding.multipleChoiceContainer.visibility = View.GONE
        binding.arrangeContainer.visibility = View.VISIBLE
        binding.feedbackContainer.visibility = View.GONE
        hideFeedback(animated = false)
        stopAudioIfPlaying()

        binding.tvQuestionTitle.text = resources.getString(R.string.arrange_question_title)
        
        // Show speech bubble and arrange avatar for arrange question
        binding.tvSentence.visibility = View.VISIBLE
        binding.tvSentence.text = currentQuestion.content
        binding.ivAvatar.visibility = View.VISIBLE
        binding.ivAvatarListen.visibility = View.GONE
        binding.ivAvatarListen.setOnClickListener(null)
        
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
            layoutManager = GridLayoutManager(requireContext(), 3)
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
        stopAudioIfPlaying()
        val nextQuestion = viewModel.getNextQuestion() ?: return
        viewModel.moveToNextQuestion()
        updateProgress()
        displayCurrentQuestion(nextQuestion)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseAudioPlayer()
        releaseFeedbackSfx()
        hasInitialized = false
        _binding = null
    }

    private fun toggleAudio(url: String) {
        if (isPreparingAudio) {
            Log.d(TAG, "audio: preparing, ignore toggle")
            return
        }

        val current = mediaPlayer
        if (current != null && playingUrl == url) {
            if (current.isPlaying) {
                current.pause()
                binding.ivAvatarListen.setImageResource(R.drawable.volume_up_24px)
                Log.d(TAG, "audio: paused url=$url")
            } else {
                current.start()
                binding.ivAvatarListen.setImageResource(R.drawable.pause_24px)
                Log.d(TAG, "audio: resumed url=$url")
            }
            return
        }

        playAudio(url)
    }

    private fun playAudio(url: String) {
        releaseAudioPlayer()
        isPreparingAudio = true
        playingUrl = url
        binding.ivAvatarListen.setImageResource(R.drawable.pause_24px)

        val player = MediaPlayer()
        mediaPlayer = player

        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        try {
            player.setDataSource(url)
            player.setOnPreparedListener {
                isPreparingAudio = false
                it.start()
                Log.d(TAG, "audio: started url=$url")
            }
            player.setOnCompletionListener {
                Log.d(TAG, "audio: completed url=$url")
                binding.ivAvatarListen.setImageResource(R.drawable.volume_up_24px)
                releaseAudioPlayer()
            }
            player.setOnErrorListener { _, what, extra ->
                Log.d(TAG, "audio: error what=$what extra=$extra url=$url")
                isPreparingAudio = false
                binding.ivAvatarListen.setImageResource(R.drawable.volume_up_24px)
                releaseAudioPlayer()
                true
            }
            player.prepareAsync()
            Log.d(TAG, "audio: preparing url=$url")
        } catch (e: Exception) {
            Log.d(TAG, "audio: exception url=$url message=${e.message}", e)
            isPreparingAudio = false
            binding.ivAvatarListen.setImageResource(R.drawable.volume_up_24px)
            releaseAudioPlayer()
        }
    }

    private fun stopAudioIfPlaying() {
        if (mediaPlayer != null) {
            binding.ivAvatarListen.setImageResource(R.drawable.volume_up_24px)
        }
        releaseAudioPlayer()
    }

    private fun releaseAudioPlayer() {
        isPreparingAudio = false
        playingUrl = null
        mediaPlayer?.run {
            try {
                stop()
            } catch (_: IllegalStateException) {
            }
            release()
        }
        mediaPlayer = null
    }

    private fun initFeedbackSfx() {
        // Use getIdentifier to avoid compile errors if raw files aren't present yet.
        val correctResId = resources.getIdentifier("correct_answer", "raw", requireContext().packageName)
        val incorrectResId = resources.getIdentifier("incorrect_answer", "raw", requireContext().packageName)

        if (correctResId == 0 && incorrectResId == 0) {
            Log.d(TAG, "feedback sfx: raw/correct_answer & raw/incorrect_answer not found (skip)")
            return
        }

        val pool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        sfxPool = pool
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            if (sampleId == sfxCorrectSoundId) sfxCorrectLoaded = true
            if (sampleId == sfxIncorrectSoundId) sfxIncorrectLoaded = true
        }

        if (correctResId != 0) {
            sfxCorrectSoundId = pool.load(requireContext(), correctResId, 1)
        }
        if (incorrectResId != 0) {
            sfxIncorrectSoundId = pool.load(requireContext(), incorrectResId, 1)
        }
    }

    private fun playFeedbackSfx(isCorrect: Boolean) {
        val pool = sfxPool ?: return
        val (soundId, loaded) = if (isCorrect) {
            sfxCorrectSoundId to sfxCorrectLoaded
        } else {
            sfxIncorrectSoundId to sfxIncorrectLoaded
        }

        if (soundId == null || !loaded) return
        pool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    private fun releaseFeedbackSfx() {
        try {
            sfxPool?.release()
        } catch (_: Exception) {
        }
        sfxPool = null
        sfxCorrectSoundId = null
        sfxIncorrectSoundId = null
        sfxCorrectLoaded = false
        sfxIncorrectLoaded = false
    }

    private fun getCachedAudioPathOrUrl(question: Question): String {
        val media = question.media ?: return ""
        val url = media.url
        if (media.type != MediaType.AUDIO) return url

        val file = File(lessonAudioCacheDir, "media_${media.id}.mp3")
        return if (file.exists()) file.absolutePath else url
    }

    private fun prefetchLessonAudio(questions: List<Question>) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!lessonAudioCacheDir.exists()) {
                    lessonAudioCacheDir.mkdirs()
                }

                val audioQuestions = questions.filter {
                    it.type == QuestionType.LISTEN_AND_ARRANGE_SENTENCE &&
                        it.media?.type == MediaType.AUDIO &&
                        !it.media.url.isNullOrBlank()
                }

                audioQuestions.forEach { q ->
                    val media = q.media ?: return@forEach
                    val outFile = File(lessonAudioCacheDir, "media_${media.id}.mp3")
                    if (outFile.exists()) return@forEach
                    downloadToFile(url = media.url, outFile = outFile)
                }

                Log.d(TAG, "audio prefetch done: cached=${lessonAudioCacheDir.listFiles()?.size ?: 0}")
            } catch (e: Exception) {
                Log.d(TAG, "audio prefetch exception: ${e.message}", e)
            }
        }
    }

    private fun downloadToFile(url: String, outFile: File) {
        val tmpFile = File(outFile.parentFile, outFile.name + ".tmp")
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 15_000
                readTimeout = 30_000
                requestMethod = "GET"
            }

            val code = connection.responseCode
            if (code !in 200..299) {
                Log.d(TAG, "audio download failed: http=$code url=$url")
                return
            }

            connection.inputStream.use { input ->
                FileOutputStream(tmpFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (!tmpFile.renameTo(outFile)) {
                // Fallback copy + delete if rename fails on some devices
                tmpFile.copyTo(outFile, overwrite = true)
                tmpFile.delete()
            }
            Log.d(TAG, "audio cached: ${outFile.absolutePath}")
        } catch (e: Exception) {
            Log.d(TAG, "audio download exception: ${e.message} url=$url", e)
        } finally {
            try {
                tmpFile.delete()
            } catch (_: Exception) {
            }
            connection?.disconnect()
        }
    }

    private suspend fun deleteLessonAudioCache() {
        withContext(Dispatchers.IO) {
            try {
                if (lessonAudioCacheDir.exists()) {
                    lessonAudioCacheDir.deleteRecursively()
                    Log.d(TAG, "audio cache deleted: ${lessonAudioCacheDir.absolutePath}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "audio cache delete exception: ${e.message}", e)
            }
        }
    }
}
