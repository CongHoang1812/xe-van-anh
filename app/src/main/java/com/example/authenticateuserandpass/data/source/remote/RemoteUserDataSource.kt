package com.example.authenticateuserandpass.data.source.remote

import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.user.User
import com.google.firebase.firestore.FirebaseFirestore
import com.example.authenticateuserandpass.data.source.Result
import kotlin.text.get

class RemoteUserDataSource() : UserDataSource{
    private val firestore = FirebaseFirestore.getInstance()
    override suspend fun getAllUsers(callback: ResultCallback<Result<List<User>>>) {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.mapNotNull { it.toObject(User::class.java) }
                callback.onResult(Result.Success(users))
            }
            .addOnFailureListener { exception ->
                callback.onResult(Result.Error(exception))
            }
    }


    override suspend fun getUserRole(
        userId: String,
        callback: ResultCallback<Result<User>>
    ) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if(document != null && document.exists()){
                    try{
                        val user = document.toObject(User::class.java)
                        if (user != null){
                            callback.onResult(Result.Success(user))
                        }else{
                            callback.onResult(Result.Error(Exception("User data is null")))
                        }
                    }catch (e : Exception){
                        callback.onResult(Result.Error(e))
                    }
                }else{
                    callback.onResult(Result.Error(Exception("User not found")))
                }
            }
            .addOnFailureListener { exception ->
                callback.onResult(Result.Error(exception))
            }
    }

    override suspend fun getAllMainDriver(callback: ResultCallback<Result<List<User>>>) {
        firestore.collection("users")
            .whereEqualTo("role", "main_driver")
            .get()
            .addOnSuccessListener { documents ->
                val mainDrivers = documents.mapNotNull { it.toObject(User::class.java) }
                callback.onResult(Result.Success(mainDrivers))
            }
            .addOnFailureListener { exception ->
                callback.onResult(Result.Error(exception))
            }
    }
    override suspend fun getAllShuttleDriver(callback: ResultCallback<Result<List<User>>>) {
        firestore.collection("users")
            .whereEqualTo("role", "shuttle_driver")
            .get()
            .addOnSuccessListener { documents ->
                val shuttleDrivers = documents.mapNotNull { it.toObject(User::class.java) }
                callback.onResult(Result.Success(shuttleDrivers))
            }
            .addOnFailureListener { exception ->
                callback.onResult(Result.Error(exception))
            }
    }
}
