package com.example.authenticateuserandpass.data.repository.booking

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val bookingCollection = db.collection("bookings") // tên collection của bạn

    /**
     * Cập nhật tất cả booking có tripId = [tripId] sang status = "Đã đi"
     */
    suspend fun markAllAsCompleted(tripId: String?): Result<Unit> {
        return try {
            val querySnapshot = bookingCollection
                .whereEqualTo("trip_id", tripId)
                .get()
                .await()

            // Batch để cập nhật nhiều document cùng lúc
            val batch = db.batch()
            for (doc in querySnapshot.documents) {
                batch.update(doc.reference, "status", "Đã đi")
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}