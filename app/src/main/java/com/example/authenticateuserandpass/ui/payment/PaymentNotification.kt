package com.example.authenticateuserandpass.ui.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ActivityPaymentNotificationBinding

class PaymentNotification : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentNotificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaymentNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var result = intent.getStringExtra("result")
        binding.tvNotification.text = result

        binding.btnReturnHome.setOnClickListener {
            finish()
            var intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

        }

    }
}