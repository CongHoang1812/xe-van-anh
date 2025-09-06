package com.example.authenticateuserandpass.data.repository.otp


import com.example.authenticateuserandpass.data.api.ApiClient
import com.example.authenticateuserandpass.data.model.otp.OtpResponse
import retrofit2.Response

class OtpRepository {
    private val apiService = ApiClient.otpApiService

    suspend fun requestOtp(
        userId: String,
        apiKey: String,
        phone: String
    ): Response<OtpResponse> {
        return apiService.requestOtp(userId, apiKey, phone)
    }

    suspend fun verifyOtp(
        userId: String,
        apiKey: String,
        phone: String,
        otpCode: String
    ): Response<OtpResponse> {
        return apiService.verifyOtp(userId, apiKey, phone, otpCode)
    }
}
