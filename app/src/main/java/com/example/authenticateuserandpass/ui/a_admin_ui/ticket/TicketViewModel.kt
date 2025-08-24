package com.example.authenticateuserandpass.ui.a_admin_ui.ticket

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.source.remote.TicketRepository
import com.example.authenticateuserandpass.data.source.remote.TripDetails
import kotlinx.coroutines.launch

class TicketViewModel : ViewModel() {
    private val repository = TicketRepository()

    private val _allTickets = MutableLiveData<List<TripDetails>>()
    val allTickets: LiveData<List<TripDetails>> = _allTickets

    private val _filteredTickets = MutableLiveData<List<TripDetails>>()
    val filteredTickets: LiveData<List<TripDetails>> = _filteredTickets

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _selectedDate = MutableLiveData<String?>()
    val selectedDate: LiveData<String?> = _selectedDate

    fun loadAllTickets() {
        viewModelScope.launch {
            _isLoading.value = true
            val tickets = repository.getAllBookingsWithDetails()
            _allTickets.value = tickets
            _filteredTickets.value = tickets
            _isLoading.value = false
        }
    }

    fun loadTicketsByDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedDate.value = date
            val tickets = repository.getBookingsByDate(date)
            _filteredTickets.value = tickets
            _isLoading.value = false
        }
    }

    fun clearDateFilter() {
        _selectedDate.value = null
        _filteredTickets.value = _allTickets.value
    }

    fun searchTickets(query: String?) {
        val allTickets = _allTickets.value ?: emptyList()
        val filtered = if (query.isNullOrBlank()) {
            allTickets
        } else {
            allTickets.filter { tripDetails ->
                tripDetails.booking.id.contains(query, ignoreCase = true) ||
                        tripDetails.user.name.contains(query, ignoreCase = true) ||
                        tripDetails.user.phone.contains(query, ignoreCase = true)
            }
        }
        _filteredTickets.value = filtered
    }

    fun deleteTicket(tripDetails: TripDetails) {
        viewModelScope.launch {
            val success = repository.deleteBooking(tripDetails.booking.id)
            if (success) {
                val currentTickets = _allTickets.value?.toMutableList()
                currentTickets?.remove(tripDetails)
                _allTickets.value = currentTickets?: emptyList()

                val currentFiltered = _filteredTickets.value?.toMutableList()
                currentFiltered?.remove(tripDetails)
                _filteredTickets.value = currentFiltered?: emptyList()
            }
        }
    }
    fun updatePaymentStatus(bookingId: String, newStatus: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.updatePaymentStatus(bookingId, newStatus)
                callback(success)
            } catch (e: Exception) {
                Log.e("TicketViewModel", "Error updating payment status: ${e.message}")
                callback(false)
            }
        }
    }
}