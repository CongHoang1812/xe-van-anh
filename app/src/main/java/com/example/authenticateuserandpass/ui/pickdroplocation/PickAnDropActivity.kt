package com.example.authenticateuserandpass.ui.pickdroplocation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.ui.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.placeSuggestion.PlaceSuggestion
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.databinding.ActivityPickAnDropBinding
import com.example.authenticateuserandpass.ui.contactinfomation.ContactInformationActivity
import com.example.authenticateuserandpass.ui.home.HomeFragment
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class PickAnDropActivity : AppCompatActivity(), MenuProvider {
    private lateinit var binding: ActivityPickAnDropBinding
    private lateinit var adapter: PlacesAdapter
    private lateinit var adapter1: PlacesAdapter
    private lateinit var placesClient: PlacesClient
    private var userJustSelected = false

    private var ticketPrice   : String = ""
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPickAnDropBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val menuHost: MenuHost = this
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
        setSupportActionBar(binding.toolbarHome)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyAyqExml_1CLWxc4g3pFIVMWxFYf3qBP3o") // thay bằng API key của bạn
        }
        placesClient = Places.createClient(this)

        setAutoCompleteFragment()
        setupView()
        setupListener()
    }

    private fun setupListener() {
        binding.tvPickUp.setOnClickListener {
            var pickup = binding.tvPickUp.text.toString()
            binding.etSearchPickUp.setText(pickup)
        }
        binding.tvDropOff.setOnClickListener {
            var dropoff = binding.tvDropOff.text.toString()
            binding.etSearchDropOff.setText(dropoff)
        }
        binding.toolbarHome.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupView() {
        ticketPrice = intent.getStringExtra("ticket_price").toString()
        var totalPrice = intent.getStringExtra("total_price")
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
        binding.tvPickUp.text = origin
        binding.tvDropOff.text = destination
        binding.btnContinue.setOnClickListener {

            var intent = Intent(this, ContactInformationActivity::class.java)
            //intent.putExtra("trip", selectedTrip)
            intent.putExtra("origin", binding.tvOrigin.text.toString())
            intent.putExtra("destination", binding.tvDestination.text.toString())
            intent.putExtra("tripDate", binding.tvTripDate.text.toString())
            intent.putExtra("departureTime", binding.tvDepartureTime.text.toString())
            intent.putExtra("location_pickup", binding.etSearchPickUp.text.toString())
            intent.putExtra("location_dropoff", binding.etSearchDropOff.text.toString())
            intent.putExtra("total_price", totalPrice)
            intent.putExtra("ticket_price", ticketPrice)
            intent.putExtra("seat_count", seatCount)
            intent.putExtra("seats_selected", selectedSeats)
            intent.putExtra("trip", selectedTrip)
            startActivity(intent)
        }
        binding.toolbarHome.setOnClickListener {
            finish()
        }
    }

    fun setAutoCompleteFragment(){

        adapter = PlacesAdapter { place ->
            binding.etSearchPickUp.setText(place.address)
            userJustSelected = true
            adapter.submitList(emptyList())
            binding.rvSuggestions.visibility = View.GONE


        }

        adapter1 = PlacesAdapter { place ->
            binding.etSearchDropOff.setText(place.address)
            userJustSelected = true
            adapter1.submitList(emptyList())
            binding.rvSuggestions1.visibility = View.GONE


        }



        binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions.adapter = adapter

        binding.rvSuggestions1.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions1.adapter = adapter1

        binding.etSearchPickUp.doOnTextChanged { text, _, _, _ ->
            if (userJustSelected) {
                userJustSelected = false // reset cờ
                return@doOnTextChanged
            }
            val query = text.toString()
            if (query.length >= 3) {
                searchPlaces(query)
            } else {
                binding.rvSuggestions.visibility = View.GONE
            }
        }

        binding.etSearchDropOff.doOnTextChanged { text, _, _, _ ->
            if (userJustSelected) {
                userJustSelected = false // reset cờ
                return@doOnTextChanged
            }
            val query = text.toString()
            if (query.length >= 3) {
                searchPlaces1(query)
            } else {
                binding.rvSuggestions1.visibility = View.GONE
            }
        }

//        binding.etSearch.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {}
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                val query = s.toString()
//                if (query.length >= 3) {
//                    searchPlaces(query)
//                } else {
//                    binding.rvSuggestions.visibility = View.GONE
//                }
//            }
//        })
    }
    private fun searchPlaces(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setCountries(listOf("VN"))
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions.map {
                    val fullText = it.getFullText(null).toString()
                    val cleaned = fullText.removeSuffix(", Việt Nam")
                    PlaceSuggestion(cleaned)
                }
                adapter.submitList(predictions)
                findViewById<RecyclerView>(R.id.rvSuggestions).visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Log.e("PLACE", "Error: ${it.message}")
            }
    }
    private fun searchPlaces1(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setCountries(listOf("VN"))
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions.map {
                    val fullText = it.getFullText(null).toString()
                    val cleaned = fullText.removeSuffix(", Việt Nam")
                    PlaceSuggestion(cleaned)
                }
                adapter1.submitList(predictions)
                findViewById<RecyclerView>(R.id.rvSuggestions_1).visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Log.e("PLACE", "Error: ${it.message}")
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