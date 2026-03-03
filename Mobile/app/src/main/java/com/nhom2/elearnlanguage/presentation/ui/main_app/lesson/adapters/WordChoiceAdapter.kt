package com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.ItemWordChoiceBinding

class WordChoiceAdapter(
    initialWords: List<WordChoiceItem>,
    private val onWordClicked: (String) -> Unit
) : ListAdapter<WordChoiceAdapter.WordChoiceItem, WordChoiceAdapter.WordChoiceViewHolder>(DiffCallback()) {

    data class WordChoiceItem(
        val word: String,
        val isUsed: Boolean = false
    )

    inner class WordChoiceViewHolder(private val binding: ItemWordChoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WordChoiceItem) {
            binding.tvWord.apply {
                text = item.word
                isEnabled = !item.isUsed

                // Change background and text color based on usage
                if (item.isUsed) {
                    setBackgroundResource(R.drawable.button_with_shadow_disabled)
                    setTextColor(ContextCompat.getColor(context, R.color.neutral_80))
                } else {
                    setBackgroundResource(R.drawable.button_with_shadow)
                    setTextColor(ContextCompat.getColor(context, android.R.color.white))
                }

                setOnClickListener {
                    if (!item.isUsed) {
                        markWordAsUsed(item.word)
                        onWordClicked(item.word)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordChoiceViewHolder {
        val binding = ItemWordChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordChoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordChoiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    init {
        submitList(initialWords.map { it.copy() })
    }

    fun markWordAsAvailable(word: String) {
        val updated = currentList.map {
            if (it.word == word) it.copy(isUsed = false) else it
        }
        submitList(updated)
    }

    fun markWordAsUsed(word: String) {
        val updated = currentList.map {
            if (it.word == word) it.copy(isUsed = true) else it
        }
        submitList(updated)
    }

    fun resetAll() {
        submitList(currentList.map { it.copy(isUsed = false) })
    }

    class DiffCallback : DiffUtil.ItemCallback<WordChoiceItem>() {
        override fun areItemsTheSame(oldItem: WordChoiceItem, newItem: WordChoiceItem): Boolean {
            return oldItem.word == newItem.word
        }

        override fun areContentsTheSame(oldItem: WordChoiceItem, newItem: WordChoiceItem): Boolean {
            return oldItem == newItem
        }
    }
}
