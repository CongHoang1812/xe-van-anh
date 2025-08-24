package com.example.authenticateuserandpass.ui.a_admin_ui.user.updateUser

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.repository.user.UserRepositoryImpl
import com.example.authenticateuserandpass.databinding.ActivityUpdateUserBinding
import kotlinx.coroutines.launch
import kotlin.getValue

class UpdateUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateUserBinding
    private val viewModel: UpdateUserViewModel by viewModels {
        Factory(UserRepositoryImpl())
    }
    private lateinit var currentUser: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        currentUser = intent.getParcelableExtra("user") ?: run {
            Toast.makeText(this, "Không tìm thấy thông tin user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setupSpinners()
        displayUserData()
        setupButtonListener()
        observeViewModel()
    }
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Enable/disable button based on loading state
                binding.btnAddNewUser.isEnabled = !state.isLoading
                binding.btnAddNewUser.text = if (state.isLoading) "Đang cập nhật..." else "Cập nhật User"

                // Show error message
                state.error?.let { error ->
                    Toast.makeText(this@UpdateUserActivity, error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                // Handle success
                if (state.isSuccess) {
                    state.successMessage?.let { message ->
                        Toast.makeText(this@UpdateUserActivity, message, Toast.LENGTH_SHORT).show()
                    }
                    viewModel.resetSuccess()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }


    private fun displayUserData() {
        binding.apply {
            editNewName.setText(currentUser.name)
            editNewEmail.setText(currentUser.email)
            editNewPhoneNumber.setText(currentUser.phone)
            editNewAddress.setText(currentUser.address)

            // Set spinner selections
            val roleList = listOf("admin", "user", "main_driver", "shuttle_driver")
            val rolePosition = roleList.indexOf(currentUser.role)
            if (rolePosition >= 0) {
                spinnerRole.setSelection(rolePosition)
            }

            val genderList = listOf("Nam", "Nữ", "Khác")
            val genderPosition = genderList.indexOf(currentUser.gender)
            if (genderPosition >= 0) {
                spinnerGender.setSelection(genderPosition)
            }
        }
    }

    private fun setupButtonListener() {
        binding.btnAddNewUser.setOnClickListener {
            val name = binding.editNewName.text.toString().trim()
            val email = binding.editNewEmail.text.toString().trim().lowercase()
            val phone = binding.editNewPhoneNumber.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()
            val gender = binding.spinnerGender.selectedItem.toString()
            val address = binding.editNewAddress.text.toString().trim()

            val updatedUser = currentUser.copy(
                name = name,
                email = email,
                phone = phone,
                role = role,
                gender = gender,
                address = address
            )

            viewModel.updateUser(currentUser.uid, updatedUser, currentUser.email, currentUser.phone)
        }

//        binding.btnCancel.setOnClickListener {
//            finish()
//        }
    }

    private fun setupSpinners() {
        val roleList = listOf("admin", "user", "main_driver", "shuttle_driver")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roleList)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = roleAdapter

        val genderList = listOf("Nam", "Nữ", "Khác")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderList)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = genderAdapter
    }
}