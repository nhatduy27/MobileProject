package com.example.foodapp.domain.repositories

import com.example.foodapp.domain.entities.User

interface AuthRepository {
    suspend fun register(email: String, password: String, displayName: String?): User
    suspend fun login(email: String, password: String): User
    suspend fun getCurrentUser(): User?
    suspend fun logout()
}
