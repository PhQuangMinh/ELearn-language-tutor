package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.ItemVocabularyTopicBinding
import com.nhom2.elearnlanguage.domain.model.CourseProgress

class VocabularyTopicAdapter(
    private val onTopicClick: (CourseProgress) -> Unit
) : ListAdapter<CourseProgress, VocabularyTopicAdapter.TopicViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemVocabularyTopicBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TopicViewHolder(
        private val binding: ItemVocabularyTopicBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: CourseProgress) {
            binding.tvTopicName.text = course.title
            if (course.imageUrl.isNotBlank()) {
                binding.ivTopicImage.load(course.imageUrl) {
                    placeholder(R.drawable.app_logo)
                    error(R.drawable.app_logo)
                    crossfade(true)
                }
            } else {
                binding.ivTopicImage.setImageResource(R.drawable.app_logo)
            }
            binding.root.setOnClickListener { onTopicClick(course) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CourseProgress>() {
            override fun areItemsTheSame(a: CourseProgress, b: CourseProgress) = a.id == b.id
            override fun areContentsTheSame(a: CourseProgress, b: CourseProgress) = a == b
        }
    }
}
