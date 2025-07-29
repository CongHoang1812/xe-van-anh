package com.example.authenticateuserandpass.ui.findticket

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.databinding.ActivityFindTicketBinding
import com.example.authenticateuserandpass.ui.chooseSeat.ChooseSeatActivity
import com.example.authenticateuserandpass.ui.home.HomeFragment
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_DEPARTURE
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_DEPARTURE_DATE
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_DESTINATION
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_RETURN_DATE
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FindTicketActivity : AppCompatActivity(){
    private lateinit var binding: ActivityFindTicketBinding
    private lateinit var viewModel: FindTicketViewModel
    private lateinit var ticketAdapter: TicketAdapter
    private val calendar = Calendar.getInstance()

    private var origin: String? = null
    private var destination: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFindTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewModel()
        setupView()
        setupListener()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            FindTicketViewModel.Factory(TripRepositoryImpl())
        )[FindTicketViewModel::class.java]

        viewModel.tickets.observe(this) { tickets ->
            ticketAdapter.updateTickets(tickets)
            Log.d("FindTicketActivity", "Số lượng vé: ${tickets.size}")
            binding.progressBar3.visibility = View.GONE
        }
    }

    private fun setupView() {
        // Nhận dữ liệu từ Fragment
        origin = intent.getStringExtra("origin") ?: intent.getStringExtra(HomeFragment.EDIT_DEPARTURE)
        destination = intent.getStringExtra("destination") ?: intent.getStringExtra(HomeFragment.EDIT_DESTINATION)
        val dateString = intent.getStringExtra("trip_date") ?: intent.getStringExtra(HomeFragment.EDIT_DEPARTURE_DATE)



        binding.tvOrigin.text = origin
        binding.tvDestination.text = destination

        // Cập nhật ngày hiện tại
        if (!dateString.isNullOrEmpty()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
            val date = sdf.parse(dateString)
            date?.let { calendar.time = it }
        }

        updateDisplayedDate()
        searchTrips()

        ticketAdapter = TicketAdapter(object : TicketAdapter.OnTicketClickListener {
            override fun onClick(ticket: Trip, index: Int) {
                // TODO: Mở activity chọn ghế hoặc xác nhận vé
                val intent = Intent(this@FindTicketActivity, ChooseSeatActivity::class.java)
                intent.putExtra("time_departure", ticket.departure_time)
                val departure = binding.tvOrigin.text.toString()
                val destination = binding.tvDestination.text.toString()
                intent.putExtra("ticket_price", ticket.ticket_price)
                intent.putExtra("trip", ticket)
                intent.putExtra(EDIT_DEPARTURE,departure)
                intent.putExtra(EDIT_DESTINATION,destination)
                intent.putExtra("date_departure",ticket.trip_date)
                startActivity(intent)
            }
        })

        binding.rvTempTicket.adapter = ticketAdapter
    }

    private fun setupListener() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnPrev.setOnClickListener {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            updateDisplayedDate()
            ticketAdapter.updateTickets(emptyList())
            binding.progressBar3.visibility = View.VISIBLE
            searchTrips()
        }

        binding.btnNext.setOnClickListener {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            updateDisplayedDate()
            ticketAdapter.updateTickets(emptyList())
            binding.progressBar3.visibility = View.VISIBLE
            searchTrips()
        }

        binding.txtSelectedDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                updateDisplayedDate()
                searchTrips()
            }, year, month, day)

            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }
    }

    private fun updateDisplayedDate() {
        binding.txtSelectedDate.text = getFormattedDate(calendar)
    }

    private fun getFormattedDate(cal: Calendar): String {
        val sdf = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN"))
        return sdf.format(cal.time).replaceFirstChar { it.uppercaseChar() }
    }

    private fun getTripDateForQuery(): String {
        // Format đúng dạng lưu trong Firestore (ví dụ: 2025-07-09)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun searchTrips() {
        val tripDate = getTripDateForQuery()

        if (!origin.isNullOrEmpty() && !destination.isNullOrEmpty()) {
            viewModel.loadTrips(
                origin = origin!!,
                destination = destination!!,
                tripDate = tripDate
            )
        }
    }
}
