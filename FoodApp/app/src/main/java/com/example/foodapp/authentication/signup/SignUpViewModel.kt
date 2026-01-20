package com.example.foodapp.authentication.signup

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.shared.AuthRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.model.shared.auth.ApiResult
import kotlinx.coroutines.launch

class SignUpViewModel(
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
        val validationResult = validateInput(displayName, email, password, confirmPassword)
        if (validationResult is ValidationResult.Error) {
            _signUpState.value = SignUpState.Error(validationResult.message)
            return
        }
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            try {
                // Gọi repository với model mới
                val result = authRepository.register(email, displayName, password)

                when (result) {
                    is ApiResult.Success -> {
                        val authData = result.data

                        // Kiểm tra nếu authData hợp lệ
                        if (authData.isValid) {
                            val userInfo = authData.user
                            val customToken = authData.customToken

                            // SỬA: Dùng AuthManager để lưu user info
                            authManager.saveUserInfo(
                                userId = userInfo.id,
                                email = userInfo.email,
                                name = userInfo.displayName,
                                role = userInfo.role,
                                status = userInfo.status
                            )

                            if (customToken.isNotBlank()) {
                                authManager.signInWithCustomToken(customToken) { isSuccessful, idToken, error ->
                                    if (isSuccessful) {
                                        if (!idToken.isNullOrEmpty()) {
                                            // SỬA: Dùng AuthManager để lưu token với expiry time
                                            authManager.saveFirebaseToken(idToken)

                                            // Debug: In thông tin token
                                            authManager.debugTokenInfo()

                                            _signUpState.postValue(SignUpState.Success)
                                        } else {
                                            _signUpState.postValue(SignUpState.Success) // Vẫn success vì đã có user info
                                        }
                                    } else {
                                        _signUpState.postValue(SignUpState.Success) // Vẫn success vì đã có user info
                                    }
                                }
                            } else {
                                _signUpState.value = SignUpState.Success // Đã có user info từ API
                            }
                        } else {
                            _signUpState.value = SignUpState.Error("Dữ liệu người dùng không hợp lệ")
                        }
                    }

                    is ApiResult.Failure -> {
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
                        AuthRepository(),
                        context
                    ) as T
                }
            }
        }
    }
}