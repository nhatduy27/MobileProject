package com.example.foodapp.data.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val role: String = "user",
    val imageAvatar: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)