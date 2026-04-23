package com.nhom2.elearnlanguage.presentation.ui.main_app.home.vocabulary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.databinding.ItemVocabularyCardBinding

class VocabularyCardAdapter(
    private val onSpeakClick: (VocabularyCardUiModel) -> Unit
) : ListAdapter<VocabularyCardUiModel, VocabularyCardAdapter.VH>(Diff) {

    private var expandedWord: String? = null

    fun collapseAll() {
        val old = expandedWord ?: return
        expandedWord = null
        val oldIndex = currentList.indexOfFirst { it.word == old }
        if (oldIndex >= 0) notifyItemChanged(oldIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemVocabularyCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(
        private val binding: ItemVocabularyCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VocabularyCardUiModel) {
            val isExpanded = item.word == expandedWord

            binding.tvWord.text = item.word.replaceFirstChar { it.uppercase() }

            val meaningLine = if (isExpanded && !item.pronunciation.isNullOrBlank()) {
                "${item.meaning} - ${item.pronunciation}"
            } else {
                item.meaning
            }
            binding.tvMeaning.text = meaningLine

            binding.llExpandedContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivChevron.rotation = if (isExpanded) 180f else 0f

            binding.tvDefinition.text = item.definition.orEmpty()
            binding.tvExample.text = item.example.orEmpty()

            binding.root.setOnClickListener { toggle(item) }
            binding.ivChevron.setOnClickListener { toggle(item) }
            binding.ivSpeaker.setOnClickListener { onSpeakClick(item) }
        }

        private fun toggle(item: VocabularyCardUiModel) {
            val old = expandedWord
            val new = if (expandedWord == item.word) null else item.word
            expandedWord = new

            val oldIndex = old?.let { w -> currentList.indexOfFirst { it.word == w } } ?: -1
            val newIndex = currentList.indexOfFirst { it.word == item.word }

            if (oldIndex >= 0) notifyItemChanged(oldIndex)
            if (newIndex >= 0) notifyItemChanged(newIndex)
        }
    }

    private object Diff : DiffUtil.ItemCallback<VocabularyCardUiModel>() {
        override fun areItemsTheSame(oldItem: VocabularyCardUiModel, newItem: VocabularyCardUiModel): Boolean {
            return oldItem.word == newItem.word
        }

        override fun areContentsTheSame(oldItem: VocabularyCardUiModel, newItem: VocabularyCardUiModel): Boolean {
            return oldItem == newItem
        }
    }
}

