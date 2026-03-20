package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentSpeakingTabBinding
import com.nhom2.elearnlanguage.presentation.ui.main_app.home.HomeFragment
import com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking.AiConversationFragment

class SpeakingTabFragment : Fragment() {

    private var _binding: FragmentSpeakingTabBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeakingTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Debug-only entry to open the AI conversation screen.
        // Remove this block after testing to avoid affecting other behavior.
        if (BuildConfig.DEBUG) {
            binding.root.setOnLongClickListener {
                val aiFragment = AiConversationFragment().apply {
                    arguments = Bundle().apply { putInt("lessonId", 1) }
                }

                (parentFragment as? HomeFragment)?.childFragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.flTabContainer, aiFragment)
                    ?.addToBackStack(null)
                    ?.commit()

                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
