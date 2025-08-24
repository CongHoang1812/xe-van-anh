package com.example.authenticateuserandpass.data.model.route

import com.google.firebase.Timestamp

data class Route(
    val id : String = "",
    val origin : String = "",
    val destination : String = "",
    val distance : String = "",
    val duration : String = "",
    val  tripsPerDay: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)
