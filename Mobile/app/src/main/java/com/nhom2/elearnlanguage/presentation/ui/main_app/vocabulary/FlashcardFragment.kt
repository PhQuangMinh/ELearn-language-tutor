package com.nhom2.elearnlanguage.presentation.ui.main_app.vocabulary

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import androidx.viewpager2.widget.ViewPager2
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentFlashCardBinding
import com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters.ProgressSegmentAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FlashcardFragment : Fragment() {

    private var _binding: FragmentFlashCardBinding? = null
    private val binding get() = _binding!!

    // Keep VM across fragment recreation so switching mode doesn't refetch every time.
    private val viewModel: FlashcardViewModel by activityViewModels()
    private lateinit var adapter: FlashcardAdapter
    private lateinit var progressAdapter: ProgressSegmentAdapter
    private val args: FlashcardFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val topicId = args.topicId
        val topicTitle = args.topicTitle

        // Set title
        binding.tvTitle.text = topicTitle

        // Setup UI
        setupAdapter()
        setupViewPager()
        setUpObservers()
        setupBackButton()

        binding.tvModeVocab.setOnClickListener {
            val nav = findNavController()
            if (nav.previousBackStackEntry?.destination?.id == R.id.topicVocabularyFragment) {
                nav.popBackStack()
            } else {
                nav.navigate(
                    FlashcardFragmentDirections.actionFlashcardFragmentToTopicVocabularyFragment(
                        topicId = topicId,
                        topicName = topicTitle
                    ),
                    NavOptions.Builder()
                        .setPopUpTo(R.id.flashcardFragment, true)
                        .build()
                )
            }
        }

        // Fetch flashcards (cached by topicId inside VM)
        viewModel.getFlashcards(topicId)
    }

    private fun setupAdapter() {
        adapter = FlashcardAdapter()
    }

    private fun setupViewPager() {
        binding.vpFLashcard.adapter = adapter
        binding.vpFLashcard.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.vpFLashcard.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setCurrentIndex(position)
            }
        })
    }

    private fun setUpObservers() {
        // Observe flashcards collection
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.flashcards.collect { flashcards ->
                    Log.d("FlashcardFragment", "Received ${flashcards.size} flashcards")
                    adapter.submitList(flashcards)
                    // Initialize progress bar on first load
                    if (flashcards.isNotEmpty() && !::progressAdapter.isInitialized) {
                        progressAdapter = ProgressSegmentAdapter(flashcards.size)
                        binding.rvProgressBar.apply {
                            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                                flexDirection = FlexDirection.ROW
                                flexWrap = FlexWrap.WRAP
                                // Hàng chưa đủ width (vd. hàng 2 sau khi wrap) căn giữa trang
                                justifyContent = JustifyContent.CENTER
                            }
                            adapter = progressAdapter
                        }

                        // Set initial progress to show first item
                        progressAdapter.updateProgress(viewModel.currentIndex.value + 1)
                        
                        // Set initial button states
                        updateButtonStates(viewModel.currentIndex.value)
                    }
                }
            }
        }

        // Update progress and button states when current position changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentIndex.collect { position ->
                    if (::progressAdapter.isInitialized) {
                        progressAdapter.updateProgress(position + 1)
                    }
                    updateButtonStates(position)
                }
            }
        }
    }

    private fun updateButtonStates(position: Int) {
        val totalCount = viewModel.flashcards.value.size
        binding.btnPrevious.isEnabled = position > 0
        binding.btnPrevious.alpha = if (position > 0) 1f else 0.5f

        binding.btnNext.isEnabled = position < totalCount - 1
        binding.btnNext.alpha = if (position < totalCount - 1) 1f else 0.5f
    }

    private fun setupBackButton() {
        binding.btnBack.root.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnPrevious.setOnClickListener {
            val currentPosition = binding.vpFLashcard.currentItem
            if (currentPosition > 0) {
                binding.vpFLashcard.setCurrentItem(currentPosition - 1, true)
            }
        }

        binding.btnNext.setOnClickListener {
            val currentPosition = binding.vpFLashcard.currentItem
            if (currentPosition < adapter.itemCount - 1) {
                binding.vpFLashcard.setCurrentItem(currentPosition + 1, true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
