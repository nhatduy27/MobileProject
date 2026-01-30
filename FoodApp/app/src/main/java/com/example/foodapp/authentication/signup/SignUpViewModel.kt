package com.example.foodapp.authentication.signup

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.shared.AuthRepository
import com.example.foodapp.data.repository.client.notification.NotificationRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.model.shared.auth.ApiResult
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
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
                                    if (isSuccessful && !idToken.isNullOrEmpty()) {
                                        authManager.saveFirebaseToken(idToken)
                                        authManager.debugTokenInfo()
                                        registerDeviceTokenForUser()
                                    }
                                    // Chỉ set Success 1 lần duy nhất
                                    _signUpState.postValue(SignUpState.Success)
                                }
                            } else {
                                _signUpState.value = SignUpState.Success
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


    private fun registerDeviceTokenForUser() {
        viewModelScope.launch {
            try {
                // Lấy FCM token
                val fcmToken = FirebaseMessaging.getInstance().token.await()

                // Device info
                val deviceModel = android.os.Build.MODEL
                val osVersion = android.os.Build.VERSION.RELEASE

                // Gọi API đăng ký token
                val result = notificationRepository.registerDeviceToken(
                    token = fcmToken,
                    platform = "android",
                    model = deviceModel,
                    osVersion = osVersion
                )

                when (result) {
                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Success -> {
                    }
                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Failure -> {
                        result.exception.printStackTrace()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                        notificationRepository = NotificationRepository(),
                        context
                    ) as T
                }
            }
        }
    }
}