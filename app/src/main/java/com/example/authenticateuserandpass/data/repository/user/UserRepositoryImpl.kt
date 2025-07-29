package com.example.authenticateuserandpass.data.repository.user

import com.example.authenticateuserandpass.ResultCallback
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.data.source.remote.RemoteUserDataSource

class UserRepositoryImpl : UserRepository  {
    private val remoteUserDataSource = RemoteUserDataSource()
    override suspend fun getUserRole(
        userId: String,
        callback: ResultCallback<Result<User>>
    ) {
        remoteUserDataSource.getUserRole(userId, callback)
    }

}