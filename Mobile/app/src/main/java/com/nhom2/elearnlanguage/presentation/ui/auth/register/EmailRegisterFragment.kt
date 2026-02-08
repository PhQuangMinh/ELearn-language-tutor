package com.nhom2.elearnlanguage.presentation.ui.auth.register

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.nhom2.elearnlanguage.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailRegisterFragment : Fragment(R.layout.fragment_email_register) {

    private val viewModel: RegisterViewModel by activityViewModels()

    private data class EmailValidation(
        val isValid: Boolean,
        val message: String?,
    )

    // Mock "email already exists in system"
    private val usedEmails = setOf(
        "test@gmail.com",
        "admin@gmail.com",
        "longhoanghai.work@gmail.com",
    )

    private fun validateEmail(value: String): EmailValidation {
        val email = value.trim()
        if (email.isEmpty()) {
            return EmailValidation(false, "Your email cannot be empty!")
        }

        val gmailRegex = Regex("^[A-Za-z0-9._%+-]+@gmail\\.com$", RegexOption.IGNORE_CASE)
        if (!gmailRegex.matches(email)) {
            return EmailValidation(false, "Your email's format is incorrect!")
        }

        if (usedEmails.any { it.equals(email, ignoreCase = true) }) {
            return EmailValidation(false, "This email has been used. Please choose another email!")
        }

        return EmailValidation(true, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvEmailTitle = view.findViewById<TextView>(R.id.tvEmailTitle)
        val edtEmailRegister = view.findViewById<EditText>(R.id.edtEmailRegister)
        val btnNextRegister = view.findViewById<Button>(R.id.btnNextRegister)
        val backButton = view.findViewById<View>(R.id.backButton)
        val tvEmailError = view.findViewById<TextView>(R.id.tvEmailError)
        val emailInputLayout = view.findViewById<TextInputLayout>(R.id.emailInputLayout)

        fun applyValidation(result: EmailValidation) {
            if (result.isValid) {
                tvEmailError.visibility = View.GONE
                emailInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), R.color.input_stroke_blue)
            } else {
                tvEmailError.text = result.message ?: ""
                tvEmailError.visibility = View.VISIBLE
                emailInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), R.color.status_red)
            }
        }

        val name = viewModel.name.value.trim()
        tvEmailTitle.text = if (name.isNotEmpty()) {
            "What is your email, $name"
        } else {
            "What is your email?"
        }

        // Prefill if user comes back
        val existingEmail = viewModel.email.value
        if (existingEmail.isNotBlank() && edtEmailRegister.text.isNullOrBlank()) {
            edtEmailRegister.setText(existingEmail)
            edtEmailRegister.setSelection(existingEmail.length)
        }

        backButton.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        btnNextRegister.setOnClickListener {
            val email = edtEmailRegister.text?.toString().orEmpty()
            val result = validateEmail(email)
            applyValidation(result)
            if (result.isValid) {
                viewModel.setEmail(email.trim())
                findNavController().navigate(R.id.action_email_to_verify)
            }
        }
    }
}