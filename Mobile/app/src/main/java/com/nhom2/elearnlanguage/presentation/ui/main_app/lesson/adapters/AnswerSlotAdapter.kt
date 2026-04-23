package com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.ItemAnswerSlotBinding

class AnswerSlotAdapter(
    initialData: List<AnswerSlotItem>,
    private val onSlotClicked: (Int, String, Int?) -> Unit
) : ListAdapter<AnswerSlotAdapter.AnswerSlotItem, AnswerSlotAdapter.AnswerSlotViewHolder>(DiffCallback()) {

    data class AnswerSlotItem(
        val index: Int,
        val correctWord: String,
        val selectedWord: String = "",
        val selectedWordId: Int? = null,
        val state: SlotState = SlotState.EMPTY
    )

    enum class SlotState {
        EMPTY, FILLED, CORRECT, WRONG
    }

    inner class AnswerSlotViewHolder(private val binding: ItemAnswerSlotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnswerSlotItem) {
            binding.tvSlot.apply {
                text = item.selectedWord

                when (item.state) {
                    SlotState.EMPTY -> {
                        setBackgroundResource(R.drawable.bottom_border)
                        setTextColor(ContextCompat.getColor(context, R.color.neutral_100))
                    }
                    SlotState.FILLED -> {
                        setBackgroundResource(R.drawable.bottom_border)
                        setTextColor(ContextCompat.getColor(context, R.color.neutral_100))
                    }
                    SlotState.CORRECT -> {
                        setBackgroundResource(R.drawable.button_selected_with_shadow)
                        setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                    SlotState.WRONG -> {
                        setTextColor(ContextCompat.getColor(context, R.color.error_100))
                    }
                }

                // Handle click to remove word
                setOnClickListener {
                    if (item.selectedWord.isNotEmpty() && item.state == SlotState.FILLED) {
                        val removedWord = item.selectedWord
                        val removedWordId = item.selectedWordId
                        updateSlot(item.index, "")
                        onSlotClicked(item.index, removedWord, removedWordId)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerSlotViewHolder {
        val binding = ItemAnswerSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnswerSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnswerSlotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    init {
        submitList(initialData.map { it.copy() })
    }

    fun updateSlot(index: Int, word: String, wordId: Int? = null) {
        val updated = currentList.map {
            if (it.index == index) {
                if (word.isBlank()) it.copy(selectedWord = "", selectedWordId = null, state = SlotState.EMPTY)
                else it.copy(selectedWord = word, selectedWordId = wordId, state = SlotState.FILLED)
            } else it
        }
        submitList(updated)
    }

    fun clearSlot(index: Int) {
        val updated = currentList.map {
            if (it.index == index) it.copy(selectedWord = "", selectedWordId = null, state = SlotState.EMPTY)
            else it
        }
        submitList(updated)
    }

    fun updateSlotState(index: Int, state: SlotState) {
        val updated = currentList.map {
            if (it.index == index) it.copy(state = state) else it
        }
        submitList(updated)
    }

    fun updateAllSlotStates(states: List<SlotState>) {
        val updated = currentList.mapIndexed { index, item ->
            if (index < states.size) {
                item.copy(state = states[index])
            } else {
                item
            }
        }
        submitList(updated)
    }

    fun getAnswerText(): List<String> = currentList.map { it.selectedWord }

    class DiffCallback : DiffUtil.ItemCallback<AnswerSlotItem>() {
        override fun areItemsTheSame(oldItem: AnswerSlotItem, newItem: AnswerSlotItem): Boolean {
            return oldItem.index == newItem.index
        }

        override fun areContentsTheSame(oldItem: AnswerSlotItem, newItem: AnswerSlotItem): Boolean {
            return oldItem == newItem
        }
    }
}
