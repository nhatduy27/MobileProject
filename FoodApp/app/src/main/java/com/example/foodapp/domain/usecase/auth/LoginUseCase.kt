package com.example.foodapp.domain.usecase.auth

import com.example.foodapp.domain.entities.User
import com.example.foodapp.domain.repositories.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): User {
        return authRepository.login(email, password)
    }
}
