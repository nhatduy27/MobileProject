package com.example.foodapp.data.remote.model

data class RestaurantRemote(
    val id: String? = null,
    val ownerId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val address: Map<String, Any>? = null,
    val averageRating: Double? = null,
    val totalReviews: Int? = null,
    val logoUrl: String? = null,
    val bannerUrl: String? = null,
    val isOpen: Boolean? = null,
    val operatingHours: Map<String, Any>? = null,
    val deliveryFee: Long? = null,
    val minOrderAmount: Long? = null,
    val orderCount: Int? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
