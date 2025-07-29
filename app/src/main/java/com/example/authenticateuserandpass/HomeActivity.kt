package com.example.authenticateuserandpass

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.authenticateuserandpass.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
//        navController = navHostFragment.navController
//        appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.homeFragment, R.id.myTicketFragment, R.id.accountFragment),
//            drawerLayout = binding.drawerLayout
//        )
//        // gắn navigation view vào navController nếu navigation view tồn tại:
//        binding.navView?.setupWithNavController(navController)
//        val toolbar: Toolbar? = findViewById(R.id.toolbar_home)
//        setSupportActionBar(toolbar)
//        toolbar?.setupWithNavController(navController, appBarConfiguration)
        setupBottomNav()



    }
    private fun setupBottomNav() {
        val navView: BottomNavigationView = binding.includeMainLayout.bottomNavView
        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment)
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
    }
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

}