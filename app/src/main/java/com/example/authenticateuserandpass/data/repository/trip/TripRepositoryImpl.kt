package com.example.authenticateuserandpass.data.repository.trip

import android.util.Log
import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.UserTicket
import com.example.authenticateuserandpass.data.model.trip.MainDriverTripInfo
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.model.trip.TripList
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.data.source.remote.RemoteTripDataSource
import com.example.authenticateuserandpass.data.source.remote.TripDetails
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TripRepositoryImpl : TripRepository.Remote {
    private val remoteTripDataSource = RemoteTripDataSource()
    private val database = FirebaseDatabase.getInstance()
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
    override suspend fun getUserTickets(
        userId: String,
        callback: ResultCallback<Result<List<UserTicket>>>
    ) {
        remoteTripDataSource.getUserTickets(userId, callback)
    }

    override suspend fun addTrip(
        trip: Trip,
        callback: ResultCallback<Result<String>>
    ) {
        remoteTripDataSource.addTrip(trip, callback)
    }


}