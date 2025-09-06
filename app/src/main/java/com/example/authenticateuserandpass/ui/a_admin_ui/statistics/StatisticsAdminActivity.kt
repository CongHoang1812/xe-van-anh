package com.example.authenticateuserandpass.ui.a_admin_ui.statistics

import android.graphics.Color
import android.os.Bundle
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
import java.text.NumberFormat
import java.util.Locale

class StatisticsAdminActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvAverageRevenue: TextView
    private lateinit var tvBestDay: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_statistics_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupWeeklyRevenueChart()
    }

    private fun initViews() {
        barChart = findViewById(R.id.barChart)
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
        tvAverageRevenue = findViewById(R.id.tvAverageRevenue)
        tvBestDay = findViewById(R.id.tvBestDay)
    }

    private fun setupWeeklyRevenueChart() {
        // Sample monthly data - replace with your actual revenue data (30 days)
        val revenueData = mutableListOf<BarEntry>()

        // Generate sample data for 30 days
        for (day in 1..30) {
            val revenue = (1000000..4000000).random().toFloat() // Random revenue between 1M-4M VND
            revenueData.add(BarEntry((day - 1).toFloat(), revenue))
        }

        val dataSet = BarDataSet(revenueData, "Doanh thu theo ngày (VNĐ)")
        dataSet.color = Color.parseColor("#3F51B5")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 8f // Smaller text due to more data points
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return formatCurrency(value.toLong())
            }
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        // Customize chart appearance
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.setFitBars(true)
        barChart.setDrawGridBackground(false)
        barChart.setVisibleXRangeMaximum(15f) // Show max 15 bars at once
        barChart.isDragEnabled = true
        barChart.isScaleXEnabled = true
        barChart.isScaleYEnabled = false

        // Setup X-axis with day labels (1-30)
        val daysOfMonth = Array(30) { "${it + 1}" }
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(daysOfMonth)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.BLACK
        xAxis.labelCount = 15 // Show fewer labels to avoid crowding

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

        // Animation
        barChart.animateY(1000)
        barChart.invalidate()

        // Update summary statistics
        updateMonthlyStatistics(revenueData, daysOfMonth)
    }

    private fun updateMonthlyStatistics(revenueData: List<BarEntry>, daysOfMonth: Array<String>) {
        // Calculate total revenue
        val totalRevenue = revenueData.sumOf { it.y.toLong() }
        tvTotalRevenue.text = formatCurrency(totalRevenue)

        // Calculate average revenue
        val averageRevenue = totalRevenue / revenueData.size
        tvAverageRevenue.text = formatCurrency(averageRevenue)

        // Find best day
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
}
