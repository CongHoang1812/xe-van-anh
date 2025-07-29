package com.example.authenticateuserandpass.ui.account.updateProfile

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.databinding.ActivityUpdateProfileBinding
import java.io.File
import java.util.Calendar
import android.Manifest
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class UpdateProfileActivity : AppCompatActivity(), MenuProvider{
    private  var user : User = User()
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentPhotoUri : Uri
    private lateinit var binding: ActivityUpdateProfileBinding

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Quyền đã được cấp, tiếp tục mở camera hoặc thư viện
            openGallery() // hoặc openCamera()
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền để tiếp tục", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền CAMERA", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val menuHost: MenuHost = this
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    binding.imageAvatar.setImageURI(it)
                    uploadAvatarAndSaveUser(it)
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                binding.imageAvatar.setImageURI(currentPhotoUri)
                user.avatarUrl = currentPhotoUri.toString()
            }
        }
        setSpinner()
        setupListener()
        loadUserData()

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

    private fun setupListener(){
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.editDateOfBirth.setOnClickListener {
            showDatePicker(binding.editDateOfBirth)
        }
        binding.btnUpdateAvatar.setOnClickListener {
            showImagePickerDialog()
        }
        binding.btnUpdateAccount.setOnClickListener {
            collectUserInput()
            saveUserToFirestore()
        }

    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            editText.context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate =
                    "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
                editText.setText(selectedDate)
            },
            year, month, day
        )
        datePicker.show()
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Chọn từ thư viện", "Chụp ảnh", "Đóng")

        AlertDialog.Builder(this)
            .setTitle("Chọn ảnh đại diện")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkPermissionAndOpenGallery()
                    1 -> checkPermissionAndOpenCamera()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val imageFile = File.createTempFile("avatar_", ".jpg", cacheDir)
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            imageFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
        cameraLauncher.launch(intent)
    }
    fun checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            openGallery() // Android < 13 không cần xin quyền mới
        }
    }
    private fun checkPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun uploadAvatarAndSaveUser(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val avatarRef = storageRef.child("avatars/${UUID.randomUUID()}.jpg")

        val uploadTask = avatarRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            avatarRef.downloadUrl.addOnSuccessListener { downloadUri ->
                user.avatarUrl = downloadUri.toString()
                saveUserToFirestore()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi upload ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserToFirestore() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show()
            }
    }
    private fun collectUserInput() {
        user.name = binding.editFullName.text.toString()
        user.email = binding.editEmail.text.toString()
        user.phone = binding.editPhoneNumber.text.toString()
        user.birthDate = binding.editDateOfBirth.text.toString()
        user.gender = binding.spinnerGender.selectedItem.toString()
        user.address = binding.editAddress.text.toString()
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

//    private fun saveUserToFirestore() {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        FirebaseFirestore.getInstance()
//            .collection("users")
//            .document(uid)
//            .set(user)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
//                finish() // hoặc quay lại màn trước
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Lỗi lưu dữ liệu", Toast.LENGTH_SHORT).show()
//            }
//    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_menu1, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_account -> {
                finish()
                true
            }
            else -> false
        }
    }
}