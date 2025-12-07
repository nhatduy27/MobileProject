package com.example.foodapp.data.remote.model

data class OrderRemote(
    val id: String? = null,
    val userId: String? = null,
    val restaurantId: String? = null,
    val items: List<OrderItemRemote>? = null,
    val status: String? = null,
    val subtotal: Long? = null,
    val deliveryFee: Long? = null,
    val totalAmount: Long? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
