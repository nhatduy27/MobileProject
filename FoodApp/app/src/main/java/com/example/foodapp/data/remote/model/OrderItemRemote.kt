package com.example.foodapp.data.remote.model

data class OrderItemRemote(
    val menuItemId: String? = null,
    val name: String? = null,
    val quantity: Int? = null,
    val unitPrice: Long? = null,
    val totalPrice: Long? = null
)
