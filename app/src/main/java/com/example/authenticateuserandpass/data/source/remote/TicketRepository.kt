package com.example.authenticateuserandpass.data.source.remote

import android.util.Log
import com.example.authenticateuserandpass.data.model.booking.Booking
import com.example.authenticateuserandpass.data.model.bus.Bus
import com.example.authenticateuserandpass.data.model.payment.Payment
import com.example.authenticateuserandpass.data.model.route.Route
import com.example.authenticateuserandpass.data.model.trip.Trip
import com.example.authenticateuserandpass.data.model.user.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class TicketRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")
    private val tripsCollection = firestore.collection("trip")
    private val routesCollection = firestore.collection("routes")
    private val usersCollection = firestore.collection("users")
    private val busesCollection = firestore.collection("buses")
    private val paymentsCollection = firestore.collection("payments")

    suspend fun getAllBookingsWithDetails(): List<TripDetails> = coroutineScope {
        try {
            // 1. Lấy tất cả Booking
            val bookings = bookingsCollection.get().await().toObjects(Booking::class.java)
            Log.d("TicketRepository", "Found ${bookings.size} bookings")

            // 2. Map từng booking sang TripDetails
            bookings.mapNotNull { booking ->
                async {
                    // 3. Fetch Trip, User, Payment song song
                    val tripDeferred = async { getTrip(booking.trip_id) }
                    val userDeferred = async { getUser(booking.user_id) }
                    val paymentDeferred = async { getPayment(booking.id) }

                    val trip = tripDeferred.await()
                    val user = userDeferred.await()
                    val payment = paymentDeferred.await()

                    // 4. Fetch Route và Bus nếu Trip tồn tại
                    val routeDeferred = trip?.let { async { getRoute(it.route_id) } }
                    val busDeferred = trip?.let { async { getBus(it.bus_id) } }

                    val route = routeDeferred?.await()
                    val bus = busDeferred?.await()

                    // 5. Tạo TripDetails nếu Trip tồn tại
                    if (trip != null) {
                        TripDetails(
                            booking = booking,
                            user = user ?: User(),
                            trip = trip,
                            route = route ?: Route(),
                            bus = bus ?: Bus(),
                            payment = payment
                        )
                    } else null
                }
            }.awaitAll() // Chờ tất cả async hoàn thành
        } catch (e: Exception) {
            Log.e("TicketRepository", "Error loading all bookings: ${e.message}")
            emptyList()
        } as List<TripDetails>
    }

