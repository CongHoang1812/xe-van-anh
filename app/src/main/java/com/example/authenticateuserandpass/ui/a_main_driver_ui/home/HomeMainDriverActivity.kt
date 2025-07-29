package com.example.authenticateuserandpass.ui.a_main_driver_ui.home

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ActivityMainDriverHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeMainDriverActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainDriverHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setLightStatusBar(this)
        setupBottomNav()
    }
    private fun setupBottomNav() {
        val navView: BottomNavigationView = binding.bottomMainDriverNavView
        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment)
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
    }

    fun setLightStatusBar(activity: Activity) {
        val window = activity.window

        // B1: Cho phép hoặc không cho content "chạy lên status bar"
        // true = giữ lại padding hệ thống (UI nằm dưới status bar, an toàn)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        // B2: Dù bị deprecated, vẫn KHÔNG sao khi set ở đây (material standard)
        @Suppress("DEPRECATION") // 👍 tránh warning
        window.statusBarColor = Color.WHITE // hoặc ContextCompat.getColor(this, R.color.white)

        // B3: Kiểm soát màu icon status bar (đen hay trắng)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true // ✅ icon đen trên nền trắng
    }


}