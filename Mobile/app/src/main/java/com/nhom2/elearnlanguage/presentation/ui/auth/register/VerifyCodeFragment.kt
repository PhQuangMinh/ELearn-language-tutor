package com.nhom2.elearnlanguage.presentation.ui.auth.register

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.presentation.utils.applyBottomSystemBarInsetPadding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerifyCodeFragment : Fragment(R.layout.fragment_verify_code) {

    private var timer: CountDownTimer? = null
    private var isErrorShown: Boolean = false
    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.applyBottomSystemBarInsetPadding()

        val backButton = view.findViewById<View>(R.id.backButton)
        val tvResend = view.findViewById<TextView>(R.id.tvResend)
        val tvOtpError = view.findViewById<TextView>(R.id.tvOtpError)
        val btnNext = view.findViewById<MaterialButton>(R.id.btnNext)

        val boxes = listOf(
            view.findViewById<EditText>(R.id.otp1),
            view.findViewById<EditText>(R.id.otp2),
            view.findViewById<EditText>(R.id.otp3),
            view.findViewById<EditText>(R.id.otp4),
            view.findViewById<EditText>(R.id.otp5),
            view.findViewById<EditText>(R.id.otp6),
        )

        fun getCode(): String = boxes.joinToString(separator = "") { it.text?.toString().orEmpty() }

        fun setErrorState(show: Boolean, message: String? = null) {
            isErrorShown = show
            if (show && !message.isNullOrBlank()) {
                tvOtpError.text = message
            }
            tvOtpError.visibility = if (show) View.VISIBLE else View.GONE
            val bg = if (show) R.drawable.bg_otp_box_error else R.drawable.bg_otp_box
            boxes.forEach { it.setBackgroundResource(bg) }
        }

        fun updateNextEnabled() {
            val ok = boxes.all { (it.text?.length ?: 0) == 1 }
            btnNext.isEnabled = ok
            btnNext.alpha = if (ok) 1f else 0.6f
        }

        fun setOtpFrom(index: Int, value: String) {
            val digits = value.filter { it.isDigit() }
            if (digits.isEmpty()) return

            var i = index
            for (ch in digits) {
                if (i >= boxes.size) break
                boxes[i].setText(ch.toString())
                i++
            }
            val focusIndex = (i).coerceAtMost(boxes.lastIndex)
            boxes[focusIndex].requestFocus()
            boxes[focusIndex].setSelection(boxes[focusIndex].text?.length ?: 0)
            updateNextEnabled()
        }

        boxes.forEachIndexed { index, et ->
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    if (isErrorShown) setErrorState(false)

                    val text = s?.toString().orEmpty()

                    if (text.length > 1) {
                        et.removeTextChangedListener(this)
                        et.setText(text.last().toString())
                        et.setSelection(et.text?.length ?: 0)
                        et.addTextChangedListener(this)
                        setOtpFrom(index, text)
                        return
                    }

                    if (text.length == 1 && index < boxes.lastIndex) {
                        boxes[index + 1].requestFocus()
                        boxes[index + 1].setSelection(boxes[index + 1].text?.length ?: 0)
                    }

                    updateNextEnabled()
                }
            })

            et.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_DEL &&
                    et.text.isNullOrEmpty() &&
                    index > 0
                ) {
                    boxes[index - 1].requestFocus()
                    boxes[index - 1].setSelection(boxes[index - 1].text?.length ?: 0)
                    return@setOnKeyListener true
                }
                false
            }
        }

        backButton.setOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        btnNext.setOnClickListener {
            val code = getCode()
            btnNext.isEnabled = false
            btnNext.alpha = 0.6f

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.verifyEmailCode(code)
                    setErrorState(false)
                    findNavController().navigate(R.id.action_verify_to_password)
                } catch (e: Exception) {
                    setErrorState(true, e.message ?: "Your verification code is incorrect.")
                } finally {
                    updateNextEnabled()
                }
            }
        }

        fun startCountdown(seconds: Int) {
            timer?.cancel()
            tvResend.isClickable = false
            tvResend.setTextColor(0xFF9AA7B8.toInt())

            timer = object : CountDownTimer(seconds * 1000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    val s = (millisUntilFinished / 1000L).toInt()
                    tvResend.text = "Resend code in: $s"
                }

                override fun onFinish() {
                    tvResend.text = "Resend code"
                    tvResend.isClickable = true
                    tvResend.setTextColor(resources.getColor(R.color.primary_blue, null))
                }
            }.start()
        }

        tvResend.setOnClickListener {
            if (!tvResend.isClickable) return@setOnClickListener
            tvResend.isClickable = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.initiateRegister()
                    startCountdown(60)
                } catch (e: Exception) {
                    tvResend.text = "Resend code"
                    tvResend.isClickable = true
                    setErrorState(true, e.message ?: "Failed to resend OTP. Please try again.")
                }
            }
        }

        // init
        setErrorState(false)
        updateNextEnabled()
        startCountdown(60)
        boxes.first().requestFocus()
    }

    override fun onDestroyView() {
        timer?.cancel()
        timer = null
        super.onDestroyView()
    }
}

