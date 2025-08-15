package com.example.authenticateuserandpass.data.source.remote

import android.util.Log
import com.example.authenticateuserandpass.ResultCallback
import com.example.authenticateuserandpass.data.model.UserTicket
import com.example.authenticateuserandpass.data.model.bus.Bus
import com.example.authenticateuserandpass.data.model.route.Route
import com.example.authenticateuserandpass.data.model.trip.MainDriverTripInfo
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.model.trip.TripList
import com.example.authenticateuserandpass.data.source.Result
import com.example.authenticateuserandpass.data.source.remote.TripDataSource
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.text.get

class RemoteTripDataSource : TripDataSource.Remote {
    private val db = FirebaseFirestore.getInstance()
    private val tripsCollection = db.collection("trip")
    private val routesCollection = db.collection("routes")
    private val bookingsCollection = db.collection("bookings")
    private val busesCollection = db.collection("buses")


    override suspend fun loadTrip(
        origin: String,
        destination: String,
        date: String,
        callback: ResultCallback<Result<TripList>>
    ) {
//        val db = Firebase.firestore
        Log.d("RemoteTripDataSource", "🔍 Tìm trip với: origin=$origin, destination=$destination, date=$date")

        db.collection("routes")
            .whereEqualTo("origin", origin)
            .whereEqualTo("destination", destination)
            .get()
            .addOnSuccessListener { routeSnapshots ->
                val routeIds = routeSnapshots.map { it.id }
                if (routeIds.isEmpty()) {
                    callback.onResult(Result.Success(TripList(emptyList())))
                    return@addOnSuccessListener
                }

                db.collection("trip")
                    //.whereIn("route_id", routeIds) // Tùy bạn bật nếu cần
                    .whereEqualTo("trip_date", date)
                    .get()
                    .addOnSuccessListener { tripSnapshots ->
                        val trips = mutableListOf<Trip>()

                        if (tripSnapshots.isEmpty) {
                            callback.onResult(Result.Success(TripList(emptyList())))
                            return@addOnSuccessListener
                        }

                        var loadedCount = 0
                        for (tripDoc in tripSnapshots.documents) {
                            val trip = tripDoc.toObject(Trip::class.java)
                            val tripId = tripDoc.id
                            trip?.id = tripId

                            if (trip != null) {
                                db.collection("bookings")
                                    .whereEqualTo("trip_id", tripId)
                                    .get()
                                    .addOnSuccessListener { bookingSnapshots ->
                                        val bookedSeats = bookingSnapshots.size()
                                        trip.availableSeats = 24 - bookedSeats
                                        trips.add(trip)

                                        db.collection("trip").document(tripId)
                                            .update("availableSeats", trip.availableSeats)
                                            .addOnSuccessListener {
                                                Log.d("RemoteTripDataSource", "Đã cập nhật availableSeats cho trip $tripId = ${trip.availableSeats}")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("RemoteTripDataSource", "Lỗi khi cập nhật availableSeats: ${e.message}")
                                            }

                                        loadedCount++
                                        if (loadedCount == tripSnapshots.size()) {
                                            // Đảm bảo đã xử lý hết các trip
                                            Log.d("RemoteTripDataSource", "✅ Đã xử lý ${trips.size} trip kèm số ghế")
                                            callback.onResult(Result.Success(TripList(trips)))
                                        }
                                    }
                                    .addOnFailureListener { ex ->
                                        callback.onResult(Result.Error(ex))
                                    }
                            } else {
                                loadedCount++
                                if (loadedCount == tripSnapshots.size()) {
                                    callback.onResult(Result.Success(TripList(trips)))
                                }
                            }
                        }
                    }
                    .addOnFailureListener { ex ->
                        callback.onResult(Result.Error(ex))
                    }
            }
            .addOnFailureListener { ex ->
                callback.onResult(Result.Error(ex))
            }
    }

