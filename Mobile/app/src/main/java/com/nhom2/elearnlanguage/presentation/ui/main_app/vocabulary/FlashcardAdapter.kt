package com.nhom2.elearnlanguage.presentation.ui.main_app.vocabulary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.databinding.FlashcardItemBinding
import com.nhom2.elearnlanguage.domain.model.Flashcard

class FlashcardAdapter :
    ListAdapter<Flashcard, FlashcardAdapter.FlashcardViewHolder>(DiffCallback()) {

    inner class FlashcardViewHolder(
        private val binding: FlashcardItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isFront = true

        fun bind(card: Flashcard) {
            // Reset to front view
            isFront = true
            binding.cardContainer.rotationY = 0f
            binding.tvWord.visibility = View.VISIBLE
            binding.backView.visibility = View.GONE
            binding.backView.rotationY = 180f // Pre-rotate back view so it displays correctly when container is at 180
            
            binding.tvWord.text = "${card.word}\n${card.pronunciation}"
            binding.image.load(card.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.app_logo)
                error(R.drawable.app_logo)
            }

            binding.backView.findViewById<TextView>(R.id.tvMeaning)?.text = card.meaning + "\n" + card.example

            val scale = binding.root.context.resources.displayMetrics.density
            binding.cardContainer.cameraDistance = 8000 * scale

            binding.cardContainer.setOnClickListener {
                // Animate flip at 90 degrees, switch views
                binding.cardContainer.animate()
                    .rotationY(90f)
                    .setDuration(200)
                    .withEndAction {
                        if (isFront) {
                            // Switch to back
                            binding.tvWord.visibility = View.GONE
                            binding.backView.visibility = View.VISIBLE
                        } else {
                            // Switch to front
                            binding.backView.visibility = View.GONE
                            binding.tvWord.visibility = View.VISIBLE
                        }
                        
                        // Continue animation
                        binding.cardContainer.animate()
                            .rotationY(if (isFront) 180f else 0f)
                            .setDuration(200)
                            .start()
                            
                        isFront = !isFront
                    }
                    .start()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val binding = FlashcardItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FlashcardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Flashcard>() {

        override fun areItemsTheSame(oldItem: Flashcard, newItem: Flashcard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Flashcard, newItem: Flashcard): Boolean {
            return oldItem == newItem
        }
    }
}