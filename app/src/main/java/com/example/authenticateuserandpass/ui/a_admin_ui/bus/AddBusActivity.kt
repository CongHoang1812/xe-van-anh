package com.example.authenticateuserandpass.ui.a_admin_ui.bus

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.bus.Bus
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.data.source.remote.BusDataSource
import com.example.authenticateuserandpass.databinding.ActivityAddBusBinding


class AddBusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBusBinding
    private val busDataSource = BusDataSource()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddBusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupToolbar()
        setupSaveButton()
    }
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarAddBusManagement)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Thêm mới xe"
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveBus.setOnClickListener {
            val type = binding.tvBusType.text.toString().trim()
            val licensePlate = binding.tvBusLisencePlate.text.toString().trim()
            val seatCountText = binding.tvBusSeatCount.text.toString().trim()

            if (type.isEmpty() || licensePlate.isEmpty() || seatCountText.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val seatCount = seatCountText.toIntOrNull()
            if (seatCount == null) {
                Toast.makeText(this, "Số ghế không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newBus = Bus(
                type = type,
                license_plate = licensePlate,
                seat_count = seatCount
            )

            busDataSource.addBus(newBus, object : ResultCallback<Result<String>> {
                override fun onResult(result: Result<String>) {
                    when (result) {
                        is Result.Success -> {
                            Toast.makeText(this@AddBusActivity, "Thêm xe thành công", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK) // gửi tín hiệu về BusManagementActivity
                            finish()
                        }
                        is Result.Error -> {
                            Toast.makeText(this@AddBusActivity, "Lỗi: ${result.error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

}