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
import com.nhom2.elearnlanguage.presentation.utils.applyBottomSystemBarInsetPadding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NameRegisterFragment : Fragment(R.layout.fragment_name_register){

    private val viewModel: RegisterViewModel by activityViewModels()

    private data class NameValidation(
        val isValid: Boolean,
        val message: String?,
    )

    private fun validateName(value: String): NameValidation {
        val name = value.trim()
        if (name.isEmpty()) {
            return NameValidation(false, "Your name cannot be empty!")
        }
        if (name.length < 4) {
            return NameValidation(false, "Your name's length must be >= 4 characters.")
        }
        return NameValidation(true, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.applyBottomSystemBarInsetPadding()

        val edtNameRegister = view.findViewById<EditText>(R.id.edtNameRegister)
        val btnNextRegister = view.findViewById<Button>(R.id.btnNextRegister)
        val backButton = view.findViewById<View>(R.id.backButton)
        val tvNameError = view.findViewById<TextView>(R.id.tvNameError)
        val nameInputLayout = view.findViewById<TextInputLayout>(R.id.nameInputLayout)

        fun applyValidation(result: NameValidation) {
            if (result.isValid) {
                tvNameError.visibility = View.GONE
                nameInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), R.color.input_stroke_blue)
            } else {
                tvNameError.text = result.message ?: ""
                tvNameError.visibility = View.VISIBLE
                nameInputLayout.boxStrokeColor =
                    ContextCompat.getColor(requireContext(), R.color.status_red)
            }
        }

        // Prefill if user comes back
        val existingName = viewModel.name.value
        if (existingName.isNotBlank() && edtNameRegister.text.isNullOrBlank()) {
            edtNameRegister.setText(existingName)
            edtNameRegister.setSelection(existingName.length)
        }

        backButton.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        btnNextRegister.setOnClickListener {
            val name = edtNameRegister.text?.toString().orEmpty()
            val result = validateName(name)
            applyValidation(result)
            if (result.isValid) {
                viewModel.setName(name.trim())
                findNavController().navigate(R.id.action_name_to_email)
            }
        }
    }
}