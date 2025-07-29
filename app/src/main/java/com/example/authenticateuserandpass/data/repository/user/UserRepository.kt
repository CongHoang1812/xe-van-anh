package com.example.authenticateuserandpass.data.repository.user

import com.example.authenticateuserandpass.ResultCallback
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.source.Result

interface UserRepository {
    suspend fun getUserRole(userId: String, callback: ResultCallback<Result<User>>)

}