package com.nhom2.elearnlanguage.presentation.ui.main_app.home.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.ItemPageNumberBinding

class PageAdapter(
    private val onClick: (page: Int) -> Unit
) : ListAdapter<Int, PageAdapter.VH>(Diff) {

    var selectedPage: Int = 1
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPageNumberBinding.inflate(
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
        private val binding: ItemPageNumberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: Int) {
            val isSelected = page == selectedPage
            binding.tvPage.text = page.toString()

            if (isSelected) {
                binding.tvPage.background = ContextCompat.getDrawable(binding.root.context, R.drawable.bg_page_selected)
                binding.tvPage.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
            } else {
                binding.tvPage.background = null
                binding.tvPage.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primary_100))
            }

            binding.root.setOnClickListener { onClick(page) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean = oldItem == newItem
    }
}

