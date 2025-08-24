package com.example.authenticateuserandpass.ui.a_admin_ui.user.createNew

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.model.CreateUserUiState
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.repository.user.UserRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.Address

class CreateUserViewModel :ViewModel() {
    private val repository = UserRepositoryImpl()
    private val _uiState = MutableStateFlow(CreateUserUiState())
    val uiState: StateFlow<CreateUserUiState> = _uiState
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users


    // UserViewModel.kt
    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                val result = repository.deleteUser(user.uid)
                result.onSuccess { message ->
                    // Cập nhật danh sách users sau khi xóa thành công
                    val currentList = _users.value?.toMutableList() ?: mutableListOf()
                    currentList.removeIf { it.uid == user.uid }
                    _users.value = currentList

                    // Hiển thị thông báo thành công (có thể dùng callback hoặc LiveData khác)
                }.onFailure { exception ->
                    // Xử lý lỗi (có thể dùng callback hoặc LiveData khác)
                }
            } catch (e: Exception) {
                // Xử lý exception
            }
        }
    }


    fun createUser(name: String, email:String, phone: String, role: String, gender: String, address: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            // Validate input first
            if (!validateInput(name, email, phone)) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            // Check if email exists
            if (repository.checkEmailExists(email)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Email đã tồn tại trong hệ thống"
                )
                return@launch
            }
            // Check if phone exists
            if (repository.checkPhoneExists(phone)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Số điện thoại đã tồn tại trong hệ thống"
                )
                return@launch
            }
            // Create user
            val user  = User(
                name = name,
                email = email,
                phone = phone,
                role = role,
                gender = gender
                , address = address
            )
            repository.createUser(user).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Tạo user thành công!"
                )
            }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error =  "Có lỗi xảy ra khi tạo user"
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