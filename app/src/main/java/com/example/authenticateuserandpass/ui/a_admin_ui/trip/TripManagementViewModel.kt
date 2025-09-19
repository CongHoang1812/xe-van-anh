package com.example.authenticateuserandpass.ui.a_admin_ui.trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.model.trip.TripList
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.authenticateuserandpass.data.source.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.set

class TripManagementViewModel : ViewModel() {

    private val tripRepository = TripRepositoryImpl()

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadTrips(origin: String, destination: String, date: String) {
        _isLoading.value = true
        _error.value = ""

        viewModelScope.launch {
            tripRepository.loadTrips(origin, destination, date, object : ResultCallback<Result<TripList>> {
                override fun onResult(result: Result<TripList>) {
                    _isLoading.postValue(false)
                    when (result) {
                        is Result.Success -> {
                            _trips.postValue(result.data.trips)
                            _error.postValue("")
                        }
                        is Result.Error -> {
                            _error.postValue(result.error.message ?: "Không thể tải danh sách chuyến đi")
                            _trips.postValue(emptyList())
                        }
                    }
                }
            })
        }

    }

    fun deleteTrip(trip: Trip, origin: String, destination: String, date: String) {
        _isLoading.value = true
        val tripId = trip.id
        val db = FirebaseFirestore.getInstance()
        db.collection("trip").document(tripId)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                // Reload trips after deletion
                loadTrips(origin, destination, date)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "Failed to delete trip: ${e.message}"
            }
    }

    fun loadAllTrips(date: String) {
        _isLoading.value = true
        _error.value = ""

        viewModelScope.launch {
            tripRepository.loadTrips("", "", date, object : ResultCallback<Result<TripList>> {
                override fun onResult(result: Result<TripList>) {
                    _isLoading.postValue(false)
                    when (result) {
                        is Result.Success -> {
                            _trips.postValue(result.data.trips)
                            _error.postValue("")
                        }
                        is Result.Error -> {
                            _error.postValue(result.error.message ?: "Không thể tải danh sách chuyến đi")
                            _trips.postValue(emptyList())
                        }
                    }
                }
            })
        }
    }


    fun updateTrip(trip: Trip, origin: String, destination: String, date: String) {
        _isLoading.value = true
        val tripId = trip.id
        val db = FirebaseFirestore.getInstance()
        db.collection("trip").document(tripId)
            .set(trip)
            .addOnSuccessListener {
                _isLoading.value = false
                // Reload trips with provided params
                loadTrips(origin, destination, date)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "Failed to update trip: ${e.message}"
            }
    }



    fun clearError() {
        _error.value = ""
    }

    fun refreshTrips(origin: String = "", destination: String = "", date: String) {
        if (origin.isEmpty() && destination.isEmpty()) {
            loadAllTrips(date)
        } else {
            loadTrips(origin, destination, date)
        }
    }
}