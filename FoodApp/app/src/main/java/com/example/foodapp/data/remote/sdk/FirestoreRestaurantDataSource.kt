package com.example.foodapp.data.remote.sdk

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.foodapp.data.remote.model.RestaurantRemote
import com.example.foodapp.data.remote.model.MenuItemRemote
import com.example.foodapp.di.InjectionConstants

class FirestoreRestaurantDataSource(private val firestore: FirebaseFirestore) {
    
    suspend fun getRestaurants(): List<RestaurantRemote> {
        return try {
            val snapshot = firestore.collection(InjectionConstants.RESTAURANTS_COLLECTION).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(RestaurantRemote::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getRestaurantDetail(id: String): RestaurantRemote {
        return try {
            val doc = firestore.collection(InjectionConstants.RESTAURANTS_COLLECTION).document(id).get().await()
            doc.toObject(RestaurantRemote::class.java) ?: throw Exception("Restaurant not found")
        } catch (e: Exception) {
            throw Exception("Failed to fetch restaurant: ${e.message}")
        }
    }
    
    suspend fun getMenuItems(restaurantId: String): List<MenuItemRemote> {
        return try {
            val snapshot = firestore
                .collection(InjectionConstants.RESTAURANTS_COLLECTION)
                .document(restaurantId)
                .collection(InjectionConstants.MENU_ITEMS_COLLECTION)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(MenuItemRemote::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
