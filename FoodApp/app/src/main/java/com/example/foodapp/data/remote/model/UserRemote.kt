package com.example.foodapp.data.remote.model

data class UserRemote(
    val id: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val role: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Long? = null,
    val isActive: Boolean? = null,
    val isVerified: Boolean? = null
)
