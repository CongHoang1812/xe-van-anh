package com.example.authenticateuserandpass.ui.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.user.User
import com.example.authenticateuserandpass.data.repository.user.UserRepositoryImpl
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.databinding.FragmentDriverSelectionBottomSheetBinding
import com.example.authenticateuserandpass.ui.a_admin_ui.trip.addTrip.DriverSelectionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class DriverSelectionBottomSheet(
    private val onDriverSelected: (User) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentDriverSelectionBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var driverAdapter: DriverSelectionAdapter
    private val userRepository = UserRepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDriverSelectionBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadMainDrivers()
    }

    private fun setupRecyclerView() {
        driverAdapter = DriverSelectionAdapter { selectedDriver ->
            onDriverSelected(selectedDriver)
            dismiss()
        }

        binding.rvDrivers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = driverAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnConfirm.setOnClickListener {
            val selectedDriver = driverAdapter.getSelectedDriver()
            if (selectedDriver != null) {
                onDriverSelected(selectedDriver)
                dismiss()
            } else {
                Toast.makeText(context, "Vui lòng chọn tài xế", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMainDrivers() {
        Log.d("DriverSelection", "Bắt đầu load main drivers từ UserRepository...")

        lifecycleScope.launch {
            userRepository.getAllMainDriver(object : ResultCallback<Result<List<User>>> {
                override fun onResult(result: Result<List<User>>) {
                    Log.d("DriverSelection", "Nhận kết quả từ UserRepository")

                    when (result) {
                        is Result.Success -> {
                            Log.d("DriverSelection", "Success - Số lượng tài xế: ${result.data.size}")
                            result.data.forEachIndexed { index, driver ->
                                Log.d("DriverSelection", "Driver $index: ${driver.name}, role: ${driver.role}")
                            }

                            driverAdapter.updateDrivers(result.data)
                            Log.d("DriverSelection", "Đã gọi updateDrivers() với ${result.data.size} tài xế")
                        }
                        is Result.Error -> {
                            Log.e("DriverSelection", "Lỗi load danh sách tài xế: ${result.error.message}")
                            Toast.makeText(context, "Không thể tải danh sách tài xế", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}