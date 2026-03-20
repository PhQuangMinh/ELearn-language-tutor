package com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.nhom2.elearnlanguage.R

class AiConversationAdapter(
    private val onHintClick: (String) -> Unit,
    private val onSpeakerClick: (String) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(Diff) {

    private var expandedHintMessageId: String? = null
    private var expandedTranslateMessageId: String? = null

    fun submit(messages: List<Message>, expandedHintMessageId: String?, onCommitted: () -> Unit) {
        val expandedChanged = this.expandedHintMessageId != expandedHintMessageId
        this.expandedHintMessageId = expandedHintMessageId
        submitList(messages) {
            // expandedHintMessageId không nằm trong Message nên DiffUtil có thể không re-bind.
            // Force re-bind giúp panel "Gợi ý" hiển thị/ẩn đúng ngay khi bấm.
            if (expandedChanged) {
                notifyDataSetChanged()
            }
            onCommitted()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isFromAI) VIEW_TYPE_AI else VIEW_TYPE_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_AI -> AiMessageVH(inflater.inflate(R.layout.item_message_ai, parent, false))
            else -> UserMessageVH(inflater.inflate(R.layout.item_message_user, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is AiMessageVH -> holder.bind(
                message = item,
                isExpanded = item.id == expandedHintMessageId,
                onHintClick = onHintClick,
                onSpeakerClick = onSpeakerClick
            )
            is UserMessageVH -> holder.bind(item)
        }
    }

    inner class AiMessageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvAiText)
        private val tvTranslation: TextView = itemView.findViewById(R.id.tvAiTranslation)
        private val btnHint: TextView = itemView.findViewById(R.id.btnHint)
        private val btnTranslate: TextView = itemView.findViewById(R.id.btnTranslate)
        private val hintPanel: View = itemView.findViewById(R.id.hintPanel)
        private val tvAnalysis: TextView = itemView.findViewById(R.id.tvHintAnalysis)
        private val tvSuggestion: TextView = itemView.findViewById(R.id.tvHintSuggestion)
        private val tvExample: TextView = itemView.findViewById(R.id.tvHintExample)
        private val ivSpeaker: ImageView = itemView.findViewById(R.id.ivSpeaker)

        fun bind(
            message: Message,
            isExpanded: Boolean,
            onHintClick: (String) -> Unit,
            onSpeakerClick: (String) -> Unit
        ) {
            tvText.text = message.text
            val translation = message.translation
            val canTranslate = !translation.isNullOrBlank()
            val showTranslation = message.id == expandedTranslateMessageId

            tvTranslation.isVisible = canTranslate && showTranslation
            tvTranslation.text = translation

            btnTranslate.isVisible = canTranslate
            btnTranslate.text = if (showTranslation) "Ẩn dịch" else "Dịch"

            ivSpeaker.setOnClickListener {
                if (message.text.isNotBlank()) onSpeakerClick(message.text)
            }

            val hasHint = message.hint != null
            btnHint.isVisible = hasHint
            hintPanel.isVisible = hasHint && isExpanded

            if (hasHint) {
                val h = message.hint!!
                tvAnalysis.isVisible = h.analysis.isNotBlank()
                tvExample.isVisible = h.example.isNotBlank()

                tvAnalysis.text = "Phân tích: ${h.analysis}"
                tvSuggestion.text = "Gợi ý: ${h.suggestion}"
                tvExample.text = "Ví dụ: ${h.example}"

                btnHint.text = if (isExpanded) "Ẩn gợi ý" else "Gợi ý"
                btnHint.setOnClickListener {
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup, AutoTransition())
                    onHintClick(message.id)
                }
            }

            if (message.translation != null && message.translation.isNotBlank()) {
                btnTranslate.setOnClickListener {
                    val next = if (expandedTranslateMessageId == message.id) null else message.id
                    expandedTranslateMessageId = next
                    this@AiConversationAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    class UserMessageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvUserText)
        fun bind(message: Message) {
            tvText.text = message.text
        }
    }

    private object Diff : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean = oldItem == newItem
    }

    private companion object {
        const val VIEW_TYPE_AI = 1
        const val VIEW_TYPE_USER = 2
    }
}

