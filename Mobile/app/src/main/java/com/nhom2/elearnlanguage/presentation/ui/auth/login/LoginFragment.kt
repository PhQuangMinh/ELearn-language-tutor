package com.nhom2.elearnlanguage.presentation.ui.auth.login

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.net.toUri

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var isPasswordVisible = false
    private var emailTextColors: ColorStateList? = null
    private var passwordTextColors: ColorStateList? = null
    private var emailHintTextColor: Int? = null
    private var passwordHintTextColor: Int? = null

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            emailTextColors = etEmail.textColors
            passwordTextColors = etPassword.textColors
            emailHintTextColor = etEmail.currentHintTextColor
            passwordHintTextColor = etPassword.currentHintTextColor

        }

        setUpListeners()
        setUpObservers()
    }

    private fun setUpListeners() {
        with(binding) {
            btnLogin.setOnClickListener {
                login()
            }

            tvGoogle.setOnClickListener {
                openOAuthLogin("google")
            }

            tvCreateAccount.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_nav_auth)
            }

            ivVisibility.setOnClickListener {
                isPasswordVisible = !isPasswordVisible

                if(isPasswordVisible) {
                    etPassword.transformationMethod = null
                    ivVisibility.setImageResource(R.drawable.visibility_24px)
                } else {
                    etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    ivVisibility.setImageResource(R.drawable.visibility_off_24px)
                }
            }

            etPassword.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearLoginError()
                }
            }

            etEmail.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearLoginError()
                }
            }

            tvForgotPassword.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_inputEmailFragment)
            }
        }
    }

    private fun login() {
        with(binding) {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            clearLoginError()
            viewModel.login(email, password)
        }
    }

    private fun openOAuthLogin(provider: String) {
        val baseUrl = BuildConfig.API_BASE_URL
        val normalizedProvider = provider.lowercase(Locale.US)
        val url = "$baseUrl/api/auth/oauth2/authorize/$normalizedProvider"
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(requireContext(), url.toUri())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoginError() {
        val errorColor = ContextCompat.getColor(requireContext(), R.color.error_80)
        with(binding) {
            tvError.visibility = View.VISIBLE
            etEmail.setTextColor(errorColor)
            etEmail.setHintTextColor(errorColor)
            etPassword.setTextColor(errorColor)
            etPassword.setHintTextColor(errorColor)
        }
    }

    private fun clearLoginError() {
        binding.tvError.visibility = View.GONE
        with(binding) {
            emailTextColors?.let { etEmail.setTextColor(it) }
            passwordTextColors?.let { etPassword.setTextColor(it) }
            emailHintTextColor?.let { etEmail.setHintTextColor(it) }
            passwordHintTextColor?.let { etPassword.setHintTextColor(it) }
        }
    }

    private fun setUpObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Idle -> {
                            setLoading(false)
                        }
                        is LoginUiState.Loading -> {
                            clearLoginError()
                            setLoading(true)
                        }
                        is LoginUiState.Success -> {
                            setLoading(false)
                            Toast.makeText(requireContext(), "Login success", Toast.LENGTH_SHORT).show()

                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                            viewModel.resetState()
                        }
                        is LoginUiState.Error -> {
                            setLoading(false)
                            showLoginError()
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.pbLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}