package com.example.authenticateuserandpass.ui.a_admin_ui.bus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.authenticateuserandpass.R
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.bus.Bus
import com.example.authenticateuserandpass.data.source.remote.BusDataSource
import com.example.authenticateuserandpass.databinding.ActivityBusManagementBinding
import com.example.authenticateuserandpass.data.source.Result
import com.google.firebase.firestore.FirebaseFirestore


class BusManagementActivity : AppCompatActivity(), BusAdapter.OnBusMenuClickListener {
    private lateinit var binding: ActivityBusManagementBinding
    private lateinit var busAdapter: BusAdapter
    private val busList = mutableListOf<Bus>()
    private val db = FirebaseFirestore.getInstance()
    private val busDataSource = BusDataSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBusManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupToolbar()
        setupRecyclerView()
        loadBusesRealtime()

        binding.fabAddBus.setOnClickListener {
            Toast.makeText(this, "Thêm xe mới", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AddBusActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarBusManagement)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Quản lý xe"
        }
    }

    private fun setupRecyclerView() {
        busAdapter = BusAdapter(busList, this)
        binding.rvBus.apply {
            layoutManager = GridLayoutManager(this@BusManagementActivity, 2) // 2 cột
            adapter = busAdapter
        }
    }

    private fun loadBusesRealtime() {
        db.collection("buses")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("BusManagement", "Listen failed.", e)
                    return@addSnapshotListener
                }

                busList.clear()
                for (doc in snapshots!!) {
                    val bus = doc.toObject(Bus::class.java).copy(id = doc.id)
                    busList.add(bus)
                }
                busAdapter.notifyDataSetChanged()
            }
    }

    // Xử lý menu của mỗi bus
    override fun onView(bus: Bus) {
        Toast.makeText(this, "Xem xe: ${bus.type}", Toast.LENGTH_SHORT).show()
    }

    override fun onEdit(bus: Bus) {
        Toast.makeText(this, "Sửa xe: ${bus.type}", Toast.LENGTH_SHORT).show()
    }

    override fun onDelete(bus: Bus) {
        Toast.makeText(this, "Xoá xe: ${bus.type}", Toast.LENGTH_SHORT).show()
    }
}