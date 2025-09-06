package com.example.authenticateuserandpass.ui.dialog


import android.os.Bundle
import androidx.fragment.app.Fragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.bus.Bus
import com.example.authenticateuserandpass.data.source.remote.BusDataSource
import com.example.authenticateuserandpass.databinding.FragmentBusSelectionBottomSheetBinding
import com.example.authenticateuserandpass.ui.a_admin_ui.trip.addTrip.BusSelectionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.authenticateuserandpass.data.source.Result

class BusSelectionBottomSheet(
    private val onBusSelected: (Bus) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentBusSelectionBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var busAdapter: BusSelectionAdapter
    private val busDataSource = BusDataSource()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusSelectionBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupConfirmButton()
        loadBuses()
    }

    private fun setupRecyclerView() {
        busAdapter = BusSelectionAdapter { selectedBus ->
            //binding.btnConfirm.isEnabled = true
            Log.d("BusSelection", "Đã chọn xe: ${selectedBus.license_plate}")
        }

        binding.rvBuses.apply {
            adapter = busAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupConfirmButton() {
        binding.btnConfirmBus.setOnClickListener {
            val selectedBus = busAdapter.getSelectedBus()
            if (selectedBus != null) {
                onBusSelected(selectedBus)
                dismiss()
            } else {
                Toast.makeText(context, "Vui lòng chọn xe", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBuses() {
        busDataSource.getAllBuses(object : ResultCallback<Result<List<Bus>>> {
            override fun onResult(result: Result<List<Bus>>) {
                when (result) {
                    is Result.Success -> {
                        busAdapter.updateBuses(result.data)
                        Log.d("BusSelection", "Đã load ${result.data.size} xe")
                    }
                    is Result.Error -> {
                        Log.e("BusSelection", "Lỗi load danh sách xe: ${result.error.message}")
                        Toast.makeText(context, "Không thể tải danh sách xe", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}