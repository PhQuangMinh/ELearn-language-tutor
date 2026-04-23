package com.nhom2.elearnlanguage.presentation.ui.onboarding

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nhom2.elearnlanguage.databinding.ItemOnboardingPageBinding

class OnboardingPagerAdapter(
    private val pages: List<OnboardingPageUi>
) : RecyclerView.Adapter<OnboardingPagerAdapter.PageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    class PageViewHolder(
        private val binding: ItemOnboardingPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: OnboardingPageUi) {
            with(binding) {
                val inputStream = root.context.resources.openRawResource(page.imageRes)
                val bitmap = inputStream.use { BitmapFactory.decodeStream(it) }
                ivOnboarding.setImageBitmap(bitmap)
                tvTitle.setText(page.titleRes)
                tvDescription.setText(page.descriptionRes)
            }
        }
    }
}
