package com.example.authenticateuserandpass.ui.loginWithPhoneNumber

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ActivityEnterPhoneNumberBinding
import com.example.authenticateuserandpass.ui.viewmodel.OtpViewModel
import kotlin.getValue

class EnterPhoneNumberActivity : AppCompatActivity() {
    private lateinit var binding : ActivityEnterPhoneNumberBinding
    private val otpViewModel: OtpViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEnterPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        otpViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar4.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRequestOtp.isEnabled = !isLoading
        }

        otpViewModel.otpRequestResult.observe(this) { message ->
            binding.tvMessage.visibility = View.VISIBLE
            binding.tvMessage.text = message
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }



        otpViewModel.navigateToVerify.observe(this) { shouldNavigate ->
            if (shouldNavigate) {
                val intent = Intent(this, VerifyOtpActivity::class.java).apply {
                    putExtra("user_id", "10")
                    putExtra("api_key", "e54c8e580cf5768316ee192d6cf266483b55434a7873f640b2778bbb69e5e96d")
                    putExtra("phone", binding.etPhoneNumber.text.toString())
                }
                startActivity(intent)
                otpViewModel.onNavigateComplete()
            }
        }

    }

    private fun setupClickListeners() {
        binding.btnRequestOtp.setOnClickListener {
            val phone = binding.etPhoneNumber.text.toString().trim()

            if (validateInput(phone)) {
                binding.tvMessage.visibility = View.GONE
                otpViewModel.requestOtp("10", "e54c8e580cf5768316ee192d6cf266483b55434a7873f640b2778bbb69e5e96d", phone)
            }
        }
    }

    private fun validateInput(phone: String): Boolean {
        when {

            phone.isEmpty() -> {
                binding.etPhoneNumber.error = "Vui lòng nhập số điện thoại"
                return false
            }
            !phone.matches(Regex("^[0-9]{10,11}$")) -> {
                Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> {
                binding.etPhoneNumber.error = null
                return true
            }
        }
    }
}