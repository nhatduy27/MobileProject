package com.example.foodapp.domain.entities

data class OrderItem(
    val menuItemId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Long,
    val totalPrice: Long
)
