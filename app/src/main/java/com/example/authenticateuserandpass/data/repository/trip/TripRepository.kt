package com.example.authenticateuserandpass.data.repository.trip

import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.UserTicket
import com.example.authenticateuserandpass.data.model.trip.MainDriverTripInfo
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.model.trip.TripList
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.data.source.remote.TripDetails

interface TripRepository {
    interface Local{

    }
    interface Remote {
        suspend fun loadTrips(origin: String, destination: String, date: String,callback: ResultCallback<Result<TripList>>)
        suspend fun getUpComingTripInfoMainDriver(mainDriverId: String, callback: ResultCallback<Result<MainDriverTripInfo>>)
        suspend fun getTripsByDate(date: String, callback: ResultCallback<Result<List<Trip>>>)
        suspend fun getTripDetails(tripId: String, callback: ResultCallback<Result<TripDetails>>)
        suspend fun getUserTickets(userId: String, callback: ResultCallback<Result<List<UserTicket>>>)
        suspend fun addTrip(trip: Trip, callback: ResultCallback<Result<String>>)
    }
}