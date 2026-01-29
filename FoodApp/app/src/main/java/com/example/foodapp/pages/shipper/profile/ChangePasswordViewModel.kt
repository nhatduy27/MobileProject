package com.example.foodapp.pages.shipper.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.shared.auth.ApiResult
import com.example.foodapp.data.repository.shared.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel cho ChangePasswordScreen
 * Handles password change via backend API
 */
class ChangePasswordViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun updateCurrentPassword(value: String) {
        _uiState.value = _uiState.value.copy(currentPassword = value, error = null)
    }

    fun updateNewPassword(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value, error = null)
    }

    fun updateConfirmPassword(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, error = null)
    }

    fun toggleCurrentPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            currentPasswordVisible = !_uiState.value.currentPasswordVisible
        )
    }

    fun toggleNewPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            newPasswordVisible = !_uiState.value.newPasswordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            confirmPasswordVisible = !_uiState.value.confirmPasswordVisible
        )
    }

    fun changePassword() {
        val state = _uiState.value

        // Validate inputs
        if (state.currentPassword.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng nhập mật khẩu hiện tại")
            return
        }

        if (state.newPassword.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng nhập mật khẩu mới")
            return
        }

        if (state.newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Mật khẩu mới phải có ít nhất 6 ký tự")
            return
        }

        // Check if new password is the same as current password
        if (state.newPassword == state.currentPassword) {
            _uiState.value = _uiState.value.copy(error = "Mật khẩu mới không được trùng với mật khẩu hiện tại")
            return
        }

        if (state.confirmPassword.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng xác nhận mật khẩu mới")
            return
        }

        if (state.newPassword != state.confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Mật khẩu xác nhận không khớp")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)

            try {
                // Get current user
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Vui lòng đăng nhập lại"
                    )
                    return@launch
                }

                val email = currentUser.email
                if (email == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Không tìm thấy email, vui lòng đăng nhập lại"
                    )
                    return@launch
                }

                // Step 1: Verify old password by reauthenticating
                Log.d("ChangePasswordVM", "🔄 Verifying current password...")
                val credential = EmailAuthProvider.getCredential(email, state.currentPassword)
                
                try {
                    currentUser.reauthenticate(credential).await()
                    Log.d("ChangePasswordVM", "✅ Current password verified")
                } catch (e: Exception) {
                    Log.e("ChangePasswordVM", "❌ Reauthentication failed: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Mật khẩu hiện tại không đúng"
                    )
                    return@launch
                }

                // Step 2: Get ID token for API call
                val token = currentUser.getIdToken(true).await().token
                if (token == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Không thể xác thực, vui lòng đăng nhập lại"
                    )
                    return@launch
                }

                Log.d("ChangePasswordVM", "🔄 Calling change password API...")
                
                // Step 3: Call API to change password
                val result = authRepository.changePassword(
                    accessToken = token,
                    oldPassword = state.currentPassword,
                    newPassword = state.newPassword
                )

                when (result) {
                    is ApiResult.Success -> {
                        Log.d("ChangePasswordVM", "✅ Password changed successfully")
                        _uiState.value = ChangePasswordUiState(
                            isLoading = false,
                            successMessage = "Đổi mật khẩu thành công!"
                        )
                    }
                    is ApiResult.Failure -> {
                        Log.e("ChangePasswordVM", "❌ Failed to change password: ${result.exception.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Đổi mật khẩu thất bại"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ChangePasswordVM", "❌ Exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordVisible: Boolean = false,
    val newPasswordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
