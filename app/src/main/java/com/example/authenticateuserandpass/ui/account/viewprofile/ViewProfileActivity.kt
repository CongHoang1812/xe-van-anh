package com.example.authenticateuserandpass.ui.account.viewprofile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.databinding.ActivityViewProfileBinding
import com.example.authenticateuserandpass.ui.account.updateProfile.UpdateProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewProfileActivity : AppCompatActivity(), MenuProvider {
    private lateinit var binding: ActivityViewProfileBinding
    private  var user : User = User()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val menuHost: MenuHost = this
        menuHost.addMenuProvider(this@ViewProfileActivity, this@ViewProfileActivity, Lifecycle.State.RESUMED)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setOnClickListener()
        setSpinner()
        loadUserData()
    }
    private fun setOnClickListener() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    private fun setSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinner_gender)
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_list,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

    }

    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    user = document.toObject(User::class.java) ?: User()
                    showUserData()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showUserData() {
        binding.editFullName.setText(user.name)
        binding.editEmail.setText(user.email)
        binding.editPhoneNumber.setText(user.phone)
        binding.editDateOfBirth.setText(user.birthDate)
        binding.editAddress.setText(user.address)

        // Spinner giới tính
        val genderPosition = resources.getStringArray(R.array.gender_list).indexOf(user.gender)
        binding.spinnerGender.setSelection(genderPosition)

        // Hiển thị ảnh đại diện
        if (user.avatarUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.avatarUrl)
                .into(binding.imageAvatar)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_menu1, menu)

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_account -> {
                var intent = Intent(this, UpdateProfileActivity::class.java)
                startActivity(intent)
                true
            }

            else -> false
        }

    }


}