//    suspend fun getBookingsByDate(date: String): List<TripDetails> {
//        return try {
//            Log.d("TicketRepository", "Searching bookings for date: $date")
//
//            // Chuyển date thành range để query
//            val searchDate = convertDateFormat(date) // "09/08/2025" -> "2025-08-09"
//            val startOfDay = "$searchDate 00:00:00"
//            val endOfDay = "$searchDate 23:59:59"
//
//            Log.d("TicketRepository", "Searching from $startOfDay to $endOfDay")
//
//            val bookings = bookingsCollection
//                .whereGreaterThanOrEqualTo("book_at", startOfDay)
//                .whereLessThanOrEqualTo("book_at", endOfDay)
//                .get()
//                .await()
//                .toObjects(Booking::class.java)
//
//            Log.d("TicketRepository", "Found ${bookings.size} bookings for date $date")
//
//            bookings.mapNotNull { booking ->
//                val trip = getTrip(booking.trip_id)
//                val route = trip?.let { getRoute(it.route_id) }
//                val user = getUser(booking.user_id)
//                val bus = trip?.let { getBus(it.bus_id) }
//                val payment = getPayment(booking.id)
//
//                if (trip != null) {
//                    TripDetails(
//                        booking = booking,
//                        user = user ?: User(),
//                        trip = trip,
//                        route = route ?: Route(),
//                        bus = bus ?: Bus(),
//                        payment = payment
//                    )
//                } else null
//            }
//        } catch (e: Exception) {
//            Log.e("TicketRepository", "Error loading bookings by date: ${e.message}")
//            emptyList()
//        }
//    }

    suspend fun getBookingsByDate(date: String): List<TripDetails> = coroutineScope {
        try {
            Log.d("TicketRepository", "Searching bookings for date: $date")

            // Chuyển date thành range để query
            val searchDate = convertDateFormat(date) // "09/08/2025" -> "2025-08-09"
            val startOfDay = "$searchDate 00:00:00"
            val endOfDay = "$searchDate 23:59:59"

            Log.d("TicketRepository", "Searching from $startOfDay to $endOfDay")

            val bookings = bookingsCollection
                .whereGreaterThanOrEqualTo("book_at", startOfDay)
                .whereLessThanOrEqualTo("book_at", endOfDay)
                .get()
                .await()
                .toObjects(Booking::class.java)

            Log.d("TicketRepository", "Found ${bookings.size} bookings for date $date")

            // Map từng booking sang TripDetails song song
            bookings.mapNotNull { booking ->
                async {
                    // Song song fetch Trip, User, Payment
                    val tripDeferred = async { getTrip(booking.trip_id) }
                    val userDeferred = async { getUser(booking.user_id) }
                    val paymentDeferred = async { getPayment(booking.id) }

                    val trip = tripDeferred.await()
                    val user = userDeferred.await()
                    val payment = paymentDeferred.await()

                    // Song song fetch Route và Bus nếu Trip tồn tại
                    val routeDeferred = trip?.let { async { getRoute(it.route_id) } }
                    val busDeferred = trip?.let { async { getBus(it.bus_id) } }

                    val route = routeDeferred?.await()
                    val bus = busDeferred?.await()

                    if (trip != null) {
                        TripDetails(
                            booking = booking,
                            user = user ?: User(),
                            trip = trip,
                            route = route ?: Route(),
                            bus = bus ?: Bus(),
                            payment = payment
                        )
                    } else null
                }
            }.awaitAll() // Chờ tất cả async hoàn thành trước khi trả về
        } catch (e: Exception) {
            Log.e("TicketRepository", "Error loading bookings by date: ${e.message}")
            emptyList()
        } as List<TripDetails>
    }

    private fun convertDateFormat(date: String): String {
        return try {
            val parts = date.split("/")
            if (parts.size == 3) {
                val day = parts[0].padStart(2, '0')
                val month = parts[1].padStart(2, '0')
                val year = parts[2]
                "$year-$month-$day"
            } else {
                date
            }
        } catch (e: Exception) {
            Log.e("TicketRepository", "Error converting date format: ${e.message}")
            date
        }
    }

    private suspend fun getTrip(tripId: String): Trip? {
        return try {
            val document = tripsCollection.document(tripId).get().await()
            val trip = document.toObject(Trip::class.java)
            trip?.id = document.id
            trip
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getRoute(routeId: String): Route? {
        return try {
            routesCollection.document(routeId).get().await().toObject(Route::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getUser(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getBus(busId: String): Bus? {
        return try {
            busesCollection.document(busId).get().await().toObject(Bus::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteBooking(bookingId: String): Boolean {
        return try {
            bookingsCollection.document(bookingId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("TicketRepository", "Error deleting booking: ${e.message}")
            false
        }
    }
//    private suspend fun getPayment(bookingId: String): Payment? {
//        return try {
//            Log.d("TicketRepository", "Getting payment for booking: $bookingId")
//
//            // Lấy tất cả payments và tìm payment chứa bookingId
//            val allPayments = paymentsCollection
//                .get()
//                .await()
//                .toObjects(Payment::class.java)
//
//            Log.d("TicketRepository", "Total payments found: ${allPayments.size}")
//
//            // Tìm payment có chứa bookingId trong chuỗi bookingId
//            val payment = allPayments.find { payment ->
//                val bookingIds = payment.bookingId.split(",").map { it.trim() }
//                bookingIds.contains(bookingId)
//            }
//
//            if (payment != null) {
//                Log.d("TicketRepository", "Payment found - Status: ${payment.status}, BookingIds: ${payment.bookingId}")
//            } else {
//                Log.d("TicketRepository", "No payment found for booking $bookingId")
//            }
//
//            payment
//        } catch (e: Exception) {
//            Log.e("TicketRepository", "Error getting payment for booking $bookingId: ${e.message}")
//            null
//        }
//    }
private suspend fun getPayment(bookingId: String): Payment? {
    return try {
        val snapshot = paymentsCollection
            .whereEqualTo("bookingId", bookingId)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.toObject(Payment::class.java)
    } catch (e: Exception) {
        Log.e("TicketRepository", "Error getting payment for $bookingId: ${e.message}")
        null
    }
}
    suspend fun updatePaymentStatus(bookingId: String, newStatus: String): Boolean {
        return try {
            Log.d("TicketRepository", "Updating payment status for booking: $bookingId to: $newStatus")

            // Tìm payment document chứa bookingId
            val allPayments = paymentsCollection.get().await()

            for (document in allPayments.documents) {
                val payment = document.toObject(Payment::class.java)
                if (payment != null) {
                    val bookingIds = payment.bookingId.split(",").map { it.trim() }
                    if (bookingIds.contains(bookingId)) {
                        // Cập nhật status của payment document này
                        paymentsCollection.document(document.id)
                            .update("status", newStatus)
                            .await()

                        Log.d("TicketRepository", "Payment status updated successfully")
                        return true
                    }
                }
            }

            Log.w("TicketRepository", "No payment found for booking: $bookingId")
            false
        } catch (e: Exception) {
            Log.e("TicketRepository", "Error updating payment status: ${e.message}")
            false
        }
    }

}