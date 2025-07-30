package com.example.authenticateuserandpass.ui.a_main_driver_ui.journey

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.ResultCallback
import com.example.authenticateuserandpass.data.firebaseModel.Passenger
import com.example.authenticateuserandpass.data.repository.trip.TripRepository
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.data.repository.user.PassengerRepository
import com.example.authenticateuserandpass.data.repository.user.PassengerRepositoryImpl
import com.example.authenticateuserandpass.data.source.remote.TripDetails
import com.example.authenticateuserandpass.ui.a_main_driver_ui.viewmodel.TripDetailsUI
import kotlinx.coroutines.launch
import com.example.authenticateuserandpass.data.source.Result
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PassengerListViewModel(
    private val tripRepository: TripRepositoryImpl
) : ViewModel() {
    private val repository: PassengerRepository = PassengerRepositoryImpl()


    // LiveData cho danh sách hành khách
    private val _passengers = MutableLiveData<List<Passenger>>()
    val passengers: LiveData<List<Passenger>> = _passengers

    // LiveData cho thông tin chuyến đi
    private val _tripDetails = MutableLiveData<TripDetailsUI>()
    val tripDetails: LiveData<TripDetailsUI> = _tripDetails

    // Trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Thông báo lỗi
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadPassengers(tripId: String) {
        _isLoading.value = true

        viewModelScope.launch {
            // Lấy thông tin chuyến đi
            tripRepository.getTripDetails(tripId, object : ResultCallback<Result<TripDetails>> {
                override fun onResult(result: Result<TripDetails>) {
                    when (result) {
                        is Result.Success -> {
                            val details = result.data
                            _tripDetails.value = TripDetailsUI(
                                id = details.trip.id,
                                origin = details.route.origin,
                                destination = details.route.destination,
                                departureTime = details.trip.departure_time,
                                arrivalTime = calculateArrivalTime(details.trip.departure_time, details.trip.duration),
                                availableSeats = details.trip.availableSeats,
                                totalSeats = details.bus.seat_count,
                                stops = 28, // TODO: Lấy số điểm dừng thực tế từ dữ liệu
                                price = details.trip.ticket_price,
                                duration = details.trip.duration,
                                distance = details.trip.distance,
                                isToday = isTodayTrip(details.trip.trip_date)
                            )

                            // Lấy danh sách hành khách
                            loadPassengersFromTrip(tripId)
                        }
                        is Result.Error -> {
                            _error.value = "Không thể tải thông tin chuyến đi: ${result.error.message}"
                            _isLoading.value = false
                        }
                    }
                }
            })
        }
    }

    private fun loadPassengersFromTrip(tripId: String) {
        viewModelScope.launch {
            repository.getPassengersByTripId(tripId, object : ResultCallback<Result<List<Passenger>>> {
                override fun onResult(result: Result<List<Passenger>>) {
                    _isLoading.value = false
                    when (result) {
                        is Result.Success -> {
                            _passengers.value = result.data
                        }
                        is Result.Error -> {
                            _error.value = "Không thể tải danh sách hành khách: ${result.error.message}"
                        }
                    }
                }
            })
        }
    }

    private fun calculateArrivalTime(departureTime: String, duration: String): String {
        try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val departure = timeFormat.parse(departureTime) ?: return ""

            // Giả sử duration là chuỗi phút, ví dụ: "120" nghĩa là 2 giờ
            val durationMinutes = duration.toIntOrNull() ?: 0

            val calendar = Calendar.getInstance()
            calendar.time = departure
            calendar.add(Calendar.MINUTE, durationMinutes)

            return timeFormat.format(calendar.time)
        } catch (e: Exception) {
            return ""
        }
    }

    private fun isTodayTrip(tripDate: String): Boolean {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val todayStr = dateFormat.format(Calendar.getInstance().time)
            return tripDate == todayStr
        } catch (e: Exception) {
            return false
        }
    }
}