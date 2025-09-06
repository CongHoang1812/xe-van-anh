package com.example.authenticateuserandpass.data.source.remote

import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.UserTicket
import com.example.authenticateuserandpass.data.model.bus.Bus
import com.example.authenticateuserandpass.data.model.route.Route
import com.example.authenticateuserandpass.data.model.trip.MainDriverTripInfo
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.model.trip.TripList
import com.example.authenticateuserandpass.data.source.Result

interface TripDataSource {
    interface Local {
        //val trip: List<Trip>


    }
//    interface Remote{
//        suspend fun loadTrip(callback: ResultCallback<Result<TripList>>)
//    }

    interface Remote{
        suspend fun loadTrip(
            origin: String,
            destination: String,
            date: String,
            callback: ResultCallback<Result<TripList>>
        )

        suspend fun getUpComingTripInfoMainDriver(
            mainDriverId :String,
            callback: ResultCallback<Result<MainDriverTripInfo>>
        )

        suspend fun getTripByDate(date: String, callback: ResultCallback<Result<List<Trip>>>)
        suspend fun getTripDetails(tripId: String, callback: ResultCallback<Result<TripDetails>>)
        suspend fun getUserTickets(
            userId: String,
            callback: ResultCallback<Result<List<UserTicket>>>
        )
        suspend fun addTrip(trip: Trip, callback: ResultCallback<Result<String>>)
    }

}

