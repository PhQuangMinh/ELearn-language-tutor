package com.nhom2.elearnlanguage.presentation.ui.mainscreen.lessonlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.ItemLessonBinding

class LessonAdapter(
    private val onClick: (LessonRowUiModel) -> Unit
) : ListAdapter<LessonRowUiModel, LessonAdapter.LessonVH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonVH {
        val binding = ItemLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LessonVH(binding, onClick)
    }

    override fun onBindViewHolder(holder: LessonVH, position: Int) {
        holder.bind(getItem(position))
    }

    class LessonVH(
        private val binding: ItemLessonBinding,
        private val onClick: (LessonRowUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LessonRowUiModel) {
            val ctx = binding.root.context

            binding.tvTitle.text = item.title

            // Lesson image from URL
            binding.ivLessonImage.load(item.imageUrl) {
                placeholder(R.drawable.app_logo)
                error(R.drawable.app_logo)
                crossfade(true)
            }

            // Completed state: light-green background
            if (item.completed) {
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.success_80)
                )
                binding.root.strokeColor =
                    ContextCompat.getColor(ctx, R.color.success_100)
            } else {
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.white)
                )
                binding.root.strokeColor =
                    ContextCompat.getColor(ctx, R.color.border_gray)
            }
            binding.root.alpha = 1f

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<LessonRowUiModel>() {
        override fun areItemsTheSame(oldItem: LessonRowUiModel, newItem: LessonRowUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LessonRowUiModel, newItem: LessonRowUiModel): Boolean {
            return oldItem == newItem
        }
    }
}

