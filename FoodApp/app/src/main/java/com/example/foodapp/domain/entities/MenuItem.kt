package com.example.foodapp.domain.entities

data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String?,
    val price: Long,
    val isAvailable: Boolean,
    val imageUrl: String?
)
