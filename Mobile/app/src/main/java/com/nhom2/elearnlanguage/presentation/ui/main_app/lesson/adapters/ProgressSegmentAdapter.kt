package com.nhom2.elearnlanguage.presentation.ui.main_app.lesson.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.R

class ProgressSegmentAdapter(
    private val totalSegments: Int
) : ListAdapter<Int, ProgressSegmentAdapter.ProgressSegmentViewHolder>(DiffCallback()) {

    private var currentProgress: Int = 0

    inner class ProgressSegmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val segmentView: View = itemView.findViewById(R.id.progressSegment)

        fun bind(position: Int) {
            val background = if (position < currentProgress) {
                R.drawable.progress_segment_filled
            } else {
                R.drawable.progress_segment_empty
            }
            segmentView.setBackgroundResource(background)

            // Remove margin from last item
            val layoutParams = segmentView.layoutParams as ViewGroup.MarginLayoutParams
            if (position == totalSegments - 1) {
                layoutParams.marginEnd = 0
            } else {
                layoutParams.marginEnd = 4.dpToPx()
            }
            segmentView.layoutParams = layoutParams
        }

        private fun Int.dpToPx(): Int {
            return (this * itemView.context.resources.displayMetrics.density).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressSegmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress_segment, parent, false)
        return ProgressSegmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressSegmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    init {
        submitList((0 until totalSegments).toList())
    }

    fun updateProgress(progress: Int) {
        if (progress != currentProgress) {
            currentProgress = progress.coerceIn(0, totalSegments)
            notifyDataSetChanged()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean = oldItem == newItem
    }
}
