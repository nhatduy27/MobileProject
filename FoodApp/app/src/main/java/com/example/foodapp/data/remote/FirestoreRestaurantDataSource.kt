package com.example.foodapp.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.foodapp.domain.entities.Restaurant
import com.example.foodapp.domain.entities.MenuItem
import com.example.foodapp.di.InjectionConstants

class FirestoreRestaurantDataSource(private val firestore: FirebaseFirestore) {
    
    suspend fun getRestaurants(): List<Restaurant> {
        return try {
            val snapshot = firestore.collection(InjectionConstants.RESTAURANTS_COLLECTION).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Restaurant::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getRestaurantDetail(id: String): Restaurant {
        return try {
            val doc = firestore.collection(InjectionConstants.RESTAURANTS_COLLECTION).document(id).get().await()
            doc.toObject(Restaurant::class.java) ?: throw Exception("Restaurant not found")
        } catch (e: Exception) {
            throw Exception("Failed to fetch restaurant: ${e.message}")
        }
    }
    
    suspend fun getMenuItems(restaurantId: String): List<MenuItem> {
        return try {
            val snapshot = firestore
                .collection(InjectionConstants.RESTAURANTS_COLLECTION)
                .document(restaurantId)
                .collection(InjectionConstants.MENU_ITEMS_COLLECTION)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(MenuItem::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
