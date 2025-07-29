package com.example.authenticateuserandpass.ui.contactinfomation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.databinding.ActivityContactInformationBinding
import com.example.authenticateuserandpass.ui.home.HomeFragment
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_DEPARTURE
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_DESTINATION
import com.example.authenticateuserandpass.ui.payment.PaymentActivity

class ContactInformationActivity : AppCompatActivity(), MenuProvider {
    private lateinit var binding: ActivityContactInformationBinding
    private var ticketPrice   : String = ""
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityContactInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupView()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupView() {
        ticketPrice = intent.getStringExtra("ticket_price").toString()
        var totalPrice = intent.getStringExtra("total_price")
        var pickUpLocation = intent.getStringExtra("location_pickup")
        var dropOffLocation = intent.getStringExtra("location_dropoff")
        var origin = intent.getStringExtra("origin")
        var destination = intent.getStringExtra("destination")
        var departureTime = intent.getStringExtra("departureTime")
        var tripDate = intent.getStringExtra("tripDate")
        var selectedSeats = intent.getStringExtra("seats_selected")
        var seatCount = intent.getStringExtra("seat_count")
        var selectedTrip = intent.getSerializableExtra("trip", Trip::class.java)!!
        binding.tvOrigin.text = origin
        binding.tvDestination.text = destination
        binding.tvTripDate.text = tripDate
        binding.tvDepartureTime.text = departureTime
        binding.btnContinue.setOnClickListener {
            var intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("trip", selectedTrip)
            intent.putExtra("origin", binding.tvOrigin.text.toString())
            intent.putExtra("destination", binding.tvDestination.text.toString())
            intent.putExtra("tripDate", binding.tvTripDate.text.toString())
            intent.putExtra("departureTime", binding.tvDepartureTime.text.toString())
            intent.putExtra("location_pickup", pickUpLocation)
            intent.putExtra("location_dropoff", dropOffLocation)
            intent.putExtra("total_price", totalPrice)
            intent.putExtra("ticket_price", ticketPrice)
            intent.putExtra("seat_count", seatCount)
            intent.putExtra("seats_selected", selectedSeats)
            startActivity(intent)
        }
        binding.toolbarHome.setOnClickListener {
            finish()
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