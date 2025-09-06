package com.example.authenticateuserandpass.ui.a_main_driver_ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.trip.MainDriverTripInfo
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.data.source.remote.TripDetails
import com.example.authenticateuserandpass.ui.findticket.FindTicketViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripViewModel(
    private val tripRepository: TripRepositoryImpl
) : ViewModel() {
    private val _nextTrip = MutableLiveData<MainDriverTripInfo>()
    val nextTrip: LiveData<MainDriverTripInfo> = _nextTrip

    // LiveData cho danh sách chuyến đi hôm nay
    private val _todayTrips = MutableLiveData<List<TripDetailsUI>>()
    val todayTrips: LiveData<List<TripDetailsUI>> = _todayTrips

    // LiveData cho danh sách chuyến đi ngày mai
    private val _tomorrowTrips = MutableLiveData<List<TripDetailsUI>>()
    val tomorrowTrips: LiveData<List<TripDetailsUI>> = _tomorrowTrips

    // Trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Thông báo lỗi
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error



    fun fetchTrips() {
        _isLoading.value = true

        // Format ngày hôm nay và ngày mai
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val todayStr = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val tomorrowStr = dateFormat.format(calendar.time)

        viewModelScope.launch {
            // Lấy chuyến đi hôm nay
            tripRepository.getTripsByDate(todayStr, object : ResultCallback<Result<List<Trip>>> {
                override fun onResult(result: Result<List<Trip>>) {
                    when (result) {
                        is Result.Success -> {
                            Log.d("TripViewModel", "Số lượng trip trả về: ${result.data.size} cho ngày hôm nay")
                            fetchTripDetails(result.data, true)
                        }

                        is Result.Error -> {
                            _error.value =
                                "Không thể tải chuyến đi hôm nay: ${result.error.message}"
                            _isLoading.value = false
                        }
                    }
                }

            })

            // Lấy chuyến đi ngày mai
            tripRepository.getTripsByDate(tomorrowStr, object : ResultCallback<Result<List<Trip>>> {
                override fun onResult(result: Result<List<Trip>>) {
                    when (result) {
                        is Result.Success -> {
                            fetchTripDetails(result.data, false)
                        }
                        is Result.Error -> {
                            _error.value = "Không thể tải chuyến đi ngày mai: ${result.error.message}"
                            _isLoading.value = false
                        }
                    }
                }


            })
        }
    }

    private fun fetchTripDetails(trips: List<Trip>, isToday: Boolean) {
        Log.d("TripViewModel", "Bắt đầu fetchTripDetails với ${trips.size} chuyến")
        if (trips.isEmpty()) {
            if (isToday) {
                _todayTrips.value = emptyList()
            } else {
                _tomorrowTrips.value = emptyList()
            }
            _isLoading.value = false
            return
        }

        val tripDetailsUIList = mutableListOf<TripDetailsUI>()
        var completedCount = 0

        viewModelScope.launch {
            for (trip in trips) {
                Log.d("TripViewModel", "Gọi getTripDetails với tripId=${trip.id}")
                tripRepository.getTripDetails(trip.id, object : ResultCallback<Result<TripDetails>> {

                    override fun onResult(result: Result<TripDetails>) {
                        when (result) {
                            is Result.Success -> {
                                val details = result.data
                                tripDetailsUIList.add(
                                    TripDetailsUI(
                                        id = details.trip.id,
                                        origin = details.route.origin,
                                        destination = details.route.destination,
                                        departureTime = details.trip.departure_time,
                                        arrivalTime = calculateArrivalTime(details.trip.departure_time, details.trip.duration),
                                        availableSeats = 24 -  details.trip.availableSeats,
                                        totalSeats = details.bus.seat_count,
                                        stops = 28, // TODO: Lấy số điểm dừng thực tế từ dữ liệu
                                        price = details.trip.ticket_price,
                                        duration = details.trip.duration,
                                        distance = details.trip.distance,
                                        isToday = isToday
                                    )
                                )

                                completedCount++
                                Log.d("TripViewModel", "Đã xử lý $completedCount chuyến")
                                if (completedCount == trips.size) {
                                    // Sắp xếp danh sách theo thời gian khởi hành
                                    val sortedList = tripDetailsUIList.sortedBy { it.departureTime }
                                    if (isToday) {
                                        _todayTrips.value = sortedList
                                    } else {
                                        _tomorrowTrips.value = sortedList
                                    }
                                    _isLoading.value = false
                                }
                            }
                            is Result.Error -> {
                                completedCount++
                                if (completedCount == trips.size) {
                                    if (isToday) {
                                        _todayTrips.value = tripDetailsUIList
                                    } else {
                                        _tomorrowTrips.value = tripDetailsUIList
                                    }
                                    _isLoading.value = false
                                }
                            }
                        }
                    }


                })
            }
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

    fun getUpComingTripInfoMainDriver(mainDriverId: String){
        viewModelScope.launch(Dispatchers.IO) {
            val callback = object  : ResultCallback<Result<MainDriverTripInfo>>{
                override fun onResult(result: Result<MainDriverTripInfo>) {
                    when(result) {
                        is Result.Success -> {
                            _nextTrip.postValue(result.data)
                            Log.d("TripViewModel", "🚀 Đã nhận được tripInfo: ${result.data}")
                        }

                        is Result.Error -> {
                            // Xử lý lỗi
                            Log.e("TripViewModel", "Lỗi khi tải chuyến sắp tới", result.error)
                        }
                    }
                }

            }
            tripRepository.getUpComingTripInfoMainDriver(mainDriverId, callback)

        }
    }

    class Factory(
        private val tripRepository: TripRepositoryImpl
    ): ViewModelProvider.Factory{
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(TripViewModel::class.java)){
                return TripViewModel(tripRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")

        }
    }
//    fun getUpComingTripInfoMainDriver(mainDriverId: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            tripRepository.getUpcomingTripInfoForMainDriver(mainDriverId, object : ResultCallback<Result<MainDriverTripInfo>> {
//                override fun onResult(result: Result<MainDriverTripInfo>) {
//                    when (result) {
//                        is Result.Success -> _nextTrip.postValue(result.data)
//                        is Result.Error -> {
//                            // bạn có thể tạo thêm LiveData cho lỗi nếu cần
//                            Log.e("TripViewModel", "Lỗi khi tải chuyến sắp tới", result.error)
//                        }
//                    }
//                }
//            })
//        }
//    }
}

data class TripDetailsUI(
    val id: String,
    val origin: String,
    val destination: String,
    val departureTime: String,
    val arrivalTime: String,
    val availableSeats: Int,
    val totalSeats: Int,
    val stops: Int,
    val price: String,
    val duration: String,
    val distance: String,
    val isToday: Boolean
)