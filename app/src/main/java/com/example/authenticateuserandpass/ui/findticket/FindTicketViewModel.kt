package com.example.authenticateuserandpass.ui.findticket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.authenticateuserandpass.ResultCallback
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.model.trip.TripList
import com.example.authenticateuserandpass.data.repository.trip.TripRepositoryImpl
import com.example.authenticateuserandpass.data.source.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FindTicketViewModel(
    private val tripRepository: TripRepositoryImpl
) : ViewModel(){
    private val _tickets = MutableLiveData<List<Trip>>()
    val tickets : LiveData<List<Trip>> = _tickets

//    init {
//        loadTrips()
//    }
 fun loadTrips(origin: String, destination: String, tripDate: String){
        viewModelScope.launch(Dispatchers.IO) {
            val callback = object : ResultCallback<Result<TripList>>{
                override fun onResult(result: Result<TripList>) {
                    if(result is Result.Success){
                        val tickets = result.data.trips
                        _tickets.postValue(tickets)

                    }
                }
            }
            tripRepository.loadTrips(origin, destination, tripDate,callback)

        }
    }

    class Factory(
        private val songRepository: TripRepositoryImpl
    ): ViewModelProvider.Factory{
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(FindTicketViewModel::class.java)){
                return FindTicketViewModel(songRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")

        }
    }

}