package com.example.authenticateuserandpass.ui.findticket.filterTicket

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.ui.findticket.FindTicketActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.RangeSlider
import java.text.DecimalFormat

class FilterTicketActivity : AppCompatActivity() {

    private lateinit var rangeSliderTime: RangeSlider
    private lateinit var rangeSliderSeats: RangeSlider
    private lateinit var rangeSliderPrice: RangeSlider

    private lateinit var tvTimeStart: TextView
    private lateinit var tvTimeEnd: TextView
    private lateinit var tvSeatsStart: TextView
    private lateinit var tvSeatsEnd: TextView
    private lateinit var tvPriceStart: TextView
    private lateinit var tvPriceEnd: TextView

    private lateinit var cbLimousine: CheckBox
    private lateinit var cbLimousine2: CheckBox
    private lateinit var btnSearch: MaterialButton
    private lateinit var tvResetFilter: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_filter_ticket)

        initViews()
        setupRangeSliders()
        setupClickListeners()
        setupToolbar()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        rangeSliderTime = findViewById(R.id.rangeSliderTime)
        rangeSliderSeats = findViewById(R.id.rangeSliderSeats)
        rangeSliderPrice = findViewById(R.id.rangeSliderPrice)

        rangeSliderTime.isTickVisible = false
        rangeSliderSeats.isTickVisible = false
        rangeSliderPrice.isTickVisible = false

        tvTimeStart = findViewById(R.id.tvTimeStart)
        tvTimeEnd = findViewById(R.id.tvTimeEnd)
        tvSeatsStart = findViewById(R.id.tvSeatsStart)
        tvSeatsEnd = findViewById(R.id.tvSeatsEnd)
        tvPriceStart = findViewById(R.id.tvPriceStart)
        tvPriceEnd = findViewById(R.id.tvPriceEnd)


        cbLimousine = findViewById(R.id.cbLimousine)
        cbLimousine2 = findViewById(R.id.cbLimousine2)
        btnSearch = findViewById(R.id.btnSearch)
        tvResetFilter = findViewById(R.id.tvResetFilter)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRangeSliders() {
        // Time slider listener
        rangeSliderTime.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            tvTimeStart.text = formatTime(values[0].toInt())
            tvTimeEnd.text = formatTime(values[1].toInt())
        }

        // Seats slider listener
        rangeSliderSeats.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            tvSeatsStart.text = values[0].toInt().toString()
            tvSeatsEnd.text = values[1].toInt().toString()
        }

        // Price slider listener
        rangeSliderPrice.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            tvPriceStart.text = formatPrice(values[0].toLong())
            tvPriceEnd.text = formatPrice(values[1].toLong())
        }
        updateDisplayValues()
    }
    private fun setupClickListeners() {
        // Nút tìm kiếm
        btnSearch.setOnClickListener {
            searchTickets()
        }

        // Đặt lại bộ lọc
        tvResetFilter.setOnClickListener {
            resetFilters()
        }
    }



    private fun searchTickets() {
        // Lấy các giá trị filter
        val timeValues = rangeSliderTime.values
        val seatsValues = rangeSliderSeats.values
        val priceValues = rangeSliderPrice.values

        val filterData = Intent().apply {
            // Thời gian (phút từ 00:00)
            putExtra("TIME_START", timeValues[0].toInt())
            putExtra("TIME_END", timeValues[1].toInt())

            // Số ghế trống
            putExtra("SEATS_MIN", seatsValues[0].toInt())
            putExtra("SEATS_MAX", seatsValues[1].toInt())

            // Giá vé
            putExtra("PRICE_MIN", priceValues[0].toLong())
            putExtra("PRICE_MAX", priceValues[1].toLong())

            // Loại xe
            putExtra("VEHICLE_LIMOUSINE", cbLimousine.isChecked)
            putExtra("VEHICLE_LIMOUSINE2", cbLimousine2.isChecked)

            // Thời gian formatted
            putExtra("TIME_START_FORMATTED", formatTime(timeValues[0].toInt()))
            putExtra("TIME_END_FORMATTED", formatTime(timeValues[1].toInt()))

            // Giá formatted
            putExtra("PRICE_START_FORMATTED", formatPrice(priceValues[0].toLong()))
            putExtra("PRICE_END_FORMATTED", formatPrice(priceValues[1].toLong()))

            // Đánh dấu có áp dụng filter
            putExtra("APPLY_FILTER", true)
        }

        val intent = Intent(this, FindTicketActivity::class.java)
        intent.putExtras(filterData.extras ?: Bundle())

        setResult(RESULT_OK, filterData)
        finish()
    }

    private fun resetFilters() {
        // Reset về giá trị mặc định
        rangeSliderTime.values = listOf(150f, 1200f) // 02:30 - 20:00
        rangeSliderSeats.values = listOf(2f, 24f)
        rangeSliderPrice.values = listOf(100000f, 800000f)

        cbLimousine.isChecked = false
        cbLimousine2.isChecked = false

        updateDisplayValues()
    }

    private fun updateDisplayValues() {
        // Cập nhật hiển thị giá trị ban đầu
        val timeValues = rangeSliderTime.values
        val seatsValues = rangeSliderSeats.values
        val priceValues = rangeSliderPrice.values

        tvTimeStart.text = formatTime(timeValues[0].toInt())
        tvTimeEnd.text = formatTime(timeValues[1].toInt())
        tvSeatsStart.text = seatsValues[0].toInt().toString()
        tvSeatsEnd.text = seatsValues[1].toInt().toString()
        tvPriceStart.text = formatPrice(priceValues[0].toLong())
        tvPriceEnd.text = formatPrice(priceValues[1].toLong())
    }

    private fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
    }

    private fun formatPrice(price: Long): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(price) + "đ"
    }
}