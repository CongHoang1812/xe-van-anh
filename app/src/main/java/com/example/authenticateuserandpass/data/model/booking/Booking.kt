package com.example.authenticateuserandpass.data.model.booking

data class Booking(
    var id : String = "",
    var user_id :String= "",
    var trip_id : String = "",
    var seat_id: String = "",
    var shuttle_driver_id : String = "",
    var status : String = "",
    var pickup_location : String= "",
    var dropoff_location : String = "",
    var note : String = "",
    var book_at : String = "",
)