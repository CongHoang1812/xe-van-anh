package com.example.authenticateuserandpass.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.repository.otp.OtpRepository
import kotlinx.coroutines.launch

class OtpViewModel : ViewModel() {
    private val repository = OtpRepository()

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData cho kết quả request OTP
    private val _otpRequestResult = MutableLiveData<String>()
    val otpRequestResult: LiveData<String> = _otpRequestResult

    // LiveData cho kết quả verify OTP
    private val _otpVerifyResult = MutableLiveData<String>()
    val otpVerifyResult: LiveData<String> = _otpVerifyResult

    // LiveData cho việc chuyển màn hình
    private val _navigateToVerify = MutableLiveData<Boolean>()
    val navigateToVerify: LiveData<Boolean> = _navigateToVerify

    // LiveData cho thành công đăng nhập
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    fun requestOtp(userId: String, apiKey: String, phone: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.requestOtp(userId, apiKey, phone)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        _otpRequestResult.value = "OTP đã được gửi thành công!"
                        _navigateToVerify.value = true
                    } else {
                        _otpRequestResult.value = body?.message ?: "Có lỗi xảy ra khi gửi OTP"
                    }
                } else {
                    _otpRequestResult.value = "Lỗi kết nối: ${response.code()}"
                }
            } catch (e: Exception) {
                _otpRequestResult.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyOtp(userId: String, apiKey: String, otpCode: String, phone: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.verifyOtp(userId, apiKey, phone, otpCode)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        _otpVerifyResult.value = "Xác thực thành công!"
                        _loginSuccess.value = true
                    } else {
                        _otpVerifyResult.value = body?.message ?: "Mã OTP không đúng"
                    }
                } else {
                    _otpVerifyResult.value = "Lỗi kết nối: ${response.code()}"
                }
            } catch (e: Exception) {
                _otpVerifyResult.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // LiveData cho kết quả gửi lại OTP
    private val _otpSendResult = MutableLiveData<String>()
    val otpSendResult: LiveData<String> = _otpSendResult

    fun sendOtp(apiKey: String, phone: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.requestOtp("", apiKey, phone)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        _otpSendResult.value = "OTP đã được gửi lại thành công!"
                    } else {
                        _otpSendResult.value = body?.message ?: "Có lỗi xảy ra khi gửi OTP"
                    }
                } else {
                    _otpSendResult.value = "Lỗi kết nối: ${response.code()}"
                }
            } catch (e: Exception) {
                _otpSendResult.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNavigateComplete() {
        _navigateToVerify.value = false
    }
}
