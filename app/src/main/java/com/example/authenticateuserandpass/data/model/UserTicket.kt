package com.example.authenticateuserandpass.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserTicket(
    val ticketCode: String = "",
    val routeName: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureDate: String = "",
    val departureTime: String = "",
    val price: String = "",
    val paymentStatus: String = "",
    val tripStatus: String = "",
    val seatNumbers: String = "",
    val busType: String = "",
    val bookingId: String = "",
    val tripId: String = "",
    val pickupPoint: String = "",
    val dropoffPoint: String = "",
    val mainDriverName: String = "",
    val mainDriverPhone: String = "",
    val mainBusLicensePlate: String = "",
    val mainBusType: String = "",
    val hasTransfer: Boolean = false
) : Parcelable