package com.example.foodapp.authentication.forgotpassword.resetpassword

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.data.repository.shared.AuthRepository
import com.example.foodapp.data.model.shared.auth.ApiResult
import kotlinx.coroutines.launch

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

class ResetPasswordViewModel(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableLiveData<ResetPasswordState>(ResetPasswordState.Idle)
    val state: LiveData<ResetPasswordState> = _state

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    private val _newPassword = MutableLiveData<String>("")
    val newPassword: LiveData<String> = _newPassword

    private val _confirmPassword = MutableLiveData<String>("")
    val confirmPassword: LiveData<String> = _confirmPassword

    private val _passwordError = MutableLiveData<String?>(null)
    val passwordError: LiveData<String?> = _passwordError

    private val _confirmPasswordError = MutableLiveData<String?>(null)
    val confirmPasswordError: LiveData<String?> = _confirmPasswordError

    fun setEmail(email: String) {
        _userEmail.value = email
    }

    fun setNewPassword(password: String) {
        _newPassword.value = password
        _passwordError.value = null
    }

    fun setConfirmPassword(password: String) {
        _confirmPassword.value = password
        _confirmPasswordError.value = null
    }

    fun resetPassword(newPassword: String, confirmPassword: String) {
        // Validate input
        val validationResult = validateResetPasswordInput(newPassword, confirmPassword)

        if (validationResult is ValidationResult.Error) {
            // Xử lý error dựa trên loại lỗi
            when {
                newPassword.isBlank() -> {
                    _passwordError.value = "Vui lòng nhập mật khẩu mới"
                    _confirmPasswordError.value = null
                }
                newPassword.length < 6 -> {
                    _passwordError.value = "Mật khẩu phải có ít nhất 6 ký tự"
                    _confirmPasswordError.value = null
                }
                newPassword.length > 100 -> {
                    _passwordError.value = "Mật khẩu quá dài (tối đa 100 ký tự)"
                    _confirmPasswordError.value = null
                }
                confirmPassword.isBlank() -> {
                    _passwordError.value = null
                    _confirmPasswordError.value = "Vui lòng xác nhận mật khẩu"
                }
                newPassword != confirmPassword -> {
                    _passwordError.value = null
                    _confirmPasswordError.value = "Mật khẩu xác nhận không khớp"
                }
                else -> {
                    _state.value = ResetPasswordState.Error(validationResult.message)
                }
            }
            return
        }

        // Nếu validate thành công, tiếp tục xử lý
        _state.value = ResetPasswordState.Loading
        performResetPassword(newPassword)
    }

    // Hàm validate riêng
    private fun validateResetPasswordInput(
        newPassword: String,
        confirmPassword: String
    ): ValidationResult {
        return when {
            newPassword.isBlank() ->
                ValidationResult.Error("Vui lòng nhập mật khẩu mới")

            newPassword.length < 6 ->
                ValidationResult.Error("Mật khẩu phải có ít nhất 6 ký tự")

            newPassword.length > 100 ->
                ValidationResult.Error("Mật khẩu quá dài (tối đa 100 ký tự)")

            confirmPassword.isBlank() ->
                ValidationResult.Error("Vui lòng xác nhận mật khẩu")

            newPassword != confirmPassword ->
                ValidationResult.Error("Mật khẩu xác nhận không khớp")

            else -> ValidationResult.Success
        }
    }

    private fun performResetPassword(newPassword: String) {
        val email = _userEmail.value
        if (email.isNullOrEmpty()) {
            _state.value = ResetPasswordState.Error("Không tìm thấy email")
            return
        }

        viewModelScope.launch {
            try {
                val result = authRepository.resetPassword(email, newPassword)

                when (result) {
                    is ApiResult.Success -> {
                        val apiResponse = result.data
                        if (apiResponse.success) {
                            _state.value = ResetPasswordState.Success
                        } else {
                            val errorMsg = apiResponse.message ?: "Đổi mật khẩu thất bại"
                            _state.value = ResetPasswordState.Error(errorMsg)
                        }
                    }

                    is ApiResult.Failure -> {
                        val errorMsg = result.exception.message ?: "Lỗi kết nối"
                        _state.value = ResetPasswordState.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                _state.value = ResetPasswordState.Error("Lỗi không xác định: ${e.message}")
            }
        }
    }

    // Helper function để kiểm tra form có hợp lệ không
    fun validateForm(): Boolean {
        val newPass = _newPassword.value ?: ""
        val confirmPass = _confirmPassword.value ?: ""

        return when (validateResetPasswordInput(newPass, confirmPass)) {
            is ValidationResult.Success -> true
            is ValidationResult.Error -> false
        }
    }

    // Reset state
    fun resetState() {
        _state.value = ResetPasswordState.Idle
        _passwordError.value = null
        _confirmPasswordError.value = null
    }

    // Clear all data
    fun clear() {
        _userEmail.value = null
        _newPassword.value = ""
        _confirmPassword.value = ""
        _passwordError.value = null
        _confirmPasswordError.value = null
        _state.value = ResetPasswordState.Idle
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ResetPasswordViewModel(
                        AuthRepository(),
                        context
                    ) as T
                }
            }
        }
    }
}