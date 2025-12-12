package com.example.foodapp.pages.foods

data class Food(
    val id: Int,
    val name: String,
    val category: String,
    val type: String,
    val rating: Double,
    val reviewCount: Int,
    val price: Int,
    val isAvailable: Boolean
)