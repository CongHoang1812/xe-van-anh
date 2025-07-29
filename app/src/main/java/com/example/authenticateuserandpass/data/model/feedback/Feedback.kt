package com.example.authenticateuserandpass.data.model.feedback

data class Feedback(
    var id: String,
    var user_id: String,
    var trip_id: String,
    var rating: Int,
    var comment: String,
    var cteate_at : String
)