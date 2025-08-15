package com.example.authenticateuserandpass.data.model.payment

data class Payment(
    var id  : String = "",
    var bookingId: String = "",
    var amount: String = "",
    var paymentMethod: String = "", // "ZaloPay" hoặc "Cash"
    var status: String = "", // "Đã thanh toán", "Chưa thanh toán", "Thất bại"
    var transactionId: String = "", // ID giao dịch từ ZaloPay (nếu có)
    var paidAt : String = "",
    var createdAt: String = ""
)
