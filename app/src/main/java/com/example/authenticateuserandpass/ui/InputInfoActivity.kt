package com.example.authenticateuserandpass.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.authenticateuserandpass.HomeActivity
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.databinding.ActivityInputInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class InputInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputInfoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInputInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        val genders = listOf("Nam", "Nữ", "Khác")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        binding.spinnerGender.adapter = genderAdapter
        binding.spinnerGender.setSelection(0)
        binding.btnUpdateAccount.setOnClickListener {
            val user = User(
                uid = auth.currentUser?.uid ?: "",
                name = binding.editFullName.text.toString(),
                email = binding.editEmail.text.toString(),
                phone = binding.editPhoneNumber.text.toString(),
                birthDate = binding.editDateOfBirth.text.toString(),
                gender = binding.spinnerGender.selectedItem.toString(),
                address = binding.editAddress.text.toString(),
                avatarUrl = "" // avatar mặc định để trống
            )
            db.collection("users").document(user.uid)
                .set(user)
                .addOnSuccessListener {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi lưu thông tin", Toast.LENGTH_SHORT).show()
                }

        }
    }
}