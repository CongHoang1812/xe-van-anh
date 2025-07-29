package com.example.authenticateuserandpass.data.model.payment

data class Payment(
    var id  : String = "",
    var bookingId: String = "",
    var amount: String = "",
    var status: String = "",
    var paidAt : String = ""
)
