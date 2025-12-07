package com.example.foodapp.domain.usecase.auth

import com.example.foodapp.domain.repositories.AuthRepository

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
