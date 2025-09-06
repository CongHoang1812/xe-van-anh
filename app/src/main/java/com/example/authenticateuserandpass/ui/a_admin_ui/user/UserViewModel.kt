package com.example.authenticateuserandpass.ui.a_admin_ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.repository.user.UserRepositoryImpl
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.login.LoginViewModel
import kotlinx.coroutines.launch
import okhttp3.Dispatcher

class UserViewModel(
    private val userRepository: UserRepositoryImpl
) : ViewModel()  {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users




    init {
        getAllUsers()
    }

    fun getAllUsers(){
        viewModelScope.launch {
            userRepository.getAllUsers(object : ResultCallback<Result<List<User>>> {
                override fun onResult(result: Result<List<User>>) {
                    if(result is Result.Success){
                        _users.postValue(result.data)
                    }
                }

            })
        }
    }
    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                val result = userRepository.deleteUser(user.uid)
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
}

class Factory(
    private val userRepository: UserRepositoryImpl
): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(UserViewModel::class.java)){
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}
