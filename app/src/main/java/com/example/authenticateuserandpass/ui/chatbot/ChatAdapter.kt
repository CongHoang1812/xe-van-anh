package com.example.authenticateuserandpass.ui.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.databinding.ItemBotMessageBinding
import com.example.authenticateuserandpass.databinding.ItemBotOptionsBinding
import com.example.authenticateuserandpass.databinding.ItemUserMessageBinding
import kotlin.collections.get
import kotlin.compareTo

class ChatAdapter(
    private val onOptionClicked: (String, String) -> Unit
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_BOT = 1
        private const val VIEW_TYPE_BOT_OPTIONS = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when {
            message.isFromUser -> VIEW_TYPE_USER
            message.options != null -> VIEW_TYPE_BOT_OPTIONS
            else -> VIEW_TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemUserMessageBinding.inflate(inflater, parent, false)
                UserMessageViewHolder(binding)
            }
            VIEW_TYPE_BOT_OPTIONS -> {
                val binding = ItemBotOptionsBinding.inflate(inflater, parent, false)
                BotOptionsViewHolder(binding, onOptionClicked)
            }
            else -> {
                val binding = ItemBotMessageBinding.inflate(inflater, parent, false)
                BotMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is BotMessageViewHolder -> holder.bind(message)
            is BotOptionsViewHolder -> holder.bind(message)
        }
    }

    class UserMessageViewHolder(
        private val binding: ItemUserMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvUserMessage.text = message.message
            binding.tvUserTime.text = message.getFormattedTime()
        }
    }

    class BotMessageViewHolder(
        private val binding: ItemBotMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvBotMessage.text = message.message
            binding.tvBotTime.text = message.getFormattedTime()
        }
    }

    class BotOptionsViewHolder(
        private val binding: ItemBotOptionsBinding,
        private val onOptionClicked: (String, String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvBotMessage.text = message.message
            binding.tvBotTime.text = message.getFormattedTime()

            message.options?.let { options ->
                if (options.size >= 2) {
                    binding.btnOption1.text = options[0].text
                    binding.btnOption2.text = options[1].text

                    binding.btnOption1.setOnClickListener {
                        onOptionClicked(options[0].value, options[0].text)
                    }

                    binding.btnOption2.setOnClickListener {
                        onOptionClicked(options[1].value, options[1].text)
                    }
                }
            }
        }
    }
}

class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem == newItem
    }
}
