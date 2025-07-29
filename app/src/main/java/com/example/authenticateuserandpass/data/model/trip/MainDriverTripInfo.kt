package com.example.authenticateuserandpass.data.model.trip

data class MainDriverTripInfo(
    val tripId: String,
    val routeName: String,
    val departureTime: String,
    val hoursLeft: Long,
    val passengerCount: Int,
    val origin: String,
    val destination: String
)
