package com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
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

    private val viewModel: AiConversationViewModel by hiltNavGraphViewModels(R.id.nav_main)
    private val args: AiConversationFragmentArgs by navArgs()
    private lateinit var adapter: AiConversationAdapter

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady: Boolean = false

    private val requestMicPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.toggleListening()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_need_to_grand_permission_to_speak),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

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
            },
            onImproveClick = { messageId ->
                viewModel.onImproveClick(messageId)
            }
        )

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = false
            }
            adapter = this@AiConversationFragment.adapter
            itemAnimator = null
        }

        binding.btnBack.root.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.conversationInput.btnListen.setOnClickListener {
            if (hasMicPermission()) {
                viewModel.toggleListening()
            } else {
                requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.conversationInput.btnSend.setOnClickListener {
            val text = binding.conversationInput.etMessage.text?.toString()?.trim().orEmpty()
            if (text.isNotBlank()) {
                binding.conversationInput.etMessage.setText("")
                viewModel.sendMessage(text)
            }
        }

        viewModel.start(args.lessonId)

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
                    viewModel.text.collect { text ->
                        binding.conversationInput.etMessage.setText(text)
                    }
                }

                launch {
                    viewModel.isListening.collect { isListening ->
                        if (isListening) {
                            binding.conversationInput.btnListen
                                .setBackgroundResource(R.drawable.button_rounded_primary_active)
                        } else {
                            binding.conversationInput.btnListen
                                .setBackgroundResource(R.drawable.rounded_button)
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

                launch {
                    viewModel.isSending.collect { isSending ->
                        binding.conversationInput.btnSend.isEnabled = !isSending
                        binding.conversationInput.etMessage.isEnabled = !isSending
                        binding.conversationInput.btnSend.alpha = if (isSending) 0.6f else 1f
                    }
                }

                launch {
                    viewModel.openImproveSheet.collect { improveResult ->
                        val tag = ImproveFragment::class.java.simpleName
                        if (childFragmentManager.findFragmentByTag(tag) == null) {
                            ImproveFragment.newInstance(
                                originalText = improveResult.original,
                                improvedText = improveResult.improved,
                                explanation = improveResult.explanation
                            ).show(childFragmentManager, tag)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.endSession()
        viewModel.stopListening()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsReady = false
        _binding = null
    }

    private fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
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