    override suspend fun getUpComingTripInfoMainDriver(
        mainDriverId: String,
        callback: ResultCallback<Result<MainDriverTripInfo>>
    ) {
        db.collection("trip")
            .whereEqualTo("main_driver_id", mainDriverId)
            .whereEqualTo("status", "chưa đi")
            .get()
            .addOnSuccessListener { tripSnapshots ->

                val trips = tripSnapshots.toObjects(Trip::class.java)
                val sortedTrips = trips.sortedBy {
                    combineDateTime(it.trip_date, it.departure_time)
                }
                val nextTrip = sortedTrips.firstOrNull()

                if (nextTrip == null) {
                    callback.onResult(Result.Error(Exception("Không có chuyến xe sắp tới")))
                    return@addOnSuccessListener
                }

                val routeId = nextTrip.route_id
                Log.d("RemoteTripDataSource", "🚀 Đã nhận được routeId: $routeId")

                if (routeId.isNullOrBlank()) {
                    callback.onResult(Result.Error(Exception("route_id của chuyến xe không hợp lệ")))
                    return@addOnSuccessListener
                }

                db.collection("routes").document(routeId)
                    .get()
                    .addOnSuccessListener { routeDoc ->
                        Log.d("RemoteTripDataSource", "$routeDoc")
                        val route = routeDoc.toObject(Route::class.java)
                        val routeName = "${route?.origin} - ${route?.destination}"
                        Log.d("RemoteTripDataSource", "🚀 Đã nhận được routeName: $routeName")

                        db.collection("bookings")
                            .whereEqualTo("trip_id", nextTrip.id)
                            .get()
                            .addOnSuccessListener { bookingSnapshots ->
                                val passengerCount = bookingSnapshots.size()
                                val departureTime = combineDateTime(nextTrip.trip_date, nextTrip.departure_time)
                                val now = System.currentTimeMillis()
                                val hoursLeft = (departureTime - now) / (1000 * 60 * 60)

                                val info = MainDriverTripInfo(
                                    tripId = nextTrip.id,
                                    routeName = routeName,
                                    departureTime = "${nextTrip.trip_date} ${nextTrip.departure_time}",
                                    hoursLeft = hoursLeft,
                                    passengerCount = passengerCount,
                                    origin = route?.origin ?: "",
                                    destination = route?.destination ?: ""

                                )
                                callback.onResult(Result.Success(info))
                            }
                            .addOnFailureListener { ex ->
                                callback.onResult(Result.Error(ex))
                            }
                    }
                    .addOnFailureListener { ex ->
                        callback.onResult(Result.Error(ex))
                    }
            }
            .addOnFailureListener { ex ->
                callback.onResult(Result.Error(ex))
            }
    }

    override suspend fun getTripByDate(
        date: String,
        callback: ResultCallback<Result<List<Trip>>>
    ) {
        tripsCollection.whereEqualTo("trip_date", date)
            .get().addOnSuccessListener { documents ->
                val trips = documents.toObjects(Trip::class.java)
                // tinh so ghe con trong cho moi chuyen
                calculateAvailableSeats(trips) {
                    callback.onResult(Result.Success(trips))
                }
            }
            .addOnFailureListener { e ->
                callback.onResult(Result.Error(e))
            }
    }

    private fun calculateAvailableSeats(trips: List<Trip>, onComplete: () -> Unit) {
        if (trips.isEmpty()) {
            onComplete()
            return
        }

        var completedCount = 0
        for (trip in trips) {
            // Lấy thông tin xe buýt để biết tổng số ghế
            busesCollection.document(trip.bus_id)
                .get()
                .addOnSuccessListener { busDoc ->
                    val bus = busDoc.toObject(Bus::class.java)
                    val totalSeats = bus?.seat_count ?: 0

                    // Lấy số lượng đặt chỗ để tính ghế còn trống
                    db.collection("bookings")
                        .whereEqualTo("trip_id", trip.id)
                        //.whereIn("status", listOf("confirmed", "pending"))
                        .get()
                        .addOnSuccessListener { bookingDocs ->
                            val bookedSeats = bookingDocs.size()
                            trip.availableSeats = totalSeats - bookedSeats

                            completedCount++
                            if (completedCount == trips.size) {
                                onComplete()
                            }
                        }
                }
        }
    }

    override suspend fun getTripDetails(tripId: String, callback: ResultCallback<Result<TripDetails>>) {
        Log.d("RemoteTripDataSource", "Bắt đầu getTripDetails với tripId=$tripId")
        tripsCollection.document(tripId).get()
            .addOnSuccessListener { tripDoc ->
                val trip = tripDoc.toObject(Trip::class.java)
                if (trip != null) {
                    // Lấy thông tin tuyến đường
                    routesCollection.document(trip.route_id).get()
                        .addOnSuccessListener { routeDoc ->
                            val route = routeDoc.toObject(Route::class.java)

                            // Lấy thông tin xe buýt
                            busesCollection.document(trip.bus_id).get()
                                .addOnSuccessListener { busDoc ->
                                    val bus = busDoc.toObject(Bus::class.java)

                                    val tripDetails = TripDetails(
                                        trip = trip,
                                        route = route ?: Route(),
                                        bus = bus ?: Bus()
                                    )

                                    callback.onResult(Result.Success(tripDetails))
                                }
                        }
                } else {
                    callback.onResult(Result.Error(Exception("Trip not found")))
                }
            }
            .addOnFailureListener { e ->
                callback.onResult(Result.Error(e))
            }
    }

    override suspend fun getUserTickets(
        userId: String,
        callback: ResultCallback<Result<List<UserTicket>>>
    ) {
        Log.d("RemoteTripDataSource", "🎫 Lấy vé của user: $userId")

        bookingsCollection.whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { bookingSnapshots ->
                if (bookingSnapshots.isEmpty) {
                    callback.onResult(Result.Success(emptyList()))
                    return@addOnSuccessListener
                }

                val userTickets = mutableListOf<UserTicket>()
                var processedCount = 0

                for (bookingDoc in bookingSnapshots.documents) {
                    val bookingData = bookingDoc.data
                    val tripId = bookingData?.get("trip_id") as? String ?: ""
                    val bookingStatus = bookingData?.get("status") as? String ?: "Chưa đi"
                    val seatNumbers = bookingData?.get("seat_numbers") as? String ?: ""

                    // Lấy thông tin trip
                    tripsCollection.document(tripId)
                        .get()
                        .addOnSuccessListener { tripDoc ->
                            val trip = tripDoc.toObject(Trip::class.java)
                            trip?.let { tripInfo ->
                                // Lấy thông tin route
                                routesCollection.document(tripInfo.route_id)
                                    .get()
                                    .addOnSuccessListener { routeDoc ->
                                        val route = routeDoc.toObject(Route::class.java)

                                        // Lấy thông tin xe chính từ bảng buses
                                        busesCollection.document(tripInfo.bus_id)
                                            .get()
                                            .addOnSuccessListener { mainBusDoc ->
                                                val mainBus = mainBusDoc.toObject(Bus::class.java)

                                                // Lấy thông tin tài xế chính từ bảng drivers (nếu có)
                                                if (!tripInfo.main_driver_id.isNullOrEmpty()) {
                                                    db.collection("drivers").document(tripInfo.main_driver_id)
                                                        .get()
                                                        .addOnSuccessListener { driverDoc ->
                                                            val driverData = driverDoc.data
                                                            val mainDriverName = driverData?.get("name") as? String ?: ""
                                                            val mainDriverPhone = driverData?.get("phone") as? String ?: ""

                                                            // Lấy thông tin thanh toán
                                                            getPaymentStatusAndCreateTicket(
                                                                bookingDoc.id, bookingData, tripInfo, route,
                                                                mainBus, mainDriverName, mainDriverPhone,
                                                                mainBus?.id, tripInfo.main_driver_id,
                                                                userTickets, processedCount, bookingSnapshots.size(), callback
                                                            )
                                                        }
                                                        .addOnFailureListener {
                                                            // Tạo ticket mà không có thông tin tài xế
                                                            getPaymentStatusAndCreateTicket(
                                                                bookingDoc.id, bookingData, tripInfo, route,
                                                                mainBus, "", "",
                                                                mainBus?.id, tripInfo.main_driver_id,
                                                                userTickets, processedCount, bookingSnapshots.size(), callback
                                                            )
                                                        }
                                                } else {
                                                    // Không có main_driver_id
                                                    getPaymentStatusAndCreateTicket(
                                                        bookingDoc.id, bookingData, tripInfo, route,
                                                        mainBus, "", "",
                                                        mainBus?.id, tripInfo.main_driver_id,
                                                        userTickets, processedCount, bookingSnapshots.size(), callback
                                                    )
                                                }
                                            }
                                    }
                            }
                        }
                }
            }
    }

