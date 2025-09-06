package com.example.authenticateuserandpass.ui.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatbotViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val currentMessages = mutableListOf<ChatMessage>()

    init {
        // Welcome message
        addBotMessage("Xin chào! Tôi là trợ lý nhà xe Vân Anh. Tôi có thể giúp bạn tìm hiểu về:\n\n• Giá vé xe\n• Tuyến đường\n• Thông tin liên hệ\n• Hủy vé\n\nBạn cần hỗ trợ gì?")
    }

    fun sendMessage(message: String) {
        // Add user message
        addUserMessage(message)

        // Process and respond
        val response = processUserMessage(message.lowercase().trim())
        addBotMessage(response.message, response.options)
    }

    fun handleOptionSelected(optionValue: String, optionText: String) {
        // Add user's selection as a message
        addUserMessage(optionText)

        // Respond based on option
        val response = when (optionValue) {
            "satisfied" -> "Cảm ơn bạn đã đánh giá! Chúng tôi rất vui khi được hỗ trợ bạn. Bạn có cần hỗ trợ gì khác không?"
            "not_satisfied" -> "Tôi rất tiếc vì chưa hỗ trợ tốt cho bạn. Bạn có thể liên hệ hotline 1900-xxxx để được hỗ trợ trực tiếp từ nhân viên. Có gì khác tôi có thể giúp không?"
            else -> "Cảm ơn bạn đã phản hồi!"
        }
        addBotMessage(response)
    }

    private fun addUserMessage(message: String) {
        val chatMessage = ChatMessage(
            message = message,
            isFromUser = true
        )
        currentMessages.add(chatMessage)
        _messages.value = currentMessages.toList()
    }

    private fun addBotMessage(message: String, options: List<ChatOption>? = null) {
        val chatMessage = ChatMessage(
            message = message,
            isFromUser = false,
            options = options
        )
        currentMessages.add(chatMessage)
        _messages.value = currentMessages.toList()
    }

    private fun processUserMessage(message: String): BotResponse {
        return when {
            // Giá vé
            message.contains("giá") || message.contains("vé") || message.contains("bao nhiêu") || message.contains("tiền") -> {
                BotResponse(
                    "Giá vé của chúng tôi phụ thuộc vào tuyến đường:\n\n" +
                            "🚌 Hà Nội - TP.HCM: 450,000đ - 550,000đ\n" +
                            "🚌 Hà Nội - Đà Nẵng: 350,000đ - 420,000đ\n" +
                            "🚌 TP.HCM - Đà Lạt: 180,000đ - 220,000đ\n" +
                            "🚌 Hà Nội - Hải Phòng: 120,000đ - 150,000đ\n\n" +
                            "Giá có thể thay đổi theo ngày và loại ghế. Bạn có hài lòng với thông tin này không?",
                    listOf(
                        ChatOption("satisfied", "Hài lòng", "satisfied"),
                        ChatOption("not_satisfied", "Không hài lòng", "not_satisfied")
                    )
                )
            }

            // Tuyến đường
            message.contains("tuyến") || message.contains("đường") || message.contains("lộ trình") ||
                    message.contains("chạy") || message.contains("đi đâu") -> {
                BotResponse(
                    "Chúng tôi có các tuyến đường chính:\n\n" +
                            "🛣️ Miền Bắc:\n" +
                            "• Hà Nội - Hải Phòng - Quảng Ninh\n" +
                            "• Hà Nội - Thanh Hóa - Nghệ An\n\n" +
                            "🛣️ Miền Trung:\n" +
                            "• Đà Nẵng - Huế - Quảng Bình\n" +
                            "• Hà Nội - Đà Nẵng (cao tốc)\n\n" +
                            "🛣️ Miền Nam:\n" +
                            "• TP.HCM - Vũng Tàu - Phan Thiết\n" +
                            "• TP.HCM - Đà Lạt - Nha Trang\n\n" +
                            "Thông tin này có hữu ích không?",
                    listOf(
                        ChatOption("satisfied", "Hài lòng", "satisfied"),
                        ChatOption("not_satisfied", "Không hài lòng", "not_satisfied")
                    )
                )
            }

            // Liên hệ
            message.contains("liên hệ") || message.contains("số điện thoại") || message.contains("hotline") ||
                    message.contains("gọi") || message.contains("phone") -> {
                BotResponse(
                    "Thông tin liên hệ nhà xe Vân Anh:\n\n" +
                            "📞 Hotline: 1900-8080\n" +
                            "📱 Zalo: 0901-234-567\n" +
                            "🌐 Website: www.nhaxevananh.com\n" +
                            "📧 Email: support@nhaxevananh.com\n\n" +
                            "🕒 Thời gian hỗ trợ: 24/7\n" +
                            "📍 Văn phòng chính: 123 Đường ABC, Quận 1, TP.HCM\n\n" +
                            "Bạn có hài lòng với thông tin này không?",
                    listOf(
                        ChatOption("satisfied", "Hài lòng", "satisfied"),
                        ChatOption("not_satisfied", "Không hài lòng", "not_satisfied")
                    )
                )
            }

            // Hủy vé
            message.contains("hủy") || message.contains("hoàn") || message.contains("trả") ||
                    message.contains("cancel") || message.contains("refund") -> {
                BotResponse(
                    "Chính sách hủy vé nhà xe Vân Anh:\n\n" +
                            "✅ Hủy trước 24h: Hoàn 100% giá vé\n" +
                            "✅ Hủy trước 12h: Hoàn 80% giá vé\n" +
                            "✅ Hủy trước 6h: Hoàn 50% giá vé\n" +
                            "❌ Hủy dưới 6h: Không hoàn tiền\n\n" +
                            "Để hủy vé, bạn có thể:\n" +
                            "• Gọi hotline: 1900-8080\n" +
                            "• Nhắn tin Zalo: 0901-234-567\n" +
                            "• Đến trực tiếp bến xe\n\n" +
                            "Thông tin này có đầy đủ không?",
                    listOf(
                        ChatOption("satisfied", "Hài lòng", "satisfied"),
                        ChatOption("not_satisfied", "Không hài lòng", "not_satisfied")
                    )
                )
            }

            // Chào hỏi
            message.contains("chào") || message.contains("hello") || message.contains("hi") -> {
                BotResponse("Xin chào! Tôi có thể hỗ trợ bạn tìm hiểu về giá vé, tuyến đường, thông tin liên hệ hoặc chính sách hủy vé. Bạn cần biết thông tin gì?")
            }

            // Cảm ơn
            message.contains("cảm ơn") || message.contains("thanks") || message.contains("thank you") -> {
                BotResponse("Không có gì! Tôi rất vui được hỗ trợ bạn. Bạn có cần hỗ trợ gì khác không?")
            }

            // Mặc định
            else -> {
                BotResponse(
                    "Tôi chưa hiểu rõ câu hỏi của bạn. Tôi có thể hỗ trợ bạn về:\n\n" +
                            "• Giá vé xe\n" +
                            "• Tuyến đường\n" +
                            "• Thông tin liên hệ\n" +
                            "• Chính sách hủy vé\n\n" +
                            "Bạn có thể hỏi cụ thể hơn được không?"
                )
            }
        }
    }

    private data class BotResponse(
        val message: String,
        val options: List<ChatOption>? = null
    )
}
