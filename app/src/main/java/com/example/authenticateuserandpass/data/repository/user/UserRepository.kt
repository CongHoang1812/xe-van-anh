package com.example.authenticateuserandpass.data.repository.user

import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.source.Result

interface UserRepository {
    suspend fun getAllUsers(callback: ResultCallback<Result<List<User>>>)
    suspend fun getUserRole(userId: String, callback: ResultCallback<Result<User>>)
    suspend fun getAllMainDriver(callback: ResultCallback<Result<List<User>>>)
    suspend fun getAllShuttleDriver(callback: ResultCallback<Result<List<User>>>)
}