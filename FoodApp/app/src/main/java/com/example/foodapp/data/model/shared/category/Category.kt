package com.example.foodapp.data.model.shared.category

data class Category(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val isActive: Boolean = true
)