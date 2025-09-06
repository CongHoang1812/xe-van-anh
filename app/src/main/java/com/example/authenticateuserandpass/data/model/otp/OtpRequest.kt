package com.example.authenticateuserandpass.data.model.otp

data class OtpRequest(
    val user_id: String,
    val api_key: String,
    val recipient_phone: String
)
