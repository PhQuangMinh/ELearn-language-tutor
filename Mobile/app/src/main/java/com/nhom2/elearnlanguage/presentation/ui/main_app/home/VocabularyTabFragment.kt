package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.databinding.FragmentVocabularyTabBinding
import com.nhom2.elearnlanguage.domain.model.vocabulary.VocabularyType
import com.nhom2.elearnlanguage.presentation.ui.main_app.home.vocabulary.PageAdapter
import com.nhom2.elearnlanguage.presentation.ui.main_app.home.vocabulary.TopicVocabularyUiState
import com.nhom2.elearnlanguage.presentation.ui.main_app.home.vocabulary.TopicVocabularyViewModel
import com.nhom2.elearnlanguage.presentation.ui.main_app.home.vocabulary.VocabularyCardAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VocabularyTabFragment : Fragment() {

    private var _binding: FragmentVocabularyTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TopicVocabularyViewModel by viewModels()

    private lateinit var vocabAdapter: VocabularyCardAdapter
    private lateinit var pageAdapter: PageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVocabularyTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupList()
        setupTabs()

        binding.backButton.root.setOnClickListener {
            runCatching { findNavController().navigateUp() }
                .onFailure { requireActivity().onBackPressedDispatcher.onBackPressed() }
        }

        observeState()

        // Mock: open a sample topic (replace with real topic selection later)
        viewModel.load(topicId = 1, topicTitle = "Travelling")
    }

    private fun setupList() {
        vocabAdapter = VocabularyCardAdapter()
        binding.rvVocabulary.adapter = vocabAdapter

        pageAdapter = PageAdapter { page -> viewModel.selectPage(page) }
        binding.rvPages.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rvPages.adapter = pageAdapter
    }

    private fun setupTabs() {
        binding.tvTabNoun.setOnClickListener { viewModel.selectType(VocabularyType.NOUN) }
        binding.tvTabVerb.setOnClickListener { viewModel.selectType(VocabularyType.VERB) }
        binding.tvTabAdjective.setOnClickListener { viewModel.selectType(VocabularyType.ADJECTIVE) }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is TopicVocabularyUiState.Idle -> {
                            setLoading(false)
                            binding.tvError.visibility = View.GONE
                        }
                        is TopicVocabularyUiState.Loading -> {
                            setLoading(true)
                            binding.tvError.visibility = View.GONE
                        }
                        is TopicVocabularyUiState.Success -> {
                            setLoading(false)
                            binding.tvError.visibility = View.GONE

                            binding.tvTopicTitle.text = state.topicTitle
                            renderTabs(state.selectedType)

                            vocabAdapter.collapseAll()
                            vocabAdapter.submitList(state.items)

                            pageAdapter.selectedPage = state.currentPage
                            pageAdapter.submitList(state.pages)
                        }
                        is TopicVocabularyUiState.Error -> {
                            setLoading(false)
                            binding.tvError.visibility = View.VISIBLE
                            binding.tvError.text = state.message ?: "Something went wrong"
                        }
                    }
                }
            }
        }
    }

    private fun renderTabs(selected: VocabularyType) {
        val selectedColor = requireContext().getColor(com.nhom2.elearnlanguage.R.color.primary_100)
        val unselectedColor = requireContext().getColor(com.nhom2.elearnlanguage.R.color.neutral_60)

        binding.tvTabNoun.setTextColor(if (selected == VocabularyType.NOUN) selectedColor else unselectedColor)
        binding.tvTabVerb.setTextColor(if (selected == VocabularyType.VERB) selectedColor else unselectedColor)
        binding.tvTabAdjective.setTextColor(if (selected == VocabularyType.ADJECTIVE) selectedColor else unselectedColor)
    }

    private fun setLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        val contentVisibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.rvVocabulary.visibility = contentVisibility
        binding.rvPages.visibility = contentVisibility
        binding.llTypeTabs.visibility = contentVisibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
