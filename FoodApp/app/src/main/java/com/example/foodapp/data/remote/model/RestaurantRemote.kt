package com.example.foodapp.data.remote.model

data class RestaurantRemote(
    val id: String? = null,
    val ownerId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val address: String? = null,
    val averageRating: Double? = null,
    val totalReviews: Int? = null,
    val isOpen: Boolean? = null,
    val deliveryFee: Long? = null,
    val minOrderAmount: Long? = null
)
