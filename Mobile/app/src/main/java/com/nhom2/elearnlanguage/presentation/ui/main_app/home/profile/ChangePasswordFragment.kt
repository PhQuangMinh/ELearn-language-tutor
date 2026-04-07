package com.nhom2.elearnlanguage.presentation.ui.main_app.home.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentChangePasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChangePasswordViewModel by viewModels()

    private data class StrengthResult(
        val isPass: Boolean,
        val message: String,
        val messageColorRes: Int,
        val strokeColorRes: Int,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
        setupListeners()
        observeViewModel()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateBackToProfile()
                }
            }
        )
    }

    private fun navigateBackToProfile() {
        val popped = parentFragmentManager.popBackStackImmediate()
        if (!popped) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
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

                newPasswordInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), s.strokeColorRes)

                val passwordsMatch = etConfirmPassword.text?.toString().orEmpty() ==
                    etNewPassword.text?.toString().orEmpty()
                btnSave.isEnabled = s.isPass && passwordsMatch
                btnSave.alpha = if (btnSave.isEnabled) 1f else 0.6f
            }

            topBackButton.root.setOnClickListener {
                navigateBackToProfile()
            }

            etNewPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    applyStrength(evaluatePassword(s?.toString().orEmpty()))
                }
            })

            etConfirmPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    applyStrength(evaluatePassword(etNewPassword.text?.toString().orEmpty()))
                }
            })

            btnSave.setOnClickListener {
                tvError.visibility = View.GONE
                val currentPassword = etCurrentPassword.text?.toString().orEmpty().trim()
                val newPassword = etNewPassword.text?.toString().orEmpty().trim()
                val confirmPassword = etConfirmPassword.text?.toString().orEmpty().trim()

                if (currentPassword.isBlank()) {
                    tvError.visibility = View.VISIBLE
                    tvError.text = getString(R.string.current_password_required)
                    return@setOnClickListener
                }

                if (newPassword != confirmPassword) {
                    tvError.visibility = View.VISIBLE
                    tvError.text = getString(R.string.confirm_password_mismatch)
                    return@setOnClickListener
                }

                viewModel.changePassword(currentPassword, newPassword, confirmPassword)
            }

            applyStrength(evaluatePassword(etNewPassword.text?.toString().orEmpty()))
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ChangePasswordUiState.Idle -> setLoading(false)
                        is ChangePasswordUiState.Loading -> setLoading(true)
                        is ChangePasswordUiState.Success -> {
                            setLoading(false)
                            Toast.makeText(requireContext(), getString(R.string.change_password_success), Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                            navigateBackToProfile()
                        }
                        is ChangePasswordUiState.Error -> {
                            setLoading(false)
                            binding.tvError.visibility = View.VISIBLE
                            binding.tvError.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.pbChangePassword.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.btnSave.isEnabled = false
            binding.btnSave.alpha = 0.6f
            return
        }

        val strength = evaluatePassword(binding.etNewPassword.text?.toString().orEmpty())
        val passwordsMatch = binding.etConfirmPassword.text?.toString().orEmpty() ==
            binding.etNewPassword.text?.toString().orEmpty()
        binding.btnSave.isEnabled = strength.isPass && passwordsMatch
        binding.btnSave.alpha = if (binding.btnSave.isEnabled) 1f else 0.6f
    }

    private fun evaluatePassword(password: String): StrengthResult {
        val p = password.trim()
        if (p.isEmpty()) {
            return StrengthResult(
                isPass = false,
                message = "",
                messageColorRes = R.color.status_grey,
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
