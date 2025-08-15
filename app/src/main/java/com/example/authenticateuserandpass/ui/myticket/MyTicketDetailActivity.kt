package com.example.authenticateuserandpass.ui.myticket

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.UserTicket
import com.example.authenticateuserandpass.databinding.ActivityMyTicketDetailBinding
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class MyTicketDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyTicketDetailBinding
    private lateinit var ticket: UserTicket
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityMyTicketDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        ticket = intent.getParcelableExtra("TICKET_DATA") ?: return
        Log.d("MyTicketDetailActivity", "Received ticket: $ticket")
        val bookingCode = ticket.ticketCode

        generateQRCode(bookingCode)
        binding.tvTicketCodeMyTicket.text = ticket.ticketCode
        binding.paymentStatusMyTicket.text = ticket.paymentStatus
        binding.tvDepartureTimeMyTicketDetail.text = ticket.departureTime
        if (ticket.paymentStatus == "Chưa thanh toán") {
            binding.paymentStatusMyTicket.setTextColor(resources.getColor(com.google.android.material.R.color.design_default_color_error, null))
        } else {
            binding.paymentStatusMyTicket.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700))
        }
        binding.tvTripDateMyTicketDetail.text = ticket.departureDate
        binding.editDropoffTic.setText("Điểm trả: ${ticket.dropoffPoint}")
        binding.editPickupTic.setText( "Điểm đón:  ${ticket.pickupPoint}")
        binding.tvSeatNumberMyTicketDetail.text = ticket.seatNumbers
        binding.tvOriginMyTicket.text = ticket.routeName
        binding.tvOriginMyTicket.text = ticket.origin
        binding.tvDestinationMyTicket.text = ticket.destination

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViews() {

    }

    private fun generateQRCode(data: String) {
        try {
            val size = 800 // pixels — chỉnh tuỳ ý (kích thước cao hơn -> dễ quét)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)
            binding.imgQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}