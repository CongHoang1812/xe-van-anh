package com.example.authenticateuserandpass.data.model.trip

import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import java.io.Serializable

data class Trip(
    var id: String = "",
    var route_id : String = "",
    var bus_id : String = "",
    var departure_time : String = "",
    var trip_date : String = "",
    var ticket_price : String = "",
    var status : String = "",
    var main_driver_id : String = "",
    var duration  :String = "",
    var distance : String = "",
    var availableSeats: Int = 0,

    @Exclude var main_driver_name: String = "",
    @Exclude var main_driver_phone: String = ""

): Serializable