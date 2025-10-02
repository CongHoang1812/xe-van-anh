package com.example.authenticateuserandpass.ui.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ActivityHotServiceBinding

class HotServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHotServiceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHotServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var url = intent.getStringExtra("url")
        Glide.with(this) // context: Activity hoặc Fragment
            .load(url) // link hoặc Uri, File, resource
            .into(binding.imageView20) // ImageView cần hiển thị
    }
}