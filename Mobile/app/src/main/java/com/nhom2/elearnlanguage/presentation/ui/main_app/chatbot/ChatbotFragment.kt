package com.nhom2.elearnlanguage.presentation.ui.main_app.chatbot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentChatbotBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatbotFragment : Fragment() {

    private val chatbotViewModel: ChatbotViewModel by viewModels()

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val requestMicPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                chatbotViewModel.toggleListening()
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.you_need_to_grand_permission_to_speak), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnListen.setOnClickListener {
            if (hasMicPermission()) {
                chatbotViewModel.toggleListening()
            } else {
                requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.btnSend.setOnClickListener {
//            val message = binding.etMessage.text.toString()
//            if (message.isNotEmpty()) {
//                binding.etMessage.setText("")
//            }
            findNavController().navigate(R.id.action_chatbotFragment_to_improveFragment)
        }
    }

    private fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            chatbotViewModel.text.collect { text ->
                binding.etMessage.setText(text)
            }
        }

        lifecycleScope.launch {
            chatbotViewModel.isListening.collect { isListening ->
                if (isListening) {
                    binding.btnListen.setBackgroundResource(R.drawable.button_rounded_primary_active)
                } else {
                    binding.btnListen.setBackgroundResource(R.drawable.rounded_button)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}