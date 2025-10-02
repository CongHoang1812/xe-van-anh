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
import com.example.authenticateuserandpass.ui.a_admin_ui.bus.BusManagementActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.route.RouteManagementActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.settings.SettingsAdminsActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.statistics.StatisticsAdminActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.ticket.TicketManagementActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.trip.SearchTripActivity
import com.example.authenticateuserandpass.ui.a_admin_ui.user.UserManagementActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        getDailyRevenue()
    }


    private fun initViews() {
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu)

        binding.circularProgressBar2.setProgressWithAnimation(75f, 2000)
        binding.circularProgressBar.roundBorder = true
        binding.circularProgressBar2.roundBorder = true
        val totalSeats = 456
        val totalTrip =  19
        countTodayBookings { count ->
            binding.textView66 .text = "$count"
            val percent = if (totalSeats > 0) (count.toFloat() / totalSeats) * 100 else 0f
            val percent1 = String.format(Locale.US,"%.2f", percent).toFloat()
            binding.circularProgressBar.setProgressWithAnimation(percent, 2000)
            binding.textView49.text = "${percent1}%"
        }

        countTodayTrips { trips ->
            binding.textView662.text = "$trips"
            val percent = if (totalSeats > 0) (trips.toFloat() / totalTrip) * 100 else 0f
            val percent1 = String.format(Locale.US,"%.2f", percent).toFloat()
            binding.circularProgressBar2.setProgressWithAnimation(percent, 2000)
            binding.textView492.text = "${percent1}%"
        }
    }

    private fun setupRecyclerView() {
        // Thiết lập GridLayoutManager với 2 cột
        val gridLayoutManager = GridLayoutManager(this, 2)
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
            AdminOption(4, "Quản lý xe", R.drawable.bus, "Quản lý đội xe"),
            //AdminOption(5, "Quản lý tài xế", R.drawable.ic_driver, "Quản lý nhân viên lái xe"),
            AdminOption(6, "Quản lý Người dùng", R.drawable.ic_customers, "Quản lý thông tin khách hàng"),
            AdminOption(7, "Báo cáo", R.drawable.ic_report, "Xem báo cáo và thống kê"),
            AdminOption(8, "Cài đặt", R.drawable.ic_settings, "Cài đặt hệ thống"),
            //AdminOption(9, "Quản lý chuyến", R.drawable.ic_directions_bus, "Quản lý các chuyến xe"),


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
                val intent = Intent(this, BusManagementActivity::class.java)
                startActivity(intent)
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
    private fun calculateDailyRevenue(date: String, callback: (Double) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Tạo start và end time cho ngày được chọn
        val startDateTime = "$date 00:00:00"
        val endDateTime = "$date 23:59:59"

        db.collection("payments")
            .whereGreaterThanOrEqualTo("createdAt", startDateTime)
            .whereLessThanOrEqualTo("createdAt", endDateTime)
            .get()
            .addOnSuccessListener { documents ->
                var totalRevenue = 0.0

                for (document in documents) {
                    val amountString = document.getString("amount") ?: "0"
                    // Loại bỏ dấu chấm và chuyển đổi sang Double
                    val cleanAmount = amountString.replace(".", "").replace(",", "")
                    try {
                        val amount = cleanAmount.toDouble()
                        totalRevenue += amount
                    } catch (e: NumberFormatException) {
                        Log.e("Revenue", "Error parsing amount: $amountString", e)
                    }
                }

                callback(totalRevenue)
            }
            .addOnFailureListener { exception ->
                Log.e("Revenue", "Error getting payments: ", exception)
                callback(0.0)
            }
    }

    fun countTodayBookings(callback: (Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Lấy ngày hôm nay
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = sdf.format(Date()) // ví dụ: "2025-09-18"

        val start = "$todayDate 00:00:00"
        val end = "$todayDate 23:59:59"

        db.collection("bookings")
            .whereGreaterThanOrEqualTo("book_at", start)
            .whereLessThanOrEqualTo("book_at", end)
            .get()
            .addOnSuccessListener { result ->
                callback(result.size())
            }
            .addOnFailureListener {
                callback(0)
            }
    }
    fun countTodayTrips(callback: (Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Lấy ngày hôm nay theo định dạng dd/MM/yyyy
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayDate = sdfDate.format(Date()) // ví dụ "19/09/2025"

        // Lấy giờ phút hiện tại
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = sdfTime.format(Date()) // ví dụ "10:30"

        db.collection("trip")
            .whereEqualTo("trip_date", todayDate) // lọc theo ngày hôm nay
            .get()
            .addOnSuccessListener { result ->
                var count = 0
                for (doc in result) {
                    val departureTime = doc.getString("departure_time") ?: continue

                    // So sánh giờ khởi hành với giờ hiện tại
                    if (departureTime < currentTime) {
                        count++
                    }
                }
                callback(count)
            }
            .addOnFailureListener {
                callback(0)
            }
    }

    // Hàm helper để format số tiền
    private fun formatCurrency(amount: Double): String {
        val formatter = java.text.DecimalFormat("#,###")
        return formatter.format(amount)
    }

    // Sử dụng hàm
    private fun getDailyRevenue() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        calculateDailyRevenue(today) { revenue ->
            runOnUiThread {
                val formattedRevenue = formatCurrency(revenue)
                Log.d("Revenue", "Doanh thu hôm nay: $formattedRevenue VND")
                binding.tvRevenueToday.text = "Doanh thu hôm nay: $formattedRevenue VND"
            }
        }
    }
}