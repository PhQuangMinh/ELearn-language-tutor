package com.nhom2.elearnlanguage.presentation.ui.auth.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nhom2.elearnlanguage.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PasswordRegisterFragment : Fragment(R.layout.fragment_password_register) {

    private val viewModel: RegisterViewModel by activityViewModels()

    private data class StrengthResult(
        val isPass: Boolean,
        val message: String,
        val messageColorRes: Int,
        val strokeColorRes: Int,
    )

    private fun evaluatePassword(password: String): StrengthResult {
        val p = password.trim()
        if (p.isEmpty()) {
            return StrengthResult(
                isPass = false,
                message = "",
                messageColorRes = R.color.status_grey,
                strokeColorRes = R.color.stroke_neutral,
            )
        }

        if (p.length in 1..<8) {
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

        if (p.isNotEmpty() && (!hasLetter || !hasDigit)) {
            return StrengthResult(
                isPass = false,
                message = "How strong your password: Low (Only numbers or characters).",
                messageColorRes = R.color.status_red,
                strokeColorRes = R.color.status_red,
            )
        }

        if (p.isNotEmpty() && !hasSpecial) {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<View>(R.id.backButton)
        val passwordInputLayout = view.findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val edtPassword = view.findViewById<TextInputEditText>(R.id.edtPasswordRegister)
        val tvStrength = view.findViewById<TextView>(R.id.tvPasswordStrength)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btnRegister)

        fun applyStrength(s: StrengthResult) {
            if (s.message.isBlank()) {
                tvStrength.text = ""
                tvStrength.visibility = View.GONE
            } else {
                tvStrength.text = s.message
                tvStrength.visibility = View.VISIBLE
                tvStrength.setTextColor(ContextCompat.getColor(requireContext(), s.messageColorRes))
            }

            passwordInputLayout.boxStrokeColor =
                ContextCompat.getColor(requireContext(), s.strokeColorRes)

            btnRegister.isEnabled = s.isPass
            btnRegister.alpha = if (s.isPass) 1f else 0.6f
        }

        // Prefill if user comes back
        val existing = viewModel.password.value
        if (existing.isNotBlank() && edtPassword.text.isNullOrBlank()) {
            edtPassword.setText(existing)
            edtPassword.setSelection(existing.length)
        }

        applyStrength(evaluatePassword(edtPassword.text?.toString().orEmpty()))

        edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                applyStrength(evaluatePassword(s?.toString().orEmpty()))
            }
        })

        backButton.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        btnRegister.setOnClickListener {
            val password = edtPassword.text?.toString().orEmpty()
            viewModel.setPassword(password)

            btnRegister.isEnabled = false
            btnRegister.alpha = 0.6f

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.completeRegister(password)
                    Toast.makeText(requireContext(), "Register successful!", Toast.LENGTH_SHORT).show()

                    val navOptions = androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.nav_auth, true)
                        .build()

                    findNavController().navigate(R.id.loginFragment, null, navOptions)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        e.message ?: "Register failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    btnRegister.isEnabled = true
                    btnRegister.alpha = 1f
                }
            }
        }
    }
}

