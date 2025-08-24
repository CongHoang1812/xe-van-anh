package com.example.authenticateuserandpass.data.repository.route

import com.example.authenticateuserandpass.data.model.route.Route
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RouteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val routesCollection = firestore.collection("routes")

    suspend fun getAllRoutes(): List<Route> {
        return try {
            val snapshot = routesCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Route::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addRoute(route: Route): Boolean {
        return try {
            routesCollection.add(route).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateRoute(routeId: String, route: Route): Boolean {
        return try {
            routesCollection.document(routeId).set(route).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteRoute(routeId: String): Boolean {
        return try {
            routesCollection.document(routeId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}