package com.example.foodapp.domain.usecase.auth

import com.example.foodapp.domain.entities.User
import com.example.foodapp.domain.repositories.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, displayName: String?): User {
        return authRepository.register(email, password, displayName)
    }
}
