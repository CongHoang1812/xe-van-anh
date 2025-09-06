package com.example.authenticateuserandpass.ui.a_admin_ui.trip

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.databinding.ActivityTripManagementBinding
import com.example.authenticateuserandpass.ui.a_admin_ui.trip.addTrip.AddTripActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTripManagementBinding
    private lateinit var tripAdapter: TripAdapter
    private lateinit var viewModel: TripManagementViewModel

    private var currentDate: String = ""
    private var currentOrigin: String = ""
    private var currentDestination: String = ""
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTripManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Nhận dữ liệu từ Intent
        getIntentData()

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()

        // Load dữ liệu
        loadTrips()
    }
    private fun getIntentData() {
        currentDate = intent.getStringExtra("date") ?: getCurrentDate()
        currentOrigin = intent.getStringExtra("departure") ?: ""
        currentDestination = intent.getStringExtra("destination") ?: ""

        android.util.Log.d("TripManagement", "Date: $currentDate, Origin: $currentOrigin, Destination: $currentDestination")
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Quản lý chuyến đi"
        }
        val backArrow = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24)
        backArrow?.setTint(ContextCompat.getColor(this, android.R.color.black))
        supportActionBar?.setHomeAsUpIndicator(backArrow)


        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[TripManagementViewModel::class.java]
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            onEditClick = { trip -> editTrip(trip) },
            onDeleteClick = { trip -> deleteTrip(trip) },
            onItemClick = { trip -> viewTripDetails(trip) }
        )

        binding.rvTrips.apply {
            adapter = tripAdapter
            layoutManager = LinearLayoutManager(this@TripManagementActivity)

            // Thêm decoration cho khoảng cách
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.bottom = 8.dpToPx()
                    outRect.left = 8.dpToPx()
                    outRect.right = 8.dpToPx()
                    if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.top = 8.dpToPx()
                    }
                }
            })
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun setupClickListeners() {
        binding.fabAddTrip.setOnClickListener {
            addNewTrip()
        }
    }

    private fun setupObservers() {
        viewModel.trips.observe(this) { trips ->
            tripAdapter.updateTrips(trips)
            updateTripInfo(trips)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Hiển thị/ẩn loading indicator nếu có
            // binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                showError(errorMessage)
            }
        }


    }

    private fun loadTrips() {
        viewModel.loadTrips(currentOrigin, currentDestination,  currentDate)
    }

    private fun updateTripInfo(trips: List<Trip>) {
        if (trips.isNotEmpty()) {
            val trip = trips.first()
            binding.tvTripName.text = "${currentOrigin} - ${currentDestination}"
        } else if (currentOrigin.isNotEmpty() && currentDestination.isNotEmpty()) {
            binding.tvTripName.text = "$currentOrigin - $currentDestination"
        } else {
            binding.tvTripName.text = "Tất cả chuyến đi"
        }

        // Format ngày đẹp hơn
        binding.tvTripDate.text = "Ngày: ${formatDate(currentDate)}"
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvTrips.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvTrips.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Lỗi")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                //viewModel.clearError()
            }
            .setNeutralButton("Thử lại") { dialog, _ ->
                dialog.dismiss()
                loadTrips()
            }
            .show()
    }

    private fun addNewTrip() {
        val routeId = intent.getStringExtra("route_id") ?: ""
        var intent = Intent(this, AddTripActivity::class.java)
        intent.putExtra("route_id", routeId)
        intent.putExtra("date", currentDate)
        startActivity(intent)
    }

    private fun editTrip(trip: Trip) {
        // TODO: Navigate to AddEditTripActivity with trip data
        Toast.makeText(this, "Chỉnh sửa chuyến ${trip.departure_time}", Toast.LENGTH_SHORT).show()
    }

    private fun viewTripDetails(trip: Trip) {
        // TODO: Navigate to TripDetailActivity
        Toast.makeText(this, "Xem chi tiết chuyến ${trip.departure_time}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteTrip(trip: Trip) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa chuyến đi lúc ${trip.departure_time}?")
            .setPositiveButton("Xóa") { _, _ ->
                // TODO: Implement delete trip API call
                Toast.makeText(this, "Chức năng xóa chuyến đi đang được phát triển", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }



    companion object {
        private const val REQUEST_CODE_ADD_TRIP = 1001
        private const val REQUEST_CODE_EDIT_TRIP = 1002
    }
}
