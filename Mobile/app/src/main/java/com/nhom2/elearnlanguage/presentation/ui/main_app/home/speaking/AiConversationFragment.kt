package com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.Locale
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentAiConversationBinding
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiConversationFragment : Fragment() {

    private var _binding: FragmentAiConversationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AiConversationViewModel by viewModels()
    private lateinit var adapter: AiConversationAdapter

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Text-to-Speech once for this screen.
        textToSpeech = TextToSpeech(requireContext()) { status ->
            isTtsReady = status == TextToSpeech.SUCCESS
        }

        adapter = AiConversationAdapter(
            onHintClick = { messageId -> viewModel.toggleHint(messageId) },
            onSpeakerClick = { aiText ->
                speakAiText(aiText)
            }
        )

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                // Hiển thị message theo chiều từ trên xuống.
                // Nếu stackFromEnd=true thì với ít message (hoặc 1 message) sẽ bị “dồn xuống đáy”.
                stackFromEnd = false
            }
            adapter = this@AiConversationFragment.adapter
            itemAnimator = null
        }

        binding.btnBack.root.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.conversationInput.btnSend.setOnClickListener {
            val text = binding.conversationInput.etMessage.text?.toString()?.trim().orEmpty()
            if (text.isNotBlank()) {
                viewModel.addUserMessage(text)
                binding.conversationInput.etMessage.setText("")
            }
        }

        val lessonId = arguments?.getInt("lessonId", 1) ?: 1
        viewModel.start(lessonId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.context.collect { ctx ->
                        binding.tvContextTitle.text = ctx.title
                        binding.tvScenario.text = ctx.scenario

                        binding.llMission.removeAllViews()
                        ctx.mission.forEach { m ->
                            val item = layoutInflater.inflate(R.layout.item_mission, binding.llMission, false)
                            item.findViewById<TextView>(R.id.tvMissionText).text = "– $m"
                            binding.llMission.addView(item)
                        }
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        adapter.submit(state.messages, state.expandedHintMessageId) {
                            if (state.messages.isNotEmpty()) {
                                binding.rvMessages.scrollToPosition(state.messages.size - 1)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsReady = false
        _binding = null
    }

    private fun speakAiText(text: String) {
        val tts = textToSpeech ?: return
        if (!isTtsReady) return

        // Guess language based on Vietnamese characters; otherwise use US English.
        val locale = if (looksVietnamese(text)) Locale("vi", "VN") else Locale.US
        val langResult = tts.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA ||
            langResult == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            tts.setLanguage(Locale.US)
        }

        val params = Bundle()
        val utteranceId = "ai_${System.nanoTime()}"
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun looksVietnamese(text: String): Boolean {
        // Rough heuristic: check for common Vietnamese diacritics/characters.
        val regex = Regex("[àáảãạăằắẳẵặâầấẩẫậđèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵ]")
        return regex.containsMatchIn(text)
    }
}

