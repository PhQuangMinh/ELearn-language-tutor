package com.nhom2.elearnlanguage.presentation.ui.auth.forget_password

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentInputVerificationCodeBinding
import com.nhom2.elearnlanguage.presentation.utils.applyBottomSystemBarInsetPadding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InputVerificationCodeFragment : Fragment() {

    private var _binding: FragmentInputVerificationCodeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForgetPasswordViewModel by activityViewModels()
    private var timer: CountDownTimer? = null
    private var isResendRequest: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInputVerificationCodeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.applyBottomSystemBarInsetPadding()

        setupListener()
        setupOtpInputs()
        setupObservers()
        startCountdown(60)
    }

    private fun setupListener () {
        with(binding) {
            btnNext.isEnabled = true
            btnNext.alpha = 1f

            btnNext.setOnClickListener {
                val email = viewModel.email.value
                val code = getOtpCode()

                clearOtpError()
                viewModel.verifyForgotPasswordCode(email, code)
            }

            tvResend.setOnClickListener {
                if (!tvResend.isClickable) return@setOnClickListener
                val email = viewModel.email.value
                if (email.isBlank()) {
                    showOtpError("Email khong hop le")
                    return@setOnClickListener
                }
                isResendRequest = true
                clearOtpError()
                viewModel.forgotPassword(email)
                startCountdown(60)
            }
        }
    }

    private fun setupOtpInputs() {
        val inputs = listOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
            binding.otp5,
            binding.otp6
        )

        inputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < inputs.lastIndex) {
                        inputs[index + 1].requestFocus()
                    } else if (s?.length == 1 && index == inputs.lastIndex) {
                        hideKeyboard(editText)
                    }

                    updateNextButtonState()
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.text.isNullOrEmpty() && index > 0) {
                        inputs[index - 1].requestFocus()
                        inputs[index - 1].setSelection(inputs[index - 1].text?.length ?: 0)
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ResetPasswordUIState.Idle -> setLoading(false)
                        is ResetPasswordUIState.Loading -> setLoading(true)
                        is ResetPasswordUIState.Success -> {
                            setLoading(false)
                            if (isResendRequest) {
                                isResendRequest = false
                                viewModel.resetState()
                                showOtpError("Da gui lai ma xac thuc")
                            } else {
                                viewModel.resetState()
                                findNavController().navigate(R.id.action_inputVerificationCodeFragment_to_newPasswordFragment)
                            }
                        }
                        is ResetPasswordUIState.Error -> {
                            setLoading(false)
                            if (isResendRequest) {
                                isResendRequest = false
                            }
                            if (state.message.isNullOrBlank()) {
                                clearOtpError()
                            } else {
                                showOtpError(state.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnNext.isEnabled = !isLoading
        binding.btnNext.alpha = if (binding.btnNext.isEnabled) 1f else 0.6f
    }

    private fun updateNextButtonState() {
        if (binding.tvOtpError.visibility == View.VISIBLE) {
            clearOtpError()
        }
    }

    private fun getOtpCode(): String {
        return buildString(OTP_LENGTH) {
            append(binding.otp1.text?.toString().orEmpty())
            append(binding.otp2.text?.toString().orEmpty())
            append(binding.otp3.text?.toString().orEmpty())
            append(binding.otp4.text?.toString().orEmpty())
            append(binding.otp5.text?.toString().orEmpty())
            append(binding.otp6.text?.toString().orEmpty())
        }
    }

    private fun showOtpError(message: String) {
        binding.tvOtpError.text = message
        binding.tvOtpError.visibility = View.VISIBLE
    }

    private fun clearOtpError() {
        binding.tvOtpError.text = ""
        binding.tvOtpError.visibility = View.GONE
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun startCountdown(seconds: Int) {
        timer?.cancel()
        binding.tvResend.isClickable = false
        binding.tvResend.setTextColor(0xFF9AA7B8.toInt())

        timer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val s = (millisUntilFinished / 1000L).toInt()
                binding.tvResend.text = "Resend code in: $s"
            }

            override fun onFinish() {
                binding.tvResend.text = "Resend code"
                binding.tvResend.isClickable = true
                binding.tvResend.setTextColor(resources.getColor(R.color.primary_blue, null))
            }
        }.start()
    }

    companion object {
        private const val OTP_LENGTH = 6
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        timer = null
        _binding = null
    }
}