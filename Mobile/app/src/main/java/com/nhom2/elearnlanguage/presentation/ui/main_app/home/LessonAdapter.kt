package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.databinding.ItemCurrentLessonBinding
import com.nhom2.elearnlanguage.domain.model.CurrentLesson

class LessonAdapter(
    private val onClickLesson: (CurrentLesson) -> Unit
) : ListAdapter<CurrentLesson, LessonAdapter.LessonViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemCurrentLessonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LessonViewHolder(
        private val binding: ItemCurrentLessonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: CurrentLesson) {
            binding.tvLessonLabel.text = "LESSON ${lesson.lessonNumber}"
            binding.tvLessonLevel.text = "${lesson.level} Level"
            binding.root.setOnClickListener { onClickLesson(lesson) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CurrentLesson>() {
            override fun areItemsTheSame(a: CurrentLesson, b: CurrentLesson) = a.id == b.id
            override fun areContentsTheSame(a: CurrentLesson, b: CurrentLesson) = a == b
        }
    }
}
