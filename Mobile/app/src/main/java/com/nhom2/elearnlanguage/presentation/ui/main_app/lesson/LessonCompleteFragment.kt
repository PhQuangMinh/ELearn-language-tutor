package com.nhom2.elearnlanguage.presentation.ui.main_app.lesson

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentLessonCompleteBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LessonCompleteFragment : Fragment() {
    companion object {
        private const val TAG = "LessonCompleteFragment"
        private const val KEY_LESSON_COMPLETED_EVENT_ID = "lessonCompletedEventId"
    }

    private var _binding: FragmentLessonCompleteBinding? = null
    private val binding get() = _binding!!

    private val args: LessonCompleteFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val correct = args.correctCount
        val total = args.totalCount.coerceAtLeast(1)
        val percent = (correct * 100) / total

        binding.tvScoreText.text = getString(R.string.lesson_complete_score, correct, total)
        binding.progressScore.max = 100
        binding.progressScore.progress = percent
        Log.d(TAG, "onViewCreated streakExtended=${args.streakExtended}, currentStreak=${args.currentStreak}")

        val initialBottomMargin = (binding.btnContinue.layoutParams as ViewGroup.MarginLayoutParams)
            .bottomMargin

        // Move CTA above system navigation bar.
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnContinue) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialBottomMargin + navigationBars.bottom
            }
            insets
        }
        ViewCompat.requestApplyInsets(binding.btnContinue)

        binding.btnContinue.setOnClickListener {
            val eventId = System.currentTimeMillis()
            if (args.streakExtended) {
                // Invalidate cached lists when user finished a lesson.
                findNavController().getBackStackEntry(R.id.lessonListFragment)
                    .savedStateHandle[KEY_LESSON_COMPLETED_EVENT_ID] = eventId
                findNavController().getBackStackEntry(R.id.homeFragment)
                    .savedStateHandle[KEY_LESSON_COMPLETED_EVENT_ID] = eventId

                val action = LessonCompleteFragmentDirections
                    .actionLessonCompleteFragmentToLessonStreakFragment(args.currentStreak)
                findNavController().navigate(action)
            } else {
                // Invalidate cached lists when user finished a lesson.
                findNavController().getBackStackEntry(R.id.lessonListFragment)
                    .savedStateHandle[KEY_LESSON_COMPLETED_EVENT_ID] = eventId
                findNavController().getBackStackEntry(R.id.homeFragment)
                    .savedStateHandle[KEY_LESSON_COMPLETED_EVENT_ID] = eventId

                findNavController().popBackStack(R.id.lessonListFragment, false)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
