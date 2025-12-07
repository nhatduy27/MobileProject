package com.example.foodapp.domain.entities

data class Restaurant(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String?,
    val category: String,
    val phoneNumber: String?,
    val email: String?,
    val address: String,
    val averageRating: Double,
    val totalReviews: Int,
    val isOpen: Boolean,
    val deliveryFee: Long,
    val minOrderAmount: Long
)
