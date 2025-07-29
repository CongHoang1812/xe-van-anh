package com.example.authenticateuserandpass.data.model.user
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


data class User(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var birthDate: String = "",
    var gender: String = "",
    var address: String = "",
    var avatarUrl: String = "",
    var role: String = ""
)


