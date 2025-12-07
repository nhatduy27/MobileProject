package com.example.foodapp.data.mapper

import com.example.foodapp.domain.entities.Order
import com.example.foodapp.domain.entities.OrderItem
import com.example.foodapp.domain.entities.OrderStatus

object OrderMapper {
    
    fun toMap(order: Order): Map<String, Any?> {
        return mapOf(
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
    }
    
    fun fromMap(map: Map<String, Any?>): Order {
        @Suppress("UNCHECKED_CAST")
        val itemsList = map["items"] as? List<Map<String, Any?>> ?: emptyList()
        
        return Order(
            id = map["id"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            restaurantId = map["restaurantId"] as? String ?: "",
            items = itemsList.map { itemMap ->
                OrderItem(
                    menuItemId = itemMap["menuItemId"] as? String ?: "",
                    name = itemMap["name"] as? String ?: "",
                    quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0,
                    unitPrice = (itemMap["unitPrice"] as? Number)?.toLong() ?: 0L,
                    totalPrice = (itemMap["totalPrice"] as? Number)?.toLong() ?: 0L
                )
            },
            status = try {
                OrderStatus.valueOf((map["status"] as? String) ?: "PENDING")
            } catch (e: Exception) {
                OrderStatus.PENDING
            },
            subtotal = (map["subtotal"] as? Number)?.toLong() ?: 0L,
            deliveryFee = (map["deliveryFee"] as? Number)?.toLong() ?: 0L,
            totalAmount = (map["totalAmount"] as? Number)?.toLong() ?: 0L,
            createdAt = (map["createdAt"] as? Number)?.toLong(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong()
        )
    }
}
