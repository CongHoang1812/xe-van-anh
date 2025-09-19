package com.example.authenticateuserandpass.ui.a_admin_ui.ticket

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.databinding.ActivityAssignDriverBinding
import com.example.authenticateuserandpass.ui.dialog.DriverSelectionBottomSheet
import com.google.firebase.firestore.FirebaseFirestore

class AssignDriverActivity : AppCompatActivity(), MenuProvider {

    private lateinit var binding: ActivityAssignDriverBinding

    private var selectedPickupDriver: User? = null
    private var selectedDropoffDriver: User? = null

    // biến cờ để biết đang chọn tài xế cho phần nào
    private var selectingPickup = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAssignDriverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbarAssignDriver)
        supportActionBar?.title = "Gán tài xế"

        val menuHost: MenuHost = this
        menuHost.addMenuProvider(this, this, androidx.lifecycle.Lifecycle.State.RESUMED)

        // chọn driver cho pickup
        binding.textPickup.setOnClickListener {
            selectingPickup = true
            showDriverSelectionBottomSheet()
        }

        // chọn driver cho dropoff
        binding.textDropoff.setOnClickListener {
            selectingPickup = false
            showDriverSelectionBottomSheet()
        }

        // cập nhật Firestore khi bấm nút
        binding.btnUpdateDriver.setOnClickListener {
            updateTripDrivers()
        }
    }

    private fun updateTripDrivers() {
        val db = FirebaseFirestore.getInstance()
        val bookingId = intent.getStringExtra("bookingId") ?: return

        val updates = mutableMapOf<String, Any>()
        selectedPickupDriver?.let { updates["pickup_driver_id"] = it.uid }
        selectedDropoffDriver?.let { updates["dropoff_driver_id"] = it.uid }

        if (updates.isEmpty()) {
            Toast.makeText(this, "Chưa chọn tài xế nào", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("bookings").document(bookingId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật tài xế thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDriverSelectionBottomSheet() {
        val driverSelectionBottomSheet = DriverSelectionBottomSheet(DriverSelectionBottomSheet.DriverType.SHUTTLE) { selectedDriver ->
            onDriverSelected(selectedDriver)
        }
        driverSelectionBottomSheet.show(supportFragmentManager, "DriverSelectionBottomSheet")
    }

    private fun onDriverSelected(driver: User) {
        if (selectingPickup) {
            selectedPickupDriver = driver
            binding.textPickup.setText(driver.name)


        } else {
            selectedDropoffDriver = driver
            binding.textDropoff.setText(driver.name)
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        // nếu cần thêm menu thì implement ở đây
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}