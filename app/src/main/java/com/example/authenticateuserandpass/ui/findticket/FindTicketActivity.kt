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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.example.authenticateuserandpass.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.TicketFilter
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.databinding.ActivityFindTicketBinding
import com.example.authenticateuserandpass.ui.chooseSeat.ChooseSeatActivity
import com.example.authenticateuserandpass.ui.findticket.filterTicket.FilterTicketActivity
import com.example.authenticateuserandpass.ui.home.HomeFragment
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_DEPARTURE
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_DEPARTURE_DATE
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_DESTINATION
import com.example.authenticateuserandpass.ui.home.HomeFragment.Companion.EDIT_RETURN_DATE
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.div
import kotlin.ranges.rangeTo
import kotlin.text.get

class FindTicketActivity : AppCompatActivity(), MenuProvider{
    private lateinit var binding: ActivityFindTicketBinding
    private lateinit var viewModel: FindTicketViewModel
    private lateinit var ticketAdapter: TicketAdapter
    private val calendar = Calendar.getInstance()
    private var currentFilter: TicketFilter? = null
    private val db = FirebaseFirestore.getInstance()

    private var origin: String? = null
    private var destination: String? = null

    companion object {
        private const val FILTER_REQUEST_CODE = 100
    }

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
        val menuHost: MenuHost = this
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setupViewModel()
        setupView()
        setupListener()
        handleFilterData()
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
            Log.d("FindTicketActivity", viewModel.tickets.toString())
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




    private fun handleFilterData() {
        val applyFilter = intent.getBooleanExtra("APPLY_FILTER", false)

        if (applyFilter) {
            currentFilter = TicketFilter(
                timeStart = intent.getIntExtra("TIME_START", 150),
                timeEnd = intent.getIntExtra("TIME_END", 1200),
                seatsMin = intent.getIntExtra("SEATS_MIN", 2),
                seatsMax = intent.getIntExtra("SEATS_MAX", 24),
                priceMin = intent.getLongExtra("PRICE_MIN", 100000),
                priceMax = intent.getLongExtra("PRICE_MAX", 800000),
                vehicleLimousine = intent.getBooleanExtra("VEHICLE_LIMOUSINE", false),
                vehicleLimousine2 = intent.getBooleanExtra("VEHICLE_LIMOUSINE2", false)
            )

            // Áp dụng filter và tìm kiếm
            applyFilterAndSearch()
        }
    }

    private fun applyFilterAndSearch() {
        currentFilter?.let { filter ->
            // Hiển thị thông tin filter đang áp dụng
            showFilterInfo(filter)

            // Thực hiện tìm kiếm với filter
            searchTripsWithFilter(filter)
        }
    }

    private fun showFilterInfo(filter: TicketFilter) {
        // Tạo một TextView hoặc Chip để hiển thị filter info
        val filterInfo = "Lọc: ${filter.getTimeStartFormatted()}-${filter.getTimeEndFormatted()}, " +
                "${filter.seatsMin}-${filter.seatsMax} ghế, ${filter.getPriceRange()}"

        // Hiển thị trong UI (có thể dùng Snackbar hoặc TextView)
        // Snackbar.make(binding.root, filterInfo, Snackbar.LENGTH_LONG).show()
    }

    private fun searchTripsWithFilter(filter: TicketFilter) {
        binding.progressBar3.visibility = View.VISIBLE

        // Lấy data từ viewModel thay vì query lại DB
        val allTrips = viewModel.tickets.value ?: emptyList()
        val filteredTrips = mutableListOf<Trip>()
        var processedCount = 0
        val totalCount = allTrips.size

        if (totalCount == 0) {
            updateTripsDisplay(emptyList())
            return
        }

        for (trip in allTrips) {
            // Lọc theo giá vé
            if (trip.ticket_price.toInt() in (filter.priceMin / 1000)..(filter.priceMax / 1000)) {

                // Lọc theo thời gian
                val departureMinutes = timeToMinutes(trip.departure_time)
                if (departureMinutes in filter.timeStart..filter.timeEnd) {

                    // Lọc theo số ghế trống
                    checkAvailableSeats(trip) { availableSeats ->
                        processedCount++

                        if (availableSeats in filter.seatsMin..filter.seatsMax) {
                            filteredTrips.add(trip)
                        }

                        // Cập nhật UI khi đã xử lý xong tất cả
                        if (processedCount == totalCount) {
                            updateTripsDisplay(filteredTrips)
                        }
                    }
                } else {
                    processedCount++
                    if (processedCount == totalCount) {
                        updateTripsDisplay(filteredTrips)
                    }
                }
            } else {
                processedCount++
                if (processedCount == totalCount) {
                    updateTripsDisplay(filteredTrips)
                }
            }
        }
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return if (parts.size == 2) {
            parts[0].toInt() * 60 + parts[1].toInt()
        } else 0
    }

    private fun checkAvailableSeats(trip: Trip, callback: (Int) -> Unit) {
        // Logic để đếm số ghế trống
        // Giả sử có 24 ghế total, cần trừ đi số ghế đã đặt
        val totalSeats = 24

        db.collection("bookings")
            .whereEqualTo("trip_id", trip.id)
            //.whereEqualTo("status", "Chưa đi")
            .get()
            .addOnSuccessListener { bookings ->
                var bookedSeats = 0
                for (booking in bookings) {
                    val seatNumbers = booking.getString("seat_id") ?: ""
                    if (seatNumbers.isNotEmpty()) {
                        bookedSeats += seatNumbers.split(",").size
                    }
                }
                val availableSeats = totalSeats - bookedSeats
                callback(availableSeats)
            }
    }


    private fun updateTripsDisplay(trips: List<Trip>) {
        runOnUiThread {
            ticketAdapter.updateTickets(trips)
            binding.progressBar3.visibility = View.GONE

            // Hiển thị thông báo nếu không có kết quả
            if (trips.isEmpty()) {
                // Có thể thêm TextView để hiển thị "Không tìm thấy vé phù hợp"
                Log.d("FindTicketActivity", "Không tìm thấy vé phù hợp với bộ lọc")
            } else {
                Log.d("FindTicketActivity", "Tìm thấy ${trips.size} vé phù hợp")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            currentFilter = TicketFilter(
                timeStart = data.getIntExtra("TIME_START", 150),
                timeEnd = data.getIntExtra("TIME_END", 1200),
                seatsMin = data.getIntExtra("SEATS_MIN", 2),
                seatsMax = data.getIntExtra("SEATS_MAX", 24),
                priceMin = data.getLongExtra("PRICE_MIN", 100000),
                priceMax = data.getLongExtra("PRICE_MAX", 800000),
                vehicleLimousine = data.getBooleanExtra("VEHICLE_LIMOUSINE", false),
                vehicleLimousine2 = data.getBooleanExtra("VEHICLE_LIMOUSINE2", false)
            )

            // Áp dụng filter và tìm kiếm
            searchTripsWithFilter(currentFilter!!)
        }
    }







    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_menu2, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.ic_fillter -> {
                val intent = Intent(this, FilterTicketActivity::class.java)
                startActivityForResult(intent, FILTER_REQUEST_CODE)
                true
            }
            else -> false
        }
    }












}
