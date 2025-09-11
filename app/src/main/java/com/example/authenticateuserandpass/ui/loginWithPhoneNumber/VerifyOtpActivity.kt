package com.example.authenticateuserandpass.ui.loginWithPhoneNumber

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.ui.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ActivityVerifyOtpBinding
import com.example.authenticateuserandpass.ui.viewmodel.OtpViewModel
import kotlin.getValue

class VerifyOtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyOtpBinding
    private val otpViewModel: OtpViewModel by viewModels()

    private lateinit var userId: String
    private lateinit var apiKey: String
    private lateinit var phone: String
    private var countDownTimer: CountDownTimer? = null
    private val otpEditTexts = mutableListOf<EditText>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getIntentData()
        setupViews()
        setupObservers()
        setupClickListeners()
        setupOtpInputs()
        startResendTimer()
    }
    private fun getIntentData() {
        userId = intent.getStringExtra("user_id") ?: ""
        apiKey = intent.getStringExtra("api_key") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
    }
    private fun setupViews() {
        binding.tvPhoneNumber.text = formatPhoneNumber(phone)

        // Initialize OTP EditTexts list
        otpEditTexts.apply {
            add(binding.etOtp1)
            add(binding.etOtp2)
            add(binding.etOtp3)
            add(binding.etOtp4)
            add(binding.etOtp5)
            add(binding.etOtp6)


        }
    }
    private fun formatPhoneNumber(phone: String): String {
        return if (phone.length >= 10) {
            "+84 ${phone.takeLast(9).chunked(3).joinToString(" ")}"
        } else {
            phone
        }
    }
    private fun setupOtpInputs() {
        otpEditTexts.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        // Move to next EditText
                        if (index < otpEditTexts.size - 1) {
                            otpEditTexts[index + 1].requestFocus()
                        }

                        // Update hidden EditText for compatibility
                        updateOtpCode()

                        // Check if all fields are filled
                        if (isOtpComplete()) {
                            binding.btnVerifyOtp.isEnabled = true
                            binding.btnVerifyOtp.alpha = 1.0f
                        }
                    } else if (s.isNullOrEmpty() && index > 0) {
                        // Move to previous EditText when deleting
                        otpEditTexts[index - 1].requestFocus()
                        updateOtpCode()
                        binding.btnVerifyOtp.isEnabled = false
                        binding.btnVerifyOtp.alpha = 0.6f
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun updateOtpCode() {
        val otpCode = otpEditTexts.joinToString("") { it.text.toString() }
        binding.etOtpCode.setText(otpCode)
    }

    private fun isOtpComplete(): Boolean {
        return otpEditTexts.all { it.text.toString().isNotEmpty() }
    }

    private fun setupObservers() {
        otpViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnVerifyOtp.text = ""
                binding.btnVerifyOtp.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnVerifyOtp.text = "Xác Thực"
                binding.btnVerifyOtp.isEnabled = isOtpComplete()
            }

            // Disable OTP inputs during loading
            otpEditTexts.forEach { it.isEnabled = !isLoading }
            binding.btnResendOtp.isEnabled = !isLoading
        }

        otpViewModel.otpVerifyResult.observe(this) { message ->
            showMessage(message)

            if (message.contains("thành công", ignoreCase = true)) {
                // Success - navigate to next activity
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Error - clear OTP fields and allow retry
                clearOtpFields()
            }
        }

        otpViewModel.otpSendResult.observe(this) { message ->
            showMessage(message)
            if (message.contains("thành công", ignoreCase = true)) {
                startResendTimer()
            }
        }
    }

    private fun showMessage(message: String) {
        binding.tvMessage.apply {
            visibility = View.VISIBLE
            text = message

            // Set background color based on message type
            val isSuccess = message.contains("thành công", ignoreCase = true)
            val backgroundColor = if (isSuccess) {
                ContextCompat.getColor(context, R.color.success_color)
            } else {
                ContextCompat.getColor(context, R.color.error_color)
            }
            setBackgroundColor(backgroundColor)
        }

        // Hide message after 3 seconds
        binding.tvMessage.postDelayed({
            binding.tvMessage.visibility = View.GONE
        }, 3000)
    }

    private fun clearOtpFields() {
        otpEditTexts.forEach {
            it.text.clear()
        }
        otpEditTexts.first().requestFocus()
        binding.btnVerifyOtp.isEnabled = false
        binding.btnVerifyOtp.alpha = 0.6f
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnVerifyOtp.setOnClickListener {
            val otpCode = otpEditTexts.joinToString("") { it.text.toString() }
            if (otpCode.length == 6) {
                otpViewModel.verifyOtp(userId, apiKey, otpCode, phone)
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnResendOtp.setOnClickListener {
            otpViewModel.sendOtp(apiKey, phone)
            binding.btnResendOtp.visibility = View.GONE
            binding.tvTimer.visibility = View.VISIBLE
            clearOtpFields()
        }
    }

    private fun startResendTimer() {
        binding.btnResendOtp.visibility = View.GONE
        binding.tvTimer.visibility = View.VISIBLE

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = "Gửi lại sau ${String.format("%02d:%02d", seconds / 60, seconds % 60)}"
            }

            override fun onFinish() {
                binding.tvTimer.visibility = View.GONE
                binding.btnResendOtp.visibility = View.VISIBLE
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}