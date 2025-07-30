package com.example.authenticateuserandpass.data.firebaseModel

data class Passenger(
    var id: String = "",
    var booking_id: String = "",
    var user_id: String = "",
    var name: String = "",
    var phone: String = "",
    var email: String = "",
    var seat_id: String = "",
    var pickup_location: String = "",
    var dropoff_location: String = "",
    var note: String = "",
    var booking_status: String = ""
)