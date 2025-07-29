package com.example.authenticateuserandpass.data.repository.trip

import android.util.Log
import com.example.authenticateuserandpass.ResultCallback
import com.example.authenticateuserandpass.data.model.trip.MainDriverTripInfo
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.model.trip.TripList
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.data.source.remote.RemoteTripDataSource
import com.example.authenticateuserandpass.data.source.remote.TripDetails

class TripRepositoryImpl : TripRepository.Remote {
    private val remoteTripDataSource = RemoteTripDataSource()
    override suspend fun loadTrips(
        origin: String,
        destination: String,
        date: String,
        callback: ResultCallback<Result<TripList>>
    ) {
        remoteTripDataSource.loadTrip(origin, destination, date, callback)
    }

    override suspend fun getUpComingTripInfoMainDriver(
        mainDriverId: String,
        callback: ResultCallback<Result<MainDriverTripInfo>>
    ) {
        remoteTripDataSource.getUpComingTripInfoMainDriver(mainDriverId, callback)
        Log.d("TripRepository", "Đang lấy chuyến kế tiếp cho mainDriverId = $mainDriverId")
    }

    override suspend fun getTripsByDate(
        date: String,
        callback: ResultCallback<Result<List<Trip>>>
    ) {
        remoteTripDataSource.getTripByDate(date, callback)
    }

    override suspend fun getTripDetails(
        tripId: String,
        callback: ResultCallback<Result<TripDetails>>
    ) {
        remoteTripDataSource.getTripDetails(tripId, callback)
    }


}