package com.example.foodapp.domain.usecase.auth

import com.example.foodapp.domain.entities.User
import com.example.foodapp.domain.repositories.AuthRepository

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}
