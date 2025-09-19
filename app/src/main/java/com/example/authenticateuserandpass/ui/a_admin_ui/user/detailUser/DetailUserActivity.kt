package com.example.authenticateuserandpass.ui.a_admin_ui.user.detailUser

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.R

class DetailUserActivity : AppCompatActivity() {
    private lateinit var binding: com.example.authenticateuserandpass.databinding.ActivityDetailUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = com.example.authenticateuserandpass.databinding.ActivityDetailUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupToolbar()
        binding.button5.setOnClickListener {
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarRoutesManagement)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Thông tin người dùng"
        }
    }
}