package com.example.authenticateuserandpass.ui.payment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.ui.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity(), MenuProvider {
    private lateinit var binding: ActivityPaymentBinding
    private var ticketPrice   : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupView()

    }



    @SuppressLint("SetTextI18n")
    private fun setupView() {
        ticketPrice = intent.getStringExtra("ticket_price").toString()
        var totalPrice = intent.getStringExtra("total_price")
        var pickUpLocation = intent.getStringExtra("location_pickup")
        var dropOffLocation = intent.getStringExtra("location_dropoff")
        var origin = intent.getStringExtra("origin")
        var destination = intent.getStringExtra("destination")
        var departureTime = intent.getStringExtra("departureTime")
        var seatsSelected = intent.getStringExtra("seats_selected")
        var tripDate = intent.getStringExtra("tripDate")
        var selectedTrip = intent.getSerializableExtra("trip")
        var seatCount = intent.getStringExtra("seat_count")

        binding.tvOrigin.text = origin
        binding.tvDestination.text = destination
        binding.tvTripDate.text = tripDate
        binding.tvDepartureTime.text = departureTime
        binding.tvTripDate1.text = tripDate
        binding.textTimeDeparture.text = departureTime
        binding.editPickupTic.setText("Điểm đón: $pickUpLocation")
        binding.editDropoffTic.setText("Điểm trả: $dropOffLocation")
        binding.tvTotalPrice1.text = "$totalPrice"
        binding.tvPrice.text = "$ticketPrice.000"
        binding.textView25.text = seatsSelected
        binding.tvSeatCount1.text = seatCount

        binding.btnContinue.setOnClickListener {
            intent = Intent(this, ChoosePaymentActivity::class.java)
            intent.putExtra("total_price", binding.tvTotalPrice1.text)
            intent.putExtra("origin", binding.tvOrigin.text.toString())
            intent.putExtra("destination", binding.tvDestination.text.toString())
            intent.putExtra("tripDate", binding.tvTripDate.text.toString())
            intent.putExtra("departureTime", binding.tvDepartureTime.text.toString())
            intent.putExtra("location_pickup", pickUpLocation)
            intent.putExtra("location_dropoff", dropOffLocation)
            intent.putExtra("total_price", totalPrice)
            intent.putExtra("ticket_price", ticketPrice)
            intent.putExtra("trip", selectedTrip)
            intent.putExtra("seats_selected", seatsSelected)
            startActivity(intent)
        }


    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_menu3, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.navigate_home -> {
                var intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
    }
}