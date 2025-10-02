package com.example.authenticateuserandpass.ui.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.authenticateuserandpass.R

class NewsActivity : AppCompatActivity() {
    private lateinit var  binding: com.example.authenticateuserandpass.databinding.ActivityNewsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = com.example.authenticateuserandpass.databinding.ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Glide.with(this) // context: Activity hoặc Fragment
            .load("https://cms.mobihome.vn/Data/Sites/1/media/dsc05558.jpg") // link hoặc Uri, File, resource
            .into(binding.imageView20) // ImageView cần hiển thị
    }
}