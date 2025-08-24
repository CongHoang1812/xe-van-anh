package com.example.authenticateuserandpass.ui.a_admin_ui.route.add_update

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.route.Route
import com.example.authenticateuserandpass.databinding.ActivityAddEditRouteBinding
import com.example.authenticateuserandpass.ui.a_admin_ui.route.RouteViewModel
import com.google.firebase.Timestamp

class AddEditRouteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditRouteBinding
    private lateinit var viewModel: RouteViewModel
    private var routeId: String? = null
    private var isEditMode = false
    private var currentRoute: Route? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddEditRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getIntentData()
        setupViewModel()
        setupToolbar()
        setupClickListeners()
        loadRouteData()
    }
    private fun getIntentData() {
        routeId = intent.getStringExtra("route_id")
        isEditMode = intent.getBooleanExtra("edit_mode", false)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (isEditMode) "Sửa tuyến đường" else "Thêm tuyến đường"
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[RouteViewModel::class.java]

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            enableForm(!isLoading)
        }

        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.addSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Thêm tuyến đường thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.updateSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Cập nhật tuyến đường thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.routes.observe(this) { routes ->
            if (isEditMode && currentRoute == null) {
                currentRoute = routes.find { it.id == routeId }
                currentRoute?.let { fillFormData(it) }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveRoute()
        }
    }

    private fun loadRouteData() {
        if (isEditMode && !routeId.isNullOrEmpty()) {
            viewModel.loadRoutes()
        }
    }

    private fun fillFormData(route: Route) {
        binding.apply {
            etOrigin.setText(route.origin)
            etDestination.setText(route.destination)
            etDistance.setText(route.distance)
            etDuration.setText(route.duration)
            etTripsPerDay.setText(route.tripsPerDay.toString())
        }
    }

    private fun saveRoute() {
        binding.apply {
            val origin = etOrigin.text.toString().trim()
            val destination = etDestination.text.toString().trim()
            val distance = etDistance.text.toString().trim()
            val duration = etDuration.text.toString().trim()
            val tripsPerDayText = etTripsPerDay.text.toString().trim()

            if (!validateInput(origin, destination, distance, duration, tripsPerDayText)) {
                return
            }

            val tripsPerDay = tripsPerDayText.toInt()

            val route = Route(
                id = if (isEditMode) routeId!! else "",
                origin = origin,
                destination = destination,
                distance = distance,
                duration = duration,
                tripsPerDay = tripsPerDay,
                createdAt = currentRoute?.createdAt ?: Timestamp.now()
            )

            if (isEditMode) {
                viewModel.updateRoute(route)
            } else {
                viewModel.addRoute(route)
            }
        }
    }

    private fun validateInput(
        origin: String,
        destination: String,
        distance: String,
        duration: String,
        tripsPerDay: String
    ): Boolean {
        when {
            origin.isEmpty() -> {
                binding.etOrigin.error = "Vui lòng nhập điểm khởi hành"
                binding.etOrigin.requestFocus()
                return false
            }
            destination.isEmpty() -> {
                binding.etDestination.error = "Vui lòng nhập điểm đến"
                binding.etDestination.requestFocus()
                return false
            }
            distance.isEmpty() -> {
                binding.etDistance.error = "Vui lòng nhập khoảng cách"
                binding.etDistance.requestFocus()
                return false
            }
            duration.isEmpty() -> {
                binding.etDuration.error = "Vui lòng nhập thời gian di chuyển"
                binding.etDuration.requestFocus()
                return false
            }
            tripsPerDay.isEmpty() -> {
                binding.etTripsPerDay.error = "Vui lòng nhập số chuyến mỗi ngày"
                binding.etTripsPerDay.requestFocus()
                return false
            }
            else -> {
                try {
                    val trips = tripsPerDay.toInt()
                    if (trips <= 0) {
                        binding.etTripsPerDay.error = "Số chuyến phải lớn hơn 0"
                        binding.etTripsPerDay.requestFocus()
                        return false
                    }
                } catch (e: NumberFormatException) {
                    binding.etTripsPerDay.error = "Số chuyến phải là số nguyên"
                    binding.etTripsPerDay.requestFocus()
                    return false
                }
            }
        }
        return true
    }

    private fun enableForm(enabled: Boolean) {
        binding.apply {
            etOrigin.isEnabled = enabled
            etDestination.isEnabled = enabled
            etDistance.isEnabled = enabled
            etDuration.isEnabled = enabled
            etTripsPerDay.isEnabled = enabled
            btnSave.isEnabled = enabled
            btnCancel.isEnabled = enabled
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}