package com.example.authenticateuserandpass.data.repository.user


import com.example.authenticateuserandpass.ResultCallback
import com.example.authenticateuserandpass.data.firebaseModel.Passenger
import com.example.authenticateuserandpass.data.model.booking.Booking
import com.example.authenticateuserandpass.data.model.user.User
import com.google.firebase.firestore.FirebaseFirestore
import com.example.authenticateuserandpass.data.source.Result

interface PassengerRepository {
    suspend fun getPassengersByTripId(tripId: String, callback: ResultCallback<Result<List<Passenger>>>)
    suspend fun getUserById(userId: String, callback: ResultCallback<Result<User>>)
}

class PassengerRepositoryImpl : PassengerRepository {
    private val db = FirebaseFirestore.getInstance()
    private val bookingsCollection = db.collection("bookings")
    private val usersCollection = db.collection("users")

    override suspend fun getPassengersByTripId(tripId: String, callback: ResultCallback<Result<List<Passenger>>>) {
        bookingsCollection
            .whereEqualTo("trip_id", tripId)
            .whereIn("status", listOf("confirmed", "pending"))
            .get()
            .addOnSuccessListener { bookingDocuments ->
                if (bookingDocuments.isEmpty) {
                    callback.onResult(Result.Success(emptyList()))
                    return@addOnSuccessListener
                }

                val passengers = mutableListOf<Passenger>()
                var completedCount = 0

                for (bookingDoc in bookingDocuments) {
                    val booking = bookingDoc.toObject(Booking::class.java)

                    // Lấy thông tin người dùng
                    usersCollection.document(booking.user_id)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val user = userDoc.toObject(User::class.java)

                            if (user != null) {
                                passengers.add(
                                    Passenger(
                                        id = "${booking.id}_${user.uid}",
                                        booking_id = booking.id,
                                        user_id = user.uid,
                                        name = user.name,
                                        phone = user.phone,
                                        email = user.email,
                                        seat_id = booking.seat_id,
                                        pickup_location = booking.pickup_location,
                                        dropoff_location = booking.dropoff_location,
                                        note = booking.note,
                                        booking_status = booking.status
                                    )
                                )
                            }

                            completedCount++
                            if (completedCount == bookingDocuments.size()) {
                                // Sắp xếp theo tên hành khách
                                val sortedPassengers = passengers.sortedBy { it.name }
                                callback.onResult(Result.Success(sortedPassengers))
                            }
                        }
                        .addOnFailureListener { e ->
                            completedCount++
                            if (completedCount == bookingDocuments.size()) {
                                val sortedPassengers = passengers.sortedBy { it.name }
                                callback.onResult(Result.Success(sortedPassengers))
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                callback.onResult(Result.Error(e))
            }
    }

    override suspend fun getUserById(userId: String, callback: ResultCallback<Result<User>>) {
        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    callback.onResult(Result.Success(user))
                } else {
                    callback.onResult(Result.Error(Exception("User not found")))
                }
            }
            .addOnFailureListener { e ->
                callback.onResult(Result.Error(e))
            }
    }
}