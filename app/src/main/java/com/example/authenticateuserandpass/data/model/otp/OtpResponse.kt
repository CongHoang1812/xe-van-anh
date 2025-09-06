package com.example.authenticateuserandpass.data.model.otp

data class OtpResponse(
    val status: String,
    val message: String,
    val data: Any? = null
)

