package com.example.foodapp.data.model

data class User(
    var id: String,
    var fullName: String,
    var email: String,
    var isVerify : Boolean,
    var phone: String = "",
    var role: String = "user",
    var imageAvatar: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)