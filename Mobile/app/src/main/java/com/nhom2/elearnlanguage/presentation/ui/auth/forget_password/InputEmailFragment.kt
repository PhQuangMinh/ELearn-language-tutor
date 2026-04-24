package com.nhom2.elearnlanguage.presentation.ui.auth.forget_password

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentInputEmailBinding
import com.nhom2.elearnlanguage.presentation.utils.applyBottomSystemBarInsetPadding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InputEmailFragment : Fragment() {

    private var _binding: FragmentInputEmailBinding? = null
    private val binding get() = _binding!!

    private val forgetPasswordViewModel: ForgetPasswordViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInputEmailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.applyBottomSystemBarInsetPadding()

        setupListener()
        setUpObservers()

        val backButton = view.findViewById<View>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListener () {
        with(binding) {
            btnNext.setOnClickListener {
                checkEmail()
            }
        }
    }

    private fun setUpObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    forgetPasswordViewModel.uiState.collect { state ->
                        when (state) {
                            is ResetPasswordUIState.Idle -> setLoading(false)
                            is ResetPasswordUIState.Loading -> setLoading(true)
                            is ResetPasswordUIState.Success -> {
                                setLoading(false)
                                forgetPasswordViewModel.resetState()
                                findNavController().navigate(R.id.action_inputEmailFragment_to_inputVerificationCodeFragment)
                            }
                            is ResetPasswordUIState.Error -> {
                                setLoading(false)
                            }
                        }
                    }
                }

                launch {
                    forgetPasswordViewModel.error.collect { errorMessage ->
                        if (errorMessage.isNotBlank()) {
                            binding.tvError.visibility = View.VISIBLE
                            binding.tvError.text = errorMessage
                        } else {
                            binding.tvError.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun checkEmail () {
        val email = binding.etEmail.text.toString()
        forgetPasswordViewModel.setEmail(email)
        forgetPasswordViewModel.forgotPassword(email)
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnNext.isEnabled = !isLoading
        binding.btnNext.alpha = if (binding.btnNext.isEnabled) 1f else 0.6f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}