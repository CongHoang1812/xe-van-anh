package com.example.authenticateuserandpass.ui.a_main_driver_ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.repository.booking.BookingRepository
import kotlinx.coroutines.launch

class BookingViewModel : ViewModel() {

    private val repository = BookingRepository()

    // LiveData để quan sát trạng thái cập nhật
    private val _updateStatus = MutableLiveData<Result<Unit>>()
    val updateStatus: LiveData<Result<Unit>> = _updateStatus

    /**
     * Cập nhật tất cả booking của tripId sang "Đã đi"
     */
    fun markAllBookingsAsCompleted(tripId: String?) {
        viewModelScope.launch {
            val result = repository.markAllAsCompleted(tripId)
            _updateStatus.value = result
        }
    }
}