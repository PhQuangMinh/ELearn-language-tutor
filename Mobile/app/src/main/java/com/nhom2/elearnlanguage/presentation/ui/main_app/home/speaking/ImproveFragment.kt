package com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentImproveBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImproveFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentImproveBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ImproveViewModel by viewModels()
    private var originalTextArg: String = ""
    private var improvedTextArg: String = ""
    private var explanationArg: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalTextArg = arguments?.getString(ARG_ORIGINAL_TEXT).orEmpty()
        improvedTextArg = arguments?.getString(ARG_IMPROVED_TEXT).orEmpty()
        explanationArg = arguments?.getString(ARG_EXPLANATION).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImproveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (improvedTextArg.isNotBlank() && explanationArg.isNotBlank()) {
            renderPrecomputed()
        } else {
            observeUiState()
            viewModel.loadImprovement(originalTextArg)
        }
    }

    override fun onStart() {
        super.onStart()
        val bottomSheetDialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.pbImprove.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.tvOriginalText.text = state.originalText
                binding.tvImprovedText.text = state.improvedText.ifBlank {
                    getString(R.string.improve_result_placeholder)
                }
                binding.tvExplanation.text = state.explanation.ifBlank {
                    getString(R.string.improve_explanation_placeholder)
                }

                if (state.errorMessageRes != null) {
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = getString(state.errorMessageRes)
                } else {
                    binding.tvError.visibility = View.GONE
                }
            }
        }
    }

    private fun renderPrecomputed() {
        binding.pbImprove.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.tvOriginalText.text = originalTextArg
        binding.tvImprovedText.text = improvedTextArg
        binding.tvExplanation.text = explanationArg
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ORIGINAL_TEXT = "arg_original_text"
        private const val ARG_IMPROVED_TEXT = "arg_improved_text"
        private const val ARG_EXPLANATION = "arg_explanation"

        fun newInstance(
            originalText: String,
            improvedText: String = "",
            explanation: String = ""
        ): ImproveFragment {
            return ImproveFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORIGINAL_TEXT, originalText)
                    putString(ARG_IMPROVED_TEXT, improvedText)
                    putString(ARG_EXPLANATION, explanation)
                }
            }
        }
    }
}