package com.example.foodapp.domain.repositories

import com.example.foodapp.domain.entities.Order

interface OrderRepository {
    suspend fun placeOrder(order: Order): Order
    suspend fun getOrdersForCurrentUser(): List<Order>
    suspend fun getOrderDetail(orderId: String): Order
}
