package com.example.authenticateuserandpass.data.source.remote

import com.example.authenticateuserandpass.ResultCallback
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.source.Result

interface UserDataSource {
    suspend fun getAllUsers(callback: ResultCallback<Result<List<User>>>)
    suspend fun getUserRole(userId: String, callback: ResultCallback<Result<User>>)
}