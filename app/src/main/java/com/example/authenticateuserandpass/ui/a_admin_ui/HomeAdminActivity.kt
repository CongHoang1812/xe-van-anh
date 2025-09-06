package com.example.authenticateuserandpass.ui.a_admin_ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.AdminOption
import com.example.authenticateuserandpass.databinding.ActivityHomeAdminBinding
import com.example.authenticateuserandpass.ui.a_admin_ui.route.RouteManagementActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.settings.SettingsAdminsActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.statistics.StatisticsAdminActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.ticket.TicketManagementActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.trip.SearchTripActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.user.UserManagementActivity

class HomeAdminActivity : AppCompatActivity() {

    private lateinit var recyclerViewMenu: RecyclerView
    private lateinit var adminOptionAdapter: AdminOptionAdapter
    private lateinit var binding : ActivityHomeAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        loadMenuData()
    }


    private fun initViews() {
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu)
        binding.circularProgressBar.setProgressWithAnimation(75f, 2000)
        binding.circularProgressBar2.setProgressWithAnimation(75f, 2000)
        binding.circularProgressBar.roundBorder = true
        binding.circularProgressBar2.roundBorder = true
    }

    private fun setupRecyclerView() {
        // Thiết lập GridLayoutManager với 2 cột
        val gridLayoutManager = GridLayoutManager(this, 3)
        recyclerViewMenu.layoutManager = gridLayoutManager

        // Tạo adapter với dữ liệu giả và xử lý click
        adminOptionAdapter = AdminOptionAdapter(getMenuOptions()) { adminOption ->
            handleMenuClick(adminOption)
        }

        recyclerViewMenu.adapter = adminOptionAdapter
    }

    private fun loadMenuData() {
        // Dữ liệu đã được tải trong getMenuOptions()
        Log.d("AdminDashboard", "Menu data loaded successfully")
    }

    private fun getMenuOptions(): List<AdminOption> {
        return listOf(
            AdminOption(1, "Quản lý chuyến", R.drawable.ic_directions_bus, "Quản lý các chuyến xe"),
            AdminOption(2, "Quản lý vé", R.drawable.ic_receipt, "Quản lý và theo dõi vé xe"),
            AdminOption(3, "Quản lý tuyến", R.drawable.ic_route, "Quản lý các tuyến đường"),
            AdminOption(4, "Quản lý xe", R.drawable.ic_directions_bus, "Quản lý đội xe"),
            AdminOption(5, "Quản lý tài xế", R.drawable.ic_driver, "Quản lý nhân viên lái xe"),
            AdminOption(6, "Khách hàng", R.drawable.ic_customers, "Quản lý thông tin khách hàng"),
            AdminOption(7, "Báo cáo", R.drawable.ic_report, "Xem báo cáo và thống kê"),
            AdminOption(8, "Cài đặt", R.drawable.ic_settings, "Cài đặt hệ thống"),
            AdminOption(9, "Quản lý chuyến", R.drawable.ic_directions_bus, "Quản lý các chuyến xe"),
            AdminOption(10, "Quản lý vé", R.drawable.ic_receipt, "Quản lý và theo dõi vé xe"),
            AdminOption(11, "Quản lý tuyến", R.drawable.ic_route, "Quản lý các tuyến đường"),
            AdminOption(12, "Quản lý xe", R.drawable.ic_directions_bus, "Quản lý đội xe"),
            AdminOption(13, "Quản lý tài xế", R.drawable.ic_driver, "Quản lý nhân viên lái xe"),
            AdminOption(14, "Khách hàng", R.drawable.ic_customers, "Quản lý thông tin khách hàng"),
            AdminOption(15, "Báo cáo", R.drawable.ic_report, "Xem báo cáo và thống kê"),
            AdminOption(16, "Cài đặt", R.drawable.ic_settings, "Cài đặt hệ thống")

        )
    }

    private fun handleMenuClick(adminOption: AdminOption) {
        Log.d("AdminDashboard", "Selected menu: ${adminOption.title}")

        // TODO: Thêm intent để chuyển sang màn hình tương ứng
        when (adminOption.id) {
            1 -> {
                // Intent to Trip Management Activity
                var intent = Intent(this, SearchTripActivity::class.java)
                startActivity(intent)
            }
            2 -> {
                // Intent to Ticket Management Activity
                var intent = Intent(this, TicketManagementActivity::class.java)
                startActivity(intent)
            }
            3 -> {
                // Intent to Route Management Activity
                var intent = Intent(this, RouteManagementActivity::class.java)
                startActivity(intent)

            }
            4 -> {
                // Intent to Vehicle Management Activity
                Log.d("AdminDashboard", "Navigating to Vehicle Management")
            }
            5 -> {
                // Intent to Driver Management Activity
                Log.d("AdminDashboard", "Navigating to Driver Management")
            }
            6 -> {
                // Intent to Customer Management Activity
                val intent = Intent(this, UserManagementActivity::class.java)
                startActivity(intent)
            }
            7 -> {
                // Intent to Reports Activity
                val intent = Intent(this, StatisticsAdminActivity::class.java)
                startActivity(intent)
            }
            8 -> {
                // Intent to Settings Activity
                val intent = Intent(this, SettingsAdminsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}