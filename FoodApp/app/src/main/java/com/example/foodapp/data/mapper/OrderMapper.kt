package com.example.foodapp.data.mapper

import com.example.foodapp.domain.entities.Order
import com.example.foodapp.domain.entities.OrderItem
import com.example.foodapp.domain.entities.OrderStatus
import com.example.foodapp.data.remote.model.OrderRemote
import com.example.foodapp.data.remote.model.OrderItemRemote

object OrderMapper {
    
    fun fromRemote(orderRemote: OrderRemote): Order {
        return Order(
            id = orderRemote.id ?: "",
            userId = orderRemote.userId ?: "",
            restaurantId = orderRemote.restaurantId ?: "",
            items = orderRemote.items?.map { itemRemote ->
                OrderItem(
                    menuItemId = itemRemote.menuItemId ?: "",
                    name = itemRemote.name ?: "",
                    quantity = itemRemote.quantity ?: 0,
                    unitPrice = itemRemote.unitPrice ?: 0L,
                    totalPrice = itemRemote.totalPrice ?: 0L
                )
            } ?: emptyList(),
            status = try {
                OrderStatus.valueOf(orderRemote.status ?: "PENDING")
            } catch (e: Exception) {
                OrderStatus.PENDING
            },
            subtotal = orderRemote.subtotal ?: 0L,
            deliveryFee = orderRemote.deliveryFee ?: 0L,
            totalAmount = orderRemote.totalAmount ?: 0L,
            createdAt = orderRemote.createdAt,
            updatedAt = orderRemote.updatedAt
            // Note: shipperId, discountAmount, promotionCode, deliveryAddress, notes, paymentMethod, 
            // paymentStatus, estimatedDeliveryTime, actualDeliveryTime, rating, review are in Firestore but not domain
        )
    }
    
    fun toRemote(order: Order): OrderRemote {
        return OrderRemote(
            id = order.id,
            userId = order.userId,
            restaurantId = order.restaurantId,
            shipperId = null, // Will be assigned when shipper accepts
            items = order.items.map { item ->
                OrderItemRemote(
                    menuItemId = item.menuItemId,
                    name = item.name,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice,
                    imageUrl = null,
                    notes = null
                )
            },
            status = order.status.name,
            subtotal = order.subtotal,
            deliveryFee = order.deliveryFee,
            discountAmount = 0L,
            totalAmount = order.totalAmount,
            promotionCode = null,
            deliveryAddress = null, // Should be set from user's address
            notes = null,
            paymentMethod = "CASH", // Default payment method
            paymentStatus = "PENDING",
            estimatedDeliveryTime = null,
            actualDeliveryTime = null,
            rating = null,
            review = null,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt
        )
    }
}
