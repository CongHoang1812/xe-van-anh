package com.example.authenticateuserandpass.data.source.remote

import com.example.authenticateuserandpass.data.ResultCallback
import com.example.authenticateuserandpass.data.model.bus.Bus
import com.google.firebase.firestore.FirebaseFirestore
import com.example.authenticateuserandpass.data.source.Result

class BusDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val busCollection = firestore.collection("buses")

    fun getAllBuses(callback: ResultCallback<Result<List<Bus>>>) {
        busCollection
            .get()
            .addOnSuccessListener { documents ->
                val buses = documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Bus::class.java).apply {
                            id = doc.id
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                callback.onResult(Result.Success(buses))
            }
            .addOnFailureListener { exception ->
                callback.onResult(Result.Error(exception))
            }
    }
}