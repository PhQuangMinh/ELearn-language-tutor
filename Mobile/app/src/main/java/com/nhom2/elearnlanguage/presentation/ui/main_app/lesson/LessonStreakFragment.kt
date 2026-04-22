package com.nhom2.elearnlanguage.presentation.ui.main_app.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentLessonStreakBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LessonStreakFragment : Fragment() {
    companion object {
        private const val KEY_LESSON_COMPLETED_EVENT_ID = "lessonCompletedEventId"
    }

    private var _binding: FragmentLessonStreakBinding? = null
    private val binding get() = _binding!!
    private val args: LessonStreakFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonStreakBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val targetStreak = args.currentStreak.coerceAtLeast(0)
        val startStreak = (targetStreak - 1).coerceAtLeast(0)
        binding.tvStreakValueCurrent.text = startStreak.toString()
        animateStreakSlideUp(startStreak, targetStreak)
        showContinueButtonWithDelay()

        binding.btnBackToLessonList.setOnClickListener {
            val eventId = System.currentTimeMillis()
            // Invalidate cached lists when user finished a lesson.
            findNavController().getBackStackEntry(R.id.lessonListFragment)
                .savedStateHandle[KEY_LESSON_COMPLETED_EVENT_ID] = eventId
            findNavController().getBackStackEntry(R.id.homeFragment)
                .savedStateHandle[KEY_LESSON_COMPLETED_EVENT_ID] = eventId

            findNavController().popBackStack(R.id.lessonListFragment, false)
        }
    }

    private fun animateStreakSlideUp(from: Int, to: Int) {
        if (from == to) {
            binding.tvStreakValueCurrent.text = to.toString()
            return
        }

        binding.tvStreakValueCurrent.post {
            if (_binding == null) return@post

            val offset = binding.tvStreakValueCurrent.height
                .takeIf { it > 0 }
                ?.toFloat()
                ?: binding.tvStreakValueCurrent.textSize

            binding.tvStreakValueNext.apply {
                text = to.toString()
                translationY = offset
                alpha = 0f
                isVisible = true
            }

            binding.tvStreakValueCurrent.animate()
                .translationY(-offset)
                .alpha(0f)
                .setDuration(500L)
                .start()

            binding.tvStreakValueNext.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500L)
                .withEndAction {
                    if (_binding == null) return@withEndAction
                    binding.tvStreakValueCurrent.apply {
                        text = to.toString()
                        translationY = 0f
                        alpha = 1f
                    }
                    binding.tvStreakValueNext.apply {
                        translationY = 0f
                        alpha = 1f
                        isVisible = false
                    }
                }
                .start()
        }
    }

    private fun showContinueButtonWithDelay() {
        binding.btnBackToLessonList.isVisible = false
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1500L)
            if (_binding == null) return@launch
            binding.btnBackToLessonList.apply {
                alpha = 0f
                translationY = 16f
                isVisible = true
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(260L)
                    .start()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
