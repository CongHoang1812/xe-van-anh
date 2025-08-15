package com.example.authenticateuserandpass.data.model.feedback

import com.google.firebase.Timestamp

data class Feedback(
    var id: String,
    var user_id: String,
    var trip_id: String,
    var rating: Float,
    val selectedReasons: List<String> = emptyList(),
    var create_at: Timestamp = Timestamp.now()
)