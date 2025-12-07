package com.example.foodapp.domain.entities

data class Order(
    val id: String,
    val userId: String,
    val restaurantId: String,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val subtotal: Long,
    val deliveryFee: Long,
    val totalAmount: Long,
    val createdAt: Long?,
    val updatedAt: Long?
)
