package com.example.foodapp.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.foodapp.domain.entities.Order
import com.example.foodapp.di.InjectionConstants

class FirestoreOrderDataSource(private val firestore: FirebaseFirestore) {
    
    suspend fun createOrder(order: Order): Order {
        val orderMap = mapOf(
            "id" to order.id,
            "userId" to order.userId,
            "restaurantId" to order.restaurantId,
            "items" to order.items.map { item ->
                mapOf(
                    "menuItemId" to item.menuItemId,
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "unitPrice" to item.unitPrice,
                    "totalPrice" to item.totalPrice
                )
            },
            "status" to order.status.name,
            "subtotal" to order.subtotal,
            "deliveryFee" to order.deliveryFee,
            "totalAmount" to order.totalAmount,
            "createdAt" to order.createdAt,
            "updatedAt" to order.updatedAt
        )
        firestore.collection(InjectionConstants.ORDERS_COLLECTION).document(order.id).set(orderMap).await()
        return order
    }
    
    suspend fun getOrdersForUser(userId: String): List<Order> {
        return try {
            val snapshot = firestore
                .collection(InjectionConstants.ORDERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getOrderDetail(orderId: String): Order {
        return try {
            val doc = firestore.collection(InjectionConstants.ORDERS_COLLECTION).document(orderId).get().await()
            doc.toObject(Order::class.java) ?: throw Exception("Order not found")
        } catch (e: Exception) {
            throw Exception("Failed to fetch order: ${e.message}")
        }
    }
}
