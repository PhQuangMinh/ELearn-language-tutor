package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.ItemCourseBinding
import com.nhom2.elearnlanguage.domain.model.CourseProgress

class CourseAdapter(
    private val onClickCourse: (CourseProgress) -> Unit
) : ListAdapter<CourseProgress, CourseAdapter.CourseViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CourseViewHolder(
        private val binding: ItemCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: CourseProgress) {
            binding.tvCourseName.text = course.title
            binding.progressBar.progress = course.progressPercent
            binding.tvProgressPercent.text = "${course.progressPercent}%"

            // Ưu tiên load ảnh thật từ API, nếu không có thì dùng app_logo mặc định
            if (course.imageUrl.isNotBlank()) {
                binding.ivCourse.load(course.imageUrl) {
                    placeholder(R.drawable.app_logo)
                    error(R.drawable.app_logo)
                    crossfade(true)
                }
            } else {
                binding.ivCourse.setImageResource(R.drawable.app_logo)
            }

            binding.root.setOnClickListener { onClickCourse(course) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CourseProgress>() {
            override fun areItemsTheSame(a: CourseProgress, b: CourseProgress) = a.id == b.id
            override fun areContentsTheSame(a: CourseProgress, b: CourseProgress) = a == b
        }
    }
}
