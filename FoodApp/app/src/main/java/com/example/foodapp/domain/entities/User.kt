package com.example.foodapp.domain.entities

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val phoneNumber: String?,
    val role: UserRole,
    val avatarUrl: String?,
    val createdAt: Long?,
    val isActive: Boolean,
    val isVerified: Boolean
)
