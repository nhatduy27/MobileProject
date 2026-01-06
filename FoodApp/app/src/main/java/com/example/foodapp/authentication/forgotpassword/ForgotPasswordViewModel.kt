package com.example.foodapp.authentication.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Sending : ForgotPasswordState()
    data class Success(val email: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class ForgotPasswordViewModel(
    private val userRepository: UserFirebaseRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    /**
     * Gửi email reset password
     */
    fun sendResetEmail(email: String) {
        viewModelScope.launch {
            // Validate email
            if (email.isEmpty() || !email.contains("@")) {
                _state.value = ForgotPasswordState.Error("Email không hợp lệ")
                return@launch
            }

            _state.value = ForgotPasswordState.Sending

            userRepository.sendPasswordResetEmail(email) { success, errorMessage ->
                if (success) {
                    _state.value = ForgotPasswordState.Success(email)
                } else {
                    _state.value = ForgotPasswordState.Error(
                        errorMessage ?: "Gửi email thất bại"
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.value = ForgotPasswordState.Idle
    }

    companion object {
        fun factory(context: android.content.Context): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
                        val repository = UserFirebaseRepository(context)
                        return ForgotPasswordViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}