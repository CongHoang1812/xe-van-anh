package com.example.authenticateuserandpass.data.model

data class TicketFilter(
    val timeStart: Int = 150, // 02:30 in minutes
    val timeEnd: Int = 1200,  // 20:00 in minutes
    val seatsMin: Int = 2,
    val seatsMax: Int = 24,
    val priceMin: Long = 100000,
    val priceMax: Long = 800000,
    val vehicleLimousine: Boolean = false,
    val vehicleLimousine2: Boolean = false
) {
    fun getTimeStartFormatted(): String {
        val hours = timeStart / 60
        val mins = timeStart % 60
        return String.format("%02d:%02d", hours, mins)
    }

    fun getTimeEndFormatted(): String {
        val hours = timeEnd / 60
        val mins = timeEnd % 60
        return String.format("%02d:%02d", hours, mins)
    }

    fun getPriceRange(): String {
        val formatter = java.text.DecimalFormat("#,###")
        return "${formatter.format(priceMin)}đ - ${formatter.format(priceMax)}đ"
    }
}