package com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.repository.user.UserRepositoryImpl
import kotlinx.coroutines.launch
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.ui.a_admin_ui.user.UserViewModel


class LoginViewModel(
    private val userRepository: UserRepositoryImpl
): ViewModel() {
    private val _user = MutableLiveData<Result<User>>()
    val user: LiveData<Result<User>> get() = _user

    fun fetchUserRole(userId: String) {
        viewModelScope.launch {
            val callback = object : ResultCallback<Result<User>> {
                override fun onResult(result: Result<User>) {
                    if (result is Result.Success) {
                        _user.postValue(result)
                    }
                }
            }
            userRepository.getUserRole(userId, callback)
        }
    }
}
class Factory(
    private val userRepository: UserRepositoryImpl
): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(LoginViewModel::class.java)){
            return LoginViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}

