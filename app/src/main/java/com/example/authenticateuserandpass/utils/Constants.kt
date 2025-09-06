package com.example.authenticateuserandpass.utils

object Constants {
    const val BASE_URL = "https://otp.goixe247.com/"

    // Các hằng số khác có thể sử dụng
    const val EXTRA_USER_ID = "user_id"
    const val EXTRA_API_KEY = "api_key"
    const val EXTRA_PHONE = "phone"

    // Response status
    const val STATUS_SUCCESS = "success"
    const val STATUS_ERROR = "error"

    // Validation
    const val MIN_OTP_LENGTH = 4
    const val MAX_OTP_LENGTH = 6
    const val PHONE_REGEX = "^[0-9]{10,11}$"
}
