package com.example.authenticateuserandpass.ui.a_admin_ui.route

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.route.Route
import com.example.authenticateuserandpass.databinding.ActivityRouteManagermentBinding
import com.example.authenticateuserandpass.ui.a_admin_ui.route.add_update.AddEditRouteActivity

class RouteManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRouteManagermentBinding
    private lateinit var routeAdapter: RouteAdapter
    private lateinit var viewModel: RouteViewModel
    private val routes = mutableListOf<Route>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRouteManagermentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupRecyclerView()
        setupViewModel()
        setupFab()
    }
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarRoutesManagement)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Quản lý tuyến đường"
        }
    }

    private fun setupRecyclerView() {
        routeAdapter = RouteAdapter(
            routes = routes,
            onEditClick = { route -> editRoute(route) },
            onDeleteClick = { route -> confirmDeleteRoute(route) }
        )

        binding.rvRoutesAdmin.apply {
            layoutManager = LinearLayoutManager(this@RouteManagementActivity)
            adapter = routeAdapter
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[RouteViewModel::class.java]

        viewModel.routes.observe(this) { routeList ->
            routes.clear()
            routes.addAll(routeList)
            routeAdapter.notifyDataSetChanged()

            // Show/hide empty state
            if (routeList.isEmpty()) {
                binding.rvRoutesAdmin.visibility = View.GONE
                // Show empty state view if you have one
            } else {
                binding.rvRoutesAdmin.visibility = View.VISIBLE
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            // Show/hide progress bar
            binding.progressBarRoutesAdmin?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.deleteSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Đã xóa tuyến đường thành công", Toast.LENGTH_SHORT).show()
            }
        }

        // Load initial data
        viewModel.loadRoutes()
    }

    private fun setupFab() {
        binding.fabAddRoute.setOnClickListener {
            // Navigate to add route activity
            val intent = Intent(this, AddEditRouteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun editRoute(route: Route) {
        val intent = Intent(this, AddEditRouteActivity::class.java)
        intent.putExtra("route_id", route.id)
        intent.putExtra("edit_mode", true)
        startActivity(intent)
    }

    private fun confirmDeleteRoute(route: Route) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa tuyến đường ${route.origin} - ${route.destination}?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteRoute(route.id)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRoutes()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
