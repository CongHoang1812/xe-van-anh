package com.example.authenticateuserandpass.data.model.otp

data class VerifyOtpRequest(
    val user_id: String,
    val api_key: String,
    val recipient_phone: String,
    val otp_code: String
)
