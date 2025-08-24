package com.example.authenticateuserandpass.ui.a_admin_ui.user.createNew

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
import com.example.authenticateuserandpass.databinding.ActivityCreateNewUserBinding
import kotlinx.coroutines.launch
import kotlin.getValue

class CreateNewUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateNewUserBinding
    private val viewModel: CreateUserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateNewUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupSpinners()
        setupButtonListener()
        observeViewModel()
    }


    private fun setupButtonListener() {
        binding.btnAddNewUser.setOnClickListener {
            val name = binding.editNewName.text.toString()
            val email = binding.editNewEmail.text.toString().trim()
            val phone = binding.editNewPhoneNumber.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()
            val gender = binding.spinnerGender.selectedItem.toString()
            val address = binding.editNewAddress.text.toString().trim()

            viewModel.createUser(name, email, phone, role,  gender, address)
        }
    }

    private fun observeViewModel(){
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Enable/disable button based on loading state
                binding.btnAddNewUser.isEnabled = !state.isLoading
                binding.btnAddNewUser.text = if (state.isLoading) "Đang tạo..." else "Thêm User"

                // Show error message
                state.error?.let { error ->
                    Toast.makeText(this@CreateNewUserActivity, error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                // Handle success
                if (state.isSuccess) {
                    state.successMessage?.let { message ->
                        Toast.makeText(this@CreateNewUserActivity, message, Toast.LENGTH_SHORT).show()
                    }
                    clearForm()
                    viewModel.resetSuccess()
                    finish()
                }
            }
        }
    }


    private fun clearForm() {
        binding.editNewName.text?.clear()
        binding.editNewEmail.text?.clear()
        binding.editNewAddress.text?.clear()
        binding.editNewPhoneNumber.text?.clear()
        binding.spinnerRole.setSelection(1)
        binding.spinnerGender.setSelection(0)
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

        // Set default selections
        binding.spinnerRole.setSelection(1) // Default: User
        binding.spinnerGender.setSelection(0) // Default: Nam
    }
}