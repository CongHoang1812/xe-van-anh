package com.example.authenticateuserandpass.data.api


import com.example.authenticateuserandpass.data.model.otp.OtpResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OtpApiService {

    @FormUrlEncoded
    @POST("request_otp.php")
    suspend fun requestOtp(
        @Field("user_id") userId: String,
        @Field("api_key") apiKey: String,
        @Field("recipient_phone") recipientPhone: String
    ): Response<OtpResponse>

    @FormUrlEncoded
    @POST("verify_otp.php")
    suspend fun verifyOtp(
        @Field("user_id") userId: String,
        @Field("api_key") apiKey: String,
        @Field("recipient_phone") recipientPhone: String,
        @Field("otp_code") otpCode: String
    ): Response<OtpResponse>
}
