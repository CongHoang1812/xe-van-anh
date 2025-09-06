package com.example.authenticateuserandpass.ui.a_admin_ui.trip

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.repository.route.RouteRepository
import com.example.authenticateuserandpass.databinding.ActivitySearchTripBinding
import com.example.authenticateuserandpass.ui.dialog.BottomSheetLocationFragment
import com.example.authenticateuserandpass.ui.dialog.OnLocationSelectedListener
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.toString

class SearchTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchTripBinding
    private val calendar = Calendar.getInstance()
    // Giả sử bạn có một RouteRepository để truy xuất dữ liệu tuyến đường
    private val routeRepository = RouteRepository()
    private var selectedDeparture = ""
    private var selectedDestination = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupClickListeners()
    }
    private fun setupClickListeners() {
        // Departure selection - giống như trong HomeFragment
        binding.tvDeparture.setOnClickListener {
            android.util.Log.d("SearchTrip", "Departure card clicked")
            val dialog = BottomSheetLocationFragment()
            dialog.listener = object : OnLocationSelectedListener {
                override fun onLocationSelected(location: String) {
                    android.util.Log.d("SearchTrip", "Location selected: $location")
                    selectedDeparture = location
                    binding.tvDeparture.text = location
                    binding.tvDeparture.setTextColor(ContextCompat.getColor(this@SearchTripActivity, R.color.text_primary))
                }
            }
            dialog.show(supportFragmentManager, "ChonDiemDialog")
        }

        // Destination selection - giống như trong HomeFragment
        binding.tvDestination.setOnClickListener {
            val dialog = BottomSheetLocationFragment()
            dialog.listener = object : OnLocationSelectedListener {
                override fun onLocationSelected(location: String) {
                    selectedDestination = location
                    binding.tvDestination.text = location
                    binding.tvDestination.setTextColor(ContextCompat.getColor(this@SearchTripActivity, R.color.text_primary))
                }
            }
            dialog.show(supportFragmentManager, "ChonDiemDialog")
        }

        // Date selection
        binding.tvDate.setOnClickListener {
            showDatePicker()
        }

        // Search button
        binding.btnSearch.setOnClickListener {
            performSearch()
        }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
                binding.tvDate.text = selectedDate
                binding.tvDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            },
            year, month, day
        )
        // Không cho chọn ngày trong quá khứ
        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun performSearch() {
        val departure = selectedDeparture
        val destination = selectedDestination
        val date = binding.tvDate.text.toString()

        if (departure.isNotEmpty() && destination.isNotEmpty() &&
            !date.equals("Chọn ngày khởi hành")) {

            // Tìm route_id từ database trước khi chuyển activity
            findRouteId(departure, destination) { routeId ->
                if (routeId != null) {
                    val intent = Intent(this, TripManagementActivity::class.java).apply {
                        putExtra("departure", departure)
                        putExtra("destination", destination)
                        putExtra("date", date)
                        putExtra("route_id", routeId) // Thêm route_id
                    }
                    startActivity(intent)
                } else {
                    // Không tìm thấy tuyến đường
                    android.widget.Toast.makeText(this, "Không tìm thấy tuyến đường phù hợp", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Show validation message
            if (departure.isEmpty()) {
                binding.tvDeparture.setTextColor(ContextCompat.getColor(this, R.color.accent_color))
            }
            if (destination.isEmpty()) {
                binding.tvDestination.setTextColor(ContextCompat.getColor(this, R.color.accent_color))
            }
            if (date.equals("Chọn ngày khởi hành")) {
                binding.tvDate.setTextColor(ContextCompat.getColor(this, R.color.accent_color))
            }
        }
    }
    private fun findRouteId(departure: String, destination: String, callback: (String?) -> Unit) {
        lifecycleScope.launch {
            val route = routeRepository.getRouteByDepartureAndDestination(departure, destination)
            callback(route?.id)
        }
    }


}