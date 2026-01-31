package com.example.foodapp.authentication.signup


// TRẠNG THÁI CỦA ĐĂNG KÝ TÀI KHOẢN BẰNG EMAIL

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    data class Error(val message: String) : SignUpState()
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}