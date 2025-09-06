package com.example.authenticateuserandpass.ui.a_admin_ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.authenticateuserandpass.MainActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.databinding.ActivitySettingsAdminsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsAdminsActivity : AppCompatActivity() {
    private lateinit var binding :  ActivitySettingsAdminsBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsAdminsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Glide.with(this) // hoặc context, hoặc fragment
            .load("https://a0.anyrgb.com/pngimg/1526/18/icon-ico-files-admin-system-administrator-ico-icon-download-user-profile-password-megaphone-login.png")
            .into(binding.ivAdminAvatar)

        auth = FirebaseAuth.getInstance()
        setOnClickListener()

    }

    private fun setOnClickListener() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
            var intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}