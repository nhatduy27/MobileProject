package com.example.foodapp.data.remote.model

data class OrderRemote(
    val id: String? = null,
    val userId: String? = null,
    val restaurantId: String? = null,
    val shipperId: String? = null,
    val items: List<OrderItemRemote>? = null,
    val status: String? = null,
    val subtotal: Long? = null,
    val deliveryFee: Long? = null,
    val discountAmount: Long? = null,
    val totalAmount: Long? = null,
    val promotionCode: String? = null,
    val deliveryAddress: Map<String, Any>? = null,
    val notes: String? = null,
    val paymentMethod: String? = null,
    val paymentStatus: String? = null,
    val estimatedDeliveryTime: Long? = null,
    val actualDeliveryTime: Long? = null,
    val rating: Double? = null,
    val review: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
