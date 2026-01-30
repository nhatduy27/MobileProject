package com.example.foodapp.authentication.login


// TRẠNG THÁI CỦA ĐĂNG NHẬP

sealed class LogInState {
    object Idle : LogInState()
    object Loading : LogInState()
    data class Success(
        val userId: String,
        val email: String,
        val displayName: String,
        val role: String
    ) : LogInState()
    data class Error(val message: String, val code: String? = null) : LogInState()
}

sealed class GoogleLogInState {
    object Idle : GoogleLogInState()
    object Loading : GoogleLogInState()
    data class Success(
        val userId: String,
        val email: String,
        val displayName: String,
        val role: String,
        val isNewUser: Boolean = false
    ) : GoogleLogInState()
    data class Error(val message: String, val code: String? = null) : GoogleLogInState()
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}