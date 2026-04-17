package com.nhom2.elearnlanguage.presentation.ui.main_app.home.vocabulary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FragmentTopicVocabularyBinding
import com.nhom2.elearnlanguage.domain.model.vocabulary.VocabularyType
import com.nhom2.elearnlanguage.presentation.ui.main_app.vocabulary.FlashcardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TopicVocabularyFragment : Fragment() {

    private var _binding: FragmentTopicVocabularyBinding? = null
    private val binding get() = _binding!!

    private val args: TopicVocabularyFragmentArgs by navArgs()
    private val viewModel: TopicVocabularyViewModel by hiltNavGraphViewModels(R.id.nav_main)
    private val flashcardViewModel: FlashcardViewModel by activityViewModels()

    private lateinit var vocabAdapter: VocabularyCardAdapter
    private lateinit var pageAdapter: PageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicVocabularyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.root.setOnClickListener { findNavController().navigateUp() }

        binding.tvModeFlashcard.setOnClickListener {
            findNavController().navigate(
                TopicVocabularyFragmentDirections.actionTopicVocabularyFragmentToFlashcardFragment(
                    topicId = args.topicId,
                    topicTitle = args.topicName
                )
            )
        }

        setupList()
        setupTabs()

        observe()

        viewModel.load(
            topicId = args.topicId,
            topicTitle = args.topicName
        )

        // Prefetch flashcards as soon as vocabulary screen opens
        // so switching to Flashcard mode feels instant.
        flashcardViewModel.getFlashcards(args.topicId)
    }

    private fun setupList() {
        vocabAdapter = VocabularyCardAdapter()
        binding.rvVocabulary.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVocabulary.adapter = vocabAdapter

        pageAdapter = PageAdapter { page ->
            viewModel.selectPage(page)
            vocabAdapter.collapseAll()
        }
        binding.rvPages.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rvPages.adapter = pageAdapter
    }

    private fun setupTabs() {
        binding.tvTabNoun.setOnClickListener {
            viewModel.selectType(VocabularyType.NOUN)
            vocabAdapter.collapseAll()
        }
        binding.tvTabVerb.setOnClickListener {
            viewModel.selectType(VocabularyType.VERB)
            vocabAdapter.collapseAll()
        }
        binding.tvTabAdjective.setOnClickListener {
            viewModel.selectType(VocabularyType.ADJECTIVE)
            vocabAdapter.collapseAll()
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is TopicVocabularyUiState.Idle -> showLoading(false)
                        is TopicVocabularyUiState.Loading -> showLoading(true)
                        is TopicVocabularyUiState.Error -> {
                            showLoading(false)
                            binding.tvError.visibility = View.VISIBLE
                            binding.tvError.text = state.message ?: getString(R.string.error_generic)
                        }
                        is TopicVocabularyUiState.Success -> {
                            showLoading(false)
                            binding.tvTopicTitle.text = state.topicTitle
                            renderTabs(state.selectedType)

                            vocabAdapter.submitList(state.items)

                            pageAdapter.selectedPage = state.currentPage
                            pageAdapter.submitList(state.pages)

                            binding.tvError.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun renderTabs(selected: VocabularyType) {
        val selectedColor = requireContext().getColor(R.color.primary_100)
        val unselectedColor = requireContext().getColor(R.color.neutral_60)

        binding.tvTabNoun.setTextColor(if (selected == VocabularyType.NOUN) selectedColor else unselectedColor)
        binding.tvTabVerb.setTextColor(if (selected == VocabularyType.VERB) selectedColor else unselectedColor)
        binding.tvTabAdjective.setTextColor(if (selected == VocabularyType.ADJECTIVE) selectedColor else unselectedColor)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        val contentVisibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.rvVocabulary.visibility = contentVisibility
        binding.rvPages.visibility = contentVisibility
        binding.llTypeTabs.visibility = contentVisibility
        binding.llModeSegment.visibility = View.VISIBLE
        if (isLoading) binding.tvError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

