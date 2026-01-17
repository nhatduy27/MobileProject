package com.example.foodapp.authentication.signup

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.data.repository.shared.AuthRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.model.shared.auth.ApiResult
import kotlinx.coroutines.launch

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

class SignUpViewModel(
    private val repository: UserFirebaseRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    // Thêm AuthManager
    private val authManager = AuthManager(context)

    private val _signUpState = MutableLiveData<SignUpState>(SignUpState.Idle)
    val signUpState: LiveData<SignUpState> = _signUpState

    private val _saveUserState = MutableLiveData<Boolean?>(null)
    val saveUserState: LiveData<Boolean?> = _saveUserState

    fun registerWithEmail(displayName: String, email: String, password: String, confirmPassword: String) {
        // Validate input
        val validationResult = validateInput(displayName, email, password, confirmPassword)
        if (validationResult is ValidationResult.Error) {
            _signUpState.value = SignUpState.Error(validationResult.message)
            return
        }

        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            try {
                val result = authRepository.register(email, displayName, password)

                when (result) {
                    is ApiResult.Success -> {
                        val apiResponse = result.data

                        // Kiểm tra outer success
                        if (apiResponse.success) {
                            // Parse data from Map (backend returns: {user: {...}, customToken: "..."})
                            val dataMap = apiResponse.data as? Map<*, *>
                            val userMap = dataMap?.get("user") as? Map<*, *>
                            val customToken = dataMap?.get("customToken") as? String
                            
                            val userId = userMap?.get("id") as? String ?: ""
                            val userEmail = userMap?.get("email") as? String ?: ""
                            val userName = userMap?.get("displayName") as? String ?: ""
                            val userRole = userMap?.get("role") as? String ?: ""
                            val userStatus = userMap?.get("status") as? String ?: ""
                            val isValidUser = userId.isNotBlank() && userEmail.isNotBlank()

                            if (isValidUser) {

                                // SỬA: Dùng AuthManager để lưu user info
                                authManager.saveUserInfo(
                                    userId = userId,
                                    email = userEmail,
                                    name = userName,
                                    role = userRole,
                                    status = userStatus
                                )

                                if (!customToken.isNullOrEmpty()) {
                                    authManager.signInWithCustomToken(customToken) { isSuccessful, idToken, error ->
                                        if (isSuccessful) {
                                            if (!idToken.isNullOrEmpty()) {
                                                // SỬA: Dùng AuthManager để lưu token với expiry time
                                                authManager.saveFirebaseToken(idToken)

                                                Log.d("SignUpViewModel", "✅ Đăng ký & lưu token thành công")

                                                // Debug: In thông tin token
                                                authManager.debugTokenInfo()
                                            } else {
                                                Log.w("SignUpViewModel", "⚠ Firebase ID Token trống")
                                            }
                                        } else {
                                            Log.w("SignUpViewModel", "⚠ Không thể sign in Firebase: $error")
                                        }
                                        _signUpState.postValue(SignUpState.Success)
                                    }
                                } else {
                                    Log.w("SignUpViewModel", "⚠ Custom token trống từ backend")
                                    _signUpState.value = SignUpState.Success
                                }
                            } else {
                                val errorMsg = apiResponse.message ?: "Không nhận được thông tin người dùng"
                                Log.w("SignUpViewModel", "❌ Invalid user data: $errorMsg")
                                _signUpState.value = SignUpState.Error(errorMsg)
                            }
                        } else {
                            val errorMsg = apiResponse.message ?: "Đăng ký thất bại"
                            Log.e("SignUpViewModel", "❌ Outer failure: $errorMsg")
                            _signUpState.value = SignUpState.Error(errorMsg)
                        }
                    }

                    is ApiResult.Failure -> {
                        Log.e("SignUpViewModel", "❌ Repository error", result.exception)
                        val errorMsg = result.exception.message ?: "Đăng ký thất bại"
                        _signUpState.value = SignUpState.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("Lỗi không xác định: ${e.message}")
            }
        }
    }

    fun resetStates() {
        _signUpState.value = SignUpState.Idle
        _saveUserState.value = null
    }

    fun validateInput(fullName: String, email: String, password: String, confirmPassword: String): ValidationResult {
        return when {
            fullName.isBlank() -> ValidationResult.Error("Vui lòng nhập họ và tên")
            email.isBlank() -> ValidationResult.Error("Vui lòng nhập email")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error("Email không hợp lệ")
            password.isBlank() -> ValidationResult.Error("Vui lòng nhập mật khẩu")
            password.length < 6 -> ValidationResult.Error("Mật khẩu phải có ít nhất 6 ký tự")
            password != confirmPassword -> ValidationResult.Error("Mật khẩu xác nhận không khớp")
            else -> ValidationResult.Success
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SignUpViewModel(
                        UserFirebaseRepository(context),
                        AuthRepository(),
                        context
                    ) as T
                }
            }
        }
    }
}