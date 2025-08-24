package com.example.authenticateuserandpass.data.repository.user

import com.example.authenticateuserandpass.ResultCallback
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.data.source.remote.RemoteUserDataSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID


class UserRepositoryImpl : UserRepository  {
    private val remoteUserDataSource = RemoteUserDataSource()
    private val firestore = FirebaseFirestore.getInstance()
    override suspend fun getAllUsers(callback: ResultCallback<Result<List<User>>>) {
        remoteUserDataSource.getAllUsers(callback)
    }

    override suspend fun getUserRole(
        userId: String,
        callback: ResultCallback<Result<User>>
    ) {
        remoteUserDataSource.getUserRole(userId, callback)
    }

    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val result = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            !result.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    suspend fun checkPhoneExists(phone: String): Boolean {
        return try {
            val result = firestore.collection("users")
                .whereEqualTo("phone", phone)
                .get()
                .await()
            !result.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    suspend fun createUser(user:User) : kotlin.Result<String> {
        return try {
            val userId = UUID.randomUUID().toString()
            val userWithId = user.copy(uid = userId)
            firestore.collection("users")
                .document(userId)
                .set(userWithId)
                .await()

            kotlin.Result.success("User created successfully")
        }catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
    suspend fun deleteUser(userId: String): kotlin.Result<String> {
        return try {
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()
            kotlin.Result.success("Xóa user thành công")
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
    suspend fun updateUser(userId: String, user: User): kotlin.Result<String> {
        return try {
            firestore.collection("users")
                .document(userId)
                .set(user)
                .await()
            kotlin.Result.success("User updated successfully")
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

}