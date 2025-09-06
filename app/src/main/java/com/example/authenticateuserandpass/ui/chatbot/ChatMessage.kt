package com.example.authenticateuserandpass.ui.chatbot

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val options: List<ChatOption>? = null
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

data class ChatOption(
    val id: String,
    val text: String,
    val value: String
)

enum class MessageType {
    USER_MESSAGE,
    BOT_MESSAGE,
    BOT_OPTIONS
}

