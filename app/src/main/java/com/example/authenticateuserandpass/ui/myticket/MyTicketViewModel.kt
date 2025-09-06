package com.example.authenticateuserandpass.ui.myticket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.UserTicket
import com.example.authenticateuserandpass.data.repository.trip.TripRepository
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.data.source.Result

import kotlinx.coroutines.launch
class MyTicketViewModel(
    private val tripRepository: TripRepositoryImpl
) : ViewModel() {

    private val _tickets = MutableLiveData<List<UserTicket>>()
    val tickets: LiveData<List<UserTicket>> = _tickets

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUserTickets(userId: String) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                tripRepository.getUserTickets(userId, object : ResultCallback<Result<List<UserTicket>>> {
                    override fun onResult(result: com.example.authenticateuserandpass.data.source.Result<List<UserTicket>>) {
                        _loading.value = false
                        when (result) {
                            is com.example.authenticateuserandpass.data.source.Result.Success -> {
                                _tickets.value = result.data
                            }
                            is Result.Error -> {
                                _error.value = result.error.message ?: "Đã xảy ra lỗi"
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                _loading.value = false
                _error.value = "Lỗi kết nối: ${e.message}"
            }
        }
    }

    fun refreshTickets(userId: String) {
        loadUserTickets(userId)
    }

    fun clearError() {
        _error.value = null
    }

    class MyTicketViewModelFactory(
        private val tripRepository: TripRepositoryImpl
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyTicketViewModel::class.java)) {
                return MyTicketViewModel(tripRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

