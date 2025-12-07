package com.example.foodapp.data.remote.sdk

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.foodapp.data.remote.model.OrderRemote
import com.example.foodapp.data.remote.model.OrderItemRemote
import com.example.foodapp.di.InjectionConstants

class FirestoreOrderDataSource(private val firestore: FirebaseFirestore) {
    
    suspend fun createOrder(orderRemote: OrderRemote): OrderRemote {
        val orderMap = mapOf(
            "id" to orderRemote.id,
            "userId" to orderRemote.userId,
            "restaurantId" to orderRemote.restaurantId,
            "items" to orderRemote.items?.map { item ->
                mapOf(
                    "menuItemId" to item.menuItemId,
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "unitPrice" to item.unitPrice,
                    "totalPrice" to item.totalPrice
                )
            },
            "status" to orderRemote.status,
            "subtotal" to orderRemote.subtotal,
            "deliveryFee" to orderRemote.deliveryFee,
            "totalAmount" to orderRemote.totalAmount,
            "createdAt" to orderRemote.createdAt,
            "updatedAt" to orderRemote.updatedAt
        )
        orderRemote.id?.let {
            firestore.collection(InjectionConstants.ORDERS_COLLECTION).document(it).set(orderMap).await()
        }
        return orderRemote
    }
    
    suspend fun getOrdersForUser(userId: String): List<OrderRemote> {
        return try {
            val snapshot = firestore
                .collection(InjectionConstants.ORDERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(OrderRemote::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getOrderDetail(orderId: String): OrderRemote {
        return try {
            val doc = firestore.collection(InjectionConstants.ORDERS_COLLECTION).document(orderId).get().await()
            doc.toObject(OrderRemote::class.java) ?: throw Exception("Order not found")
        } catch (e: Exception) {
            throw Exception("Failed to fetch order: ${e.message}")
        }
    }
}
