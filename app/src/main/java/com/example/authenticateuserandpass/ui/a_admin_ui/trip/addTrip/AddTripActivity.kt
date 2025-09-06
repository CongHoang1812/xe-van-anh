package com.example.authenticateuserandpass.ui.a_admin_ui.trip.addTrip

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.bus.Bus
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.databinding.ActivityAddTripBinding
import com.example.authenticateuserandpass.ui.dialog.BusSelectionBottomSheet
import com.example.authenticateuserandpass.ui.dialog.DriverSelectionBottomSheet
import kotlinx.coroutines.launch

class AddTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTripBinding
    private val tripRepository = com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl()
    // Variables để lưu thông tin đã chọn
    private var selectedBus: Bus? = null
    private var selectedDriver: User? = null
    private var routeId: String? = null
    private var tripDate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        routeId = intent.getStringExtra("route_id")
        Log.d("AddTripActivity", "Route ID: $routeId")
        tripDate = intent.getStringExtra("date")
        Log.d("AddTripActivity", "Trip Date: $tripDate")
        setupToolbar()
        setupClickListeners()
    }
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupClickListeners() {
        // Click listener cho chọn xe
        binding.editChooseBusAdmin.setOnClickListener {
            showBusSelectionBottomSheet()
        }
        binding.editMainDriverAdmin.setOnClickListener {
            showDriverSelectionBottomSheet()
        }
        binding.btnAddTrip.setOnClickListener {
            createTrip()
        }


    }
    private fun showDriverSelectionBottomSheet() {
        val driverSelectionBottomSheet = DriverSelectionBottomSheet { selectedDriver ->
            onDriverSelected(selectedDriver)
        }
        driverSelectionBottomSheet.show(supportFragmentManager, "DriverSelectionBottomSheet")
    }

    private fun onDriverSelected(driver: User) {
        selectedDriver = driver
        binding.editMainDriverAdmin.setText(driver.name)
        // Lưu driver đã chọn để sử dụng khi tạo trip
        // selectedMainDriver = driver
    }

    private fun showBusSelectionBottomSheet() {
        val busSelectionBottomSheet = BusSelectionBottomSheet { selectedBus ->
            onBusSelected(selectedBus)
        }
        busSelectionBottomSheet.show(supportFragmentManager, "BusSelectionBottomSheet")
    }

    private fun onBusSelected(bus: Bus) {
        // Hiển thị thông tin xe đã chọn trong EditText
        selectedBus = bus
        binding.editChooseBusAdmin.setText("${bus.type} - ${bus.license_plate}")

        // Có thể lưu bus đã chọn vào biến để sử dụng sau
        //selectedBus = bus
    }

    private fun createTrip() {
        // Validate dữ liệu
        if (!validateInput()) {
            return
        }

        // Lấy thông tin từ các EditText

        val departureTimeString = binding.editTripTimeAdmin.text.toString()
        val priceString = binding.editPriceTripAdmin.text.toString()


        // Tạo Trip object
        val trip = Trip(
            id = "", // Firestore sẽ tự generate ID
            route_id = routeId!!,
            bus_id = selectedBus!!.id,
            main_driver_id = selectedDriver!!.uid,
            trip_date = tripDate!!,
            departure_time = departureTimeString,
            ticket_price = priceString,
            availableSeats = 24,
            distance = "160",
            duration = "150",
            status = "Chưa đi"
        )

        // Gọi repository để thêm trip
        lifecycleScope.launch {
            tripRepository.addTrip(trip, object : ResultCallback<com.example.authenticateuserandpass.data.source.Result<String>> {
                override fun onResult(result: com.example.authenticateuserandpass.data.source.Result<String>) {
                    when (result) {
                        is com.example.authenticateuserandpass.data.source.Result.Success -> {
                            Toast.makeText(this@AddTripActivity, "Thêm chuyến đi thành công!", Toast.LENGTH_SHORT).show()
                            // Quay lại activity trước đó
                            finish()
                        }
                        is Result.Error -> {
                            Toast.makeText(this@AddTripActivity, "Lỗi: ${result.error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    private fun validateInput(): Boolean {
        // Kiểm tra route_id
        if (routeId.isNullOrEmpty()) {
            Toast.makeText(this, "Chưa chọn tuyến đường", Toast.LENGTH_SHORT).show()
            return false
        }

        // Kiểm tra xe đã chọn
        if (selectedBus == null) {
            Toast.makeText(this, "Vui lòng chọn xe", Toast.LENGTH_SHORT).show()
            return false
        }

        // Kiểm tra tài xế đã chọn
        if (selectedDriver == null) {
            Toast.makeText(this, "Vui lòng chọn tài xế", Toast.LENGTH_SHORT).show()
            return false
        }

        // Kiểm tra giờ đến
        if (binding.editTripTimeAdmin.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập giờ đến", Toast.LENGTH_SHORT).show()
            return false
        }

        // Kiểm tra giá vé
        val priceString = binding.editPriceTripAdmin.text.toString().trim()
        if (priceString.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập giá vé", Toast.LENGTH_SHORT).show()
            return false
        }

        try {
            priceString.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Giá vé không hợp lệ", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}