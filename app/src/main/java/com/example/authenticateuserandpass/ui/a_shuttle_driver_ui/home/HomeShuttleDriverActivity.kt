package com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.authenticateuserandpass.ui.MainActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ActivityHomeShuttleDriverBinding
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.adapter.ViewPagerAdapter
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.errorReport.ErrorReportActivity
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.tabFragments.DaHoanThanhFragment
import com.facebook.appevents.codeless.internal.ViewHierarchy.setOnClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class HomeShuttleDriverActivity : AppCompatActivity() {
    private  var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityHomeShuttleDriverBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeShuttleDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager2 = findViewById<ViewPager2>(R.id.viewPager2)

        val adapter = ViewPagerAdapter(this)
        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "Khách đón"
                1 -> "Khách trả"
                2 -> "Đã hoàn thành"
                else -> ""
            }
        }.attach()
        binding.includeShuttleDriver.hambugerMenu2.setOnClickListener {
            openDrawer()
        }
        setUpNavigationDrawerItemSelected()
    }
    fun reloadHoanThanh() {
        val fragmentManager = supportFragmentManager
        val fragments = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is DaHoanThanhFragment) {
                fragment.loadHoanThanh()
            }
        }
    }
    fun reloadAllTabs() {
        val fragmentManager = supportFragmentManager
        for (fragment in fragmentManager.fragments) {
            when(fragment) {
                //is KhachDonFragment -> fragment.loadKhachDon()
                //is KhachTraFragment -> fragment.loadKhachTra()
                is DaHoanThanhFragment -> fragment.loadHoanThanh()
            }
        }
    }
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    // lắng nghe sự kiện một phần tử trong menu navigation drawer được click
    private fun setUpNavigationDrawerItemSelected() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_drawer_logout -> {
                    auth.signOut()
                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                    var intent = Intent(this@HomeShuttleDriverActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    this.finish()
                }

                R.id.menu_item_drawer_error_report -> {
                    var intent = Intent(this@HomeShuttleDriverActivity, ErrorReportActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_item_drawer_import -> {
                    // todo
                }

                // ...
            }
            false
        }
    }
}