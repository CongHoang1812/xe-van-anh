package com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.repository.user.UserRepositoryImpl
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.databinding.ActivityLoginBinding
import com.example.authenticateuserandpass.ui.a_admin_ui.HomeAdminActivity
import com.example.authenticateuserandpass.ui.a_main_driver_ui.home.HomeMainDriverActivity
import com.example.authenticateuserandpass.ui.a_shuttle_driver_ui.home.HomeShuttleDriverActivity
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setUpViewModel()
        setupListeners()
        observeViewModel()
    }

    private fun setUpViewModel() {
        val userRepository = UserRepositoryImpl()
        viewModel = ViewModelProvider(this, Factory(userRepository))[LoginViewModel::class.java]
        Log.d("LoginActivity", "ViewModel initialized")
    }
    private fun setupListeners(){
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            Log.d("LoginActivity", "Login button clicked with email=$email")
            loginWithEmailPassword(email, password)
        }
    }
    private fun loginWithEmailPassword(email: String, password: String) {
        Log.d("LoginActivity", "Attempting login with FirebaseAuth")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                Log.d("LoginActivity", "Login successful. UID = $userId")
                if(userId != null){
                    viewModel.fetchUserRole(userId)
                }else{
                    Log.e("LoginActivity", "UserId is null after login")
                    showToast("UserId is null")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LoginActivity", "Login failed: ${exception.message}", exception)
                showToast("Login failed: ${exception.message}")
            }
    }
    private fun observeViewModel() {
        viewModel.user.observe(this) { result ->
            Log.d("LoginActivity", "Observer triggered: $result")
            when (result) {
                is Result.Success -> {
                    val role = result.data.role
                    Log.d("LoginActivity", "User role fetched: $role")
                    navigateToRoleScreen(role)
                }
                is Result.Error -> {
                    Log.e("LoginActivity", "Error fetching user role", result.error)
                    showToast("Lỗi: ${result.error.message}")
                }
            }
        }
    }
    private fun navigateToRoleScreen(role: String?) {
        when (role) {
            "admin" -> startActivity(Intent(this, HomeAdminActivity::class.java))
            "shuttle_driver" -> startActivity(Intent(this, HomeShuttleDriverActivity::class.java))
            "main_driver" -> startActivity(Intent(this, HomeMainDriverActivity::class.java))
            else -> {
                showToast("Role không hợp lệ hoặc chưa được phân quyền.")
                Log.w("LoginActivity", "Invalid or missing role")
            }
        }
        finish()
    }
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}