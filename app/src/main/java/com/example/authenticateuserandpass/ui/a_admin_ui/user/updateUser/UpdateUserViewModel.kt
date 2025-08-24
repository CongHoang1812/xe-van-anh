package com.example.authenticateuserandpass.ui.a_admin_ui.user.updateUser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.repository.user.UserRepositoryImpl
import com.example.authenticateuserandpass.ui.a_admin_ui.user.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UpdateUserViewModel(private val repository: UserRepositoryImpl) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUserUiState())
    val uiState: StateFlow<UpdateUserUiState> = _uiState

    fun updateUser(userId: String, updatedUser: User, originalEmail: String, originalPhone: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            // Validate input first
            if (!validateInput(updatedUser.name, updatedUser.email, updatedUser.phone)) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            // Check if email exists (only if email changed)
            if (updatedUser.email != originalEmail && repository.checkEmailExists(updatedUser.email)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Email đã tồn tại trong hệ thống"
                )
                return@launch
            }

            // Check if phone exists (only if phone changed)
            if (updatedUser.phone != originalPhone && repository.checkPhoneExists(updatedUser.phone)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Số điện thoại đã tồn tại trong hệ thống"
                )
                return@launch
            }

            // Update user if validation passes
            repository.updateUser(userId, updatedUser)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = "Cập nhật user thành công!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Có lỗi xảy ra khi cập nhật user"
                    )
                }
        }
    }

    private fun validateInput(name: String, email: String, phone: String): Boolean {
        when {
            name.isEmpty() -> {
                _uiState.value = _uiState.value.copy(error = "Vui lòng nhập tên")
                return false
            }
            name.length < 2 -> {
                _uiState.value = _uiState.value.copy(error = "Tên phải có ít nhất 2 ký tự")
                return false
            }
            email.isEmpty() -> {
                _uiState.value = _uiState.value.copy(error = "Vui lòng nhập email")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(error = "Email không đúng định dạng")
                return false
            }
            phone.isEmpty() -> {
                _uiState.value = _uiState.value.copy(error = "Vui lòng nhập số điện thoại")
                return false
            }
            !isValidPhoneNumber(phone) -> {
                _uiState.value = _uiState.value.copy(error = "Số điện thoại không hợp lệ (10-11 số)")
                return false
            }
        }
        return true
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = Regex("^(0[3|5|7|8|9])+([0-9]{8,9})$")
        return phoneRegex.matches(phone)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false, successMessage = null)
    }
}

class Factory(
    private val userRepository: UserRepositoryImpl
): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UpdateUserViewModel::class.java)){
            return UpdateUserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}


data class UpdateUserUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
)