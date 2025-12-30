package com.example.foodapp.data.model.owner

data class Food(
    val id: Int,
    val name: String,
    val category: String,
    val type: String,
    val rating: Double,
    val reviewCount: Int,
    val price: Int,
    val isAvailable: Boolean,
    val imageUrl: String? = null
)