    private fun getPaymentStatusAndCreateTicket(
        bookingId: String,
        bookingData: Map<String, Any>?,
        trip: Trip,
        route: Route?,
        mainBus: Bus?,
        mainDriverName: String,
        mainDriverPhone: String,
        transferBusId: String?,
        transferDriverId: String?,
        userTickets: MutableList<UserTicket>,
        processedCount: Int,
        totalCount: Int,
        callback: ResultCallback<Result<List<UserTicket>>>
    ) {
        // Lấy thông tin thanh toán
        db.collection("payments")
            .whereEqualTo("bookingId", bookingId)
            .get()
            .addOnSuccessListener { paymentSnapshots ->
                val paymentStatus = if (paymentSnapshots.isEmpty) {
                    "Chưa thanh toán"
                } else {
                    val paymentDoc = paymentSnapshots.documents.first()
                    paymentDoc.data?.get("status") as? String ?: "Chưa thanh toán"
                }

                // Tạo UserTicket với thông tin đầy đủ
                val userTicket = UserTicket(
                    ticketCode = bookingId,

                    routeName = "${route?.origin} - ${route?.destination}",
                    origin = "${route?.origin}",
                    destination = "${route?.destination}",
                    departureDate = trip.trip_date,
                    departureTime = trip.departure_time,
                    price = trip.ticket_price,
                    paymentStatus = paymentStatus,
                    tripStatus = bookingData?.get("status") as? String ?: "Chưa đi",
                    seatNumbers = bookingData?.get("seat_id") as? String ?: "",
                    bookingId = bookingId,
                    tripId = trip.id ?: "",
                    pickupPoint = bookingData?.get("pickup_location") as? String?: "",
                    dropoffPoint = bookingData?.get("dropoff_location") as? String?: "",
                    mainDriverName = mainDriverName,
                    mainDriverPhone = mainDriverPhone,
                    mainBusLicensePlate = mainBus?.license_plate ?: "",
                    hasTransfer = !transferBusId.isNullOrEmpty()
                )
                Log.d("RemoteTripDataSource", "Đã tạo ticket: ${userTicket.paymentStatus}")

                userTickets.add(userTicket)

                if (userTickets.size == totalCount) {
                    val sortedTickets = userTickets.sortedByDescending {
                        combineDateTime(it.departureDate, it.departureTime)
                    }
                    callback.onResult(Result.Success(sortedTickets))
                }
            }
            .addOnFailureListener {
                // Tạo ticket với payment status mặc định
                val userTicket = UserTicket(
                    ticketCode = bookingId,
                    routeName = "${route?.origin} - ${route?.destination}",
                    departureDate = trip.trip_date,
                    departureTime = trip.departure_time,
                    price = trip.ticket_price,
                    paymentStatus = "Chưa thanh toán",
                    tripStatus = bookingData?.get("status") as? String ?: "Chưa đi",
                    seatNumbers = bookingData?.get("seat_numbers") as? String ?: "",

                    bookingId = bookingId,
                    tripId = trip.id ?: "",
                    pickupPoint = bookingData?.get("pickup_location") as? String?: "",
                    dropoffPoint = bookingData?.get("dropoff_location") as? String?: "",
                    mainDriverName = mainDriverName,
                    mainDriverPhone = mainDriverPhone,
                    mainBusLicensePlate = mainBus?.license_plate ?: "",
                    hasTransfer = !transferBusId.isNullOrEmpty()
                )

                userTickets.add(userTicket)

                if (userTickets.size == totalCount) {
                    val sortedTickets = userTickets.sortedByDescending {
                        combineDateTime(it.departureDate, it.departureTime)
                    }
                    callback.onResult(Result.Success(sortedTickets))
                }
            }
    }



    private fun combineDateTime(dateStr: String, timeStr: String): Long {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
            format.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
            format.parse("$dateStr $timeStr")?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }

}

data class TripDetails(
    val trip: Trip = Trip(),
    val route: Route = Route(),
    val bus: Bus = Bus()
)

//    override suspend fun loadTrip(
//        origin: String,
//        destination: String,
//        date: String,
//        callback: ResultCallback<Result<TripList>>
//    ) {
//        var db = Firebase.firestore
//        Log.d("RemoteTripDataSource", "🔍 Tìm trip với: origin=$origin, destination=$destination, date=$date")
//        // Find route_id with origin and destination
//        db.collection("routes")
//            .whereEqualTo("origin", origin)
//            .whereEqualTo("destination", destination)
//            .get()
//            .addOnSuccessListener { routeSnapshots ->
//                val routeIds = routeSnapshots.map { it.id }
//                if (routeIds.isEmpty()) {
//                    callback.onResult(Result.Success(TripList(emptyList())))
//                    return@addOnSuccessListener
//                }
//                // lấy trip theo trip date
//                db.collection("trip")
//                    //.whereIn("route_id", routeIds)
//                    .whereEqualTo("trip_date", date)
//                    .get()
//                    .addOnSuccessListener { tripSnapshots ->
//                        val trips = tripSnapshots.map { it.toObject(Trip::class.java) }
//                        Log.d("RemoteTripDataSource", "Tìm thấy ${trips.size} chuyến xe")
//                        callback.onResult(Result.Success(TripList(trips)))
//                    }
//                    .addOnFailureListener { ex ->
//                        callback.onResult(Result.Error(ex))
//                    }
//            }
//            .addOnFailureListener { ex ->
//                callback.onResult(Result.Error(ex))
//            }
//
//    }

