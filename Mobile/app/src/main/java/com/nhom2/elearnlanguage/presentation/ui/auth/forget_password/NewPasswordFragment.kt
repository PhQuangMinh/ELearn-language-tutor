package com.nhom2.elearnlanguage.presentation.ui.auth.forget_password

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentNewPasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewPasswordFragment : Fragment() {

    private var _binding: FragmentNewPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForgetPasswordViewModel by activityViewModels()

    private data class StrengthResult(
        val isPass: Boolean,
        val message: String,
        val messageColorRes: Int,
        val strokeColorRes: Int,
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewPasswordBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        with(binding) {
            fun applyStrength(s: StrengthResult) {
                if (s.message.isBlank()) {
                    tvPasswordStrength.text = ""
                    tvPasswordStrength.visibility = View.GONE
                } else {
                    tvPasswordStrength.text = s.message
                    tvPasswordStrength.visibility = View.VISIBLE
                    tvPasswordStrength.setTextColor(ContextCompat.getColor(requireContext(), s.messageColorRes))
                }

                passwordInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), s.strokeColorRes)

                btnNextRegister.isEnabled = s.isPass
                btnNextRegister.alpha = if (btnNextRegister.isEnabled) 1f else 0.6f
            }

            edtPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    applyStrength(evaluatePassword(s?.toString().orEmpty()))
                }
            })

            etPasswordConfirm.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    applyStrength(evaluatePassword(edtPassword.text?.toString().orEmpty()))
                }
            })

            btnNextRegister.setOnClickListener {
                val email = viewModel.email.value
                val resetToken = viewModel.resetToken.value
                val newPassword = edtPassword.text?.toString().orEmpty()
                val confirmPassword = etPasswordConfirm.text?.toString().orEmpty()
                viewModel.resetPassword(email, resetToken, newPassword, confirmPassword)
            }

            applyStrength(evaluatePassword(edtPassword.text?.toString().orEmpty()))
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is ResetPasswordUIState.Idle -> {
                                setLoading(false)
                            }
                            is ResetPasswordUIState.Loading -> {
                                setLoading(true)
                            }
                            is ResetPasswordUIState.Success -> {
                                setLoading(false)
                                Toast.makeText(requireContext(), "Reset password successful.", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack(R.id.loginFragment, false)
                                viewModel.resetState()
                            }
                            is ResetPasswordUIState.Error -> {
                                setLoading(false)
                                viewModel.setError(state.message ?: "Reset password failed.")
                            }
                        }
                    }
                }

                launch {
                    viewModel.error.collect { errorMessage ->
                        with(binding) {
                            if (errorMessage.isNotBlank()) {
                                tvError.visibility = View.VISIBLE
                                tvError.text = errorMessage
                            } else {
                                tvError.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnNextRegister.isEnabled = false
            binding.btnNextRegister.alpha = 0.6f
            binding.pbResetPassword.visibility = View.VISIBLE
            return
        }

        binding.pbResetPassword.visibility = View.GONE
        val strength = evaluatePassword(binding.edtPassword.text?.toString().orEmpty())
        binding.btnNextRegister.isEnabled = strength.isPass
        binding.btnNextRegister.alpha = if (strength.isPass) 1f else 0.6f
    }

    private fun evaluatePassword(password: String): StrengthResult {
        val p = password.trim()
        if (p.isEmpty()) {
            return StrengthResult(
                isPass = false,
                message = "",
                messageColorRes = R.color.neutral_60,
                strokeColorRes = R.color.input_stroke_blue,
            )
        }

        if (p.length < 8) {
            return StrengthResult(
                isPass = false,
                message = "Your password's length must be >= 8 characters!",
                messageColorRes = R.color.status_red,
                strokeColorRes = R.color.status_red,
            )
        }

        val hasLetter = p.any { it.isLetter() }
        val hasDigit = p.any { it.isDigit() }
        val hasSpecial = p.any { !it.isLetterOrDigit() }

        if (!hasLetter || !hasDigit) {
            return StrengthResult(
                isPass = false,
                message = "How strong your password: Low (Only numbers or characters).",
                messageColorRes = R.color.status_red,
                strokeColorRes = R.color.status_red,
            )
        }

        if (!hasSpecial) {
            return StrengthResult(
                isPass = true,
                message = "How strong your password: Medium.",
                messageColorRes = R.color.status_orange,
                strokeColorRes = R.color.status_orange,
            )
        }

        if (p.length >= 12) {
            return StrengthResult(
                isPass = true,
                message = "How strong your password: Super Strong.",
                messageColorRes = R.color.status_green,
                strokeColorRes = R.color.status_green,
            )
        }

        return StrengthResult(
            isPass = true,
            message = "How strong your password: Strong.",
            messageColorRes = R.color.status_green,
            strokeColorRes = R.color.status_green,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}