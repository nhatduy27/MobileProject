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
import com.example.foodapp.data.model.shared.auth.ApiResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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
                            val innerSuccess = apiResponse.data?.success ?: false
                            val registerData = apiResponse.data?.data
                            val userInfo = registerData?.user

                            if (innerSuccess && userInfo != null && userInfo.isValid) {

                                saveUserInfoLocally(userInfo)

                                // 2. Đăng nhập Firebase với customToken (nếu có)
                                val customToken = registerData.customToken
                                if (!customToken.isNullOrEmpty()) {
                                    repository.signInWithCustomToken(customToken) { isSuccessful, error ->
                                        if (isSuccessful) {
                                            Log.d("SignUpViewModel", "Đã sign in Firebase thành công")
                                        } else {
                                            Log.w("SignUpViewModel", "⚠Không thể sign in Firebase: $error")
                                        }
                                        _signUpState.postValue(SignUpState.Success)
                                    }
                                } else {
                                    _signUpState.value = SignUpState.Success
                                }
                            } else {
                                val errorMsg = apiResponse.data?.message ?: "Không nhận được thông tin người dùng"
                                Log.w("SignUpViewModel", "❌ Inner failure: $errorMsg")
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
                Log.e("SignUpViewModel", "❌ Unexpected error", e)
                _signUpState.value = SignUpState.Error("Lỗi không xác định: ${e.message}")
            }
        }
    }

    private fun saveUserInfoLocally(userInfo: com.example.foodapp.data.model.shared.auth.UserInfo) {
        try {
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("user_id", userInfo.id)
            editor.putString("user_email", userInfo.email)
            editor.putString("user_name", userInfo.displayName)
            editor.putString("user_role", userInfo.role)
            editor.putString("user_status", userInfo.status)
            editor.apply()
        } catch (e: Exception) {
            Log.e("SignUpViewModel", "Lỗi khi lưu user info", e)
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