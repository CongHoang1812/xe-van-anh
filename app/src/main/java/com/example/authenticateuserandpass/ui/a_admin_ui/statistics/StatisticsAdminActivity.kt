package com.example.authenticateuserandpass.ui.a_admin_ui.statistics

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.compareTo
import kotlin.div
import kotlin.text.format
import kotlin.text.toInt
import kotlin.text.toLong

class StatisticsAdminActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvAverageRevenue: TextView
    private lateinit var tvBestDay: TextView
    private lateinit var btnSelectMonth: Button

    private lateinit var firestore: FirebaseFirestore
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_statistics_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()
        initViews()
        setupMonthSelector()
        loadPaymentDataForMonth(selectedYear, selectedMonth)
    }

    private fun initViews() {
        barChart = findViewById(R.id.barChart)
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
        tvAverageRevenue = findViewById(R.id.tvAverageRevenue)
        tvBestDay = findViewById(R.id.tvBestDay)
        btnSelectMonth = findViewById(R.id.btnSelectMonth) // Add this button to your layout
    }

    private fun setupMonthSelector() {
        updateMonthButtonText()
        btnSelectMonth.setOnClickListener {
            showMonthPicker()
        }
    }

    private fun showMonthPicker() {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, 1)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, _ ->
                selectedYear = year
                selectedMonth = month
                updateMonthButtonText()
                loadPaymentDataForMonth(selectedYear, selectedMonth)
            },
            selectedYear,
            selectedMonth,
            1
        )

        datePickerDialog.show()
    }

    private fun updateMonthButtonText() {
        val calendar = Calendar.getInstance()
        calendar.set(selectedYear, selectedMonth, 1)
        val monthFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        btnSelectMonth.text = "Tháng: ${monthFormat.format(calendar.time)}"
    }

    private fun loadPaymentDataForMonth(year: Int, month: Int) {
        val calendar = Calendar.getInstance()

        // Start of month
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time

        // End of month
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.time

        firestore.collection("payments")
            .get()
            .addOnSuccessListener { documents ->
                val dailyRevenue = mutableMapOf<Int, Float>()
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                // Initialize all days with 0
                for (day in 1..daysInMonth) {
                    dailyRevenue[day] = 0f
                }

                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                for (document in documents) {
                    val amountStr = document.getString("amount") ?: "0"
                    // "230.000" -> "230000" -> Float
                    val amount = amountStr.replace(".", "").toFloat()

                    val paidAtStr = document.getString("paidAt")

                    if (!paidAtStr.isNullOrBlank()) {
                        try {
                            val date = format.parse(paidAtStr)

                            date?.let {
                                if (!it.before(startOfMonth) && !it.after(endOfMonth)) {
                                    val paymentCalendar = Calendar.getInstance()
                                    paymentCalendar.time = it
                                    val day = paymentCalendar.get(Calendar.DAY_OF_MONTH)

                                    dailyRevenue[day] = (dailyRevenue[day] ?: 0f) + amount
                                }
                            }
                        } catch (e: ParseException) {
                            // Nếu format sai thì bỏ qua record này
                            e.printStackTrace()
                        }
                    }
                }

                setupRevenueChart(dailyRevenue, daysInMonth)
            }
            .addOnFailureListener { exception ->
                setupWeeklyRevenueChart() // fallback
            }
    }

    private fun setupRevenueChart(dailyRevenue: Map<Int, Float>, daysInMonth: Int) {
        val revenueData = mutableListOf<BarEntry>()

        for (day in 1..daysInMonth) {
            val revenue = dailyRevenue[day] ?: 0f
            revenueData.add(BarEntry((day - 1).toFloat(), revenue))
        }

        val dataSet = BarDataSet(revenueData, "Doanh thu theo ngày (VNĐ)")
        dataSet.color = Color.parseColor("#FF673AB7")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 8f
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 0) formatCurrency(value.toLong()) else ""
            }
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        // Customize chart appearance
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.setFitBars(true)
        barChart.setDrawGridBackground(false)
        barChart.setVisibleXRangeMaximum(15f)
        barChart.isDragEnabled = true
        barChart.isScaleXEnabled = true
        barChart.isScaleYEnabled = false

        // Setup X-axis
        val daysOfMonth = Array(daysInMonth) { "${it + 1}" }
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(daysOfMonth)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.BLACK
        xAxis.labelCount = 15

        // Setup Y-axis
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.LTGRAY
        leftAxis.textColor = Color.BLACK
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return formatCurrency(value.toLong())
            }
        }

        barChart.axisRight.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()

        updateMonthlyStatistics(revenueData, daysOfMonth)
    }

    private fun updateMonthlyStatistics(revenueData: List<BarEntry>, daysOfMonth: Array<String>) {
        val totalRevenue = revenueData.sumOf { it.y.toLong() }
        tvTotalRevenue.text = formatCurrency(totalRevenue)

        val nonZeroEntries = revenueData.filter { it.y > 0 }
        val averageRevenue = if (nonZeroEntries.isNotEmpty()) {
            totalRevenue / nonZeroEntries.size
        } else {
            0L
        }
        tvAverageRevenue.text = formatCurrency(averageRevenue)

        val maxEntry = revenueData.maxByOrNull { it.y }
        maxEntry?.let {
            val dayIndex = it.x.toInt()
            tvBestDay.text = "Ngày ${daysOfMonth[dayIndex]}"
        }
    }

    private fun formatCurrency(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "${formatter.format(amount)}"
    }

    // Keep existing setupWeeklyRevenueChart as fallback
    private fun setupWeeklyRevenueChart() {
        // Your existing implementation as fallback
    }
}

