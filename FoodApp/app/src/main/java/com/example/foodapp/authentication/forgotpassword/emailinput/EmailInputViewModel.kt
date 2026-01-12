package com.example.foodapp.authentication.forgotpassword.emailinput

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

sealed class ForgotPasswordEmailState {
    object Idle : ForgotPasswordEmailState()
    object Loading : ForgotPasswordEmailState()
    object Success : ForgotPasswordEmailState()
    data class Error(val message: String) : ForgotPasswordEmailState()
}


sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

class EmailInputViewModel(
    private val firebaseRepository: UserFirebaseRepository,
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableLiveData<ForgotPasswordEmailState>(ForgotPasswordEmailState.Idle)
    val state: LiveData<ForgotPasswordEmailState> = _state

    private val _email = MutableLiveData<String>("")
    val email: LiveData<String> = _email

    private val _emailError = MutableLiveData<String?>(null)
    val emailError: LiveData<String?> = _emailError

    // Hàm validate email
    private fun validateEmail(email: String): Boolean {
        return when {
            email.isBlank() -> {
                _emailError.value = "Vui lòng nhập email"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _emailError.value = "Email không hợp lệ"
                false
            }
            email.length > 100 -> {
                _emailError.value = "Email quá dài (tối đa 100 ký tự)"
                false
            }
            else -> {
                _emailError.value = null
                true
            }
        }
    }

    // Cập nhật email và tự động validate
    fun setEmail(email: String) {
        _email.value = email
        // Clear error khi người dùng đang nhập
        if (_emailError.value != null && email.isNotEmpty()) {
            _emailError.value = null
        }
    }


    private fun saveEmailToPrefs(email: String) {
        try {
            val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            sharedPref.edit()
                .putString("reset_password_email", email)
                .apply()
            Log.d("EmailInputViewModel", "Đã lưu email vào prefs: $email")
        } catch (e: Exception) {
            Log.e("EmailInputViewModel", "Lỗi khi lưu email", e)
        }
    }


    fun inputEmail() {
        val currentEmail = _email.value ?: ""

        // Validate email trước
        if (!validateEmail(currentEmail)) {
            return
        }

        _state.value = ForgotPasswordEmailState.Loading

        // Kiểm tra email có tồn tại trong database không
        firebaseRepository.checkEmailExists(currentEmail) { exists ->
            if (exists) {
                // Nếu email tồn tại -> Success
                _state.value = ForgotPasswordEmailState.Success
                Log.d("EmailInputViewModel", "Email tồn tại: $currentEmail")

                // Lưu email để sử dụng ở màn hình tiếp theo
                saveEmailToPrefs(currentEmail)
            } else {
                // Nếu email không tồn tại -> Error
                _state.value = ForgotPasswordEmailState.Error("Email chưa được đăng ký trong hệ thống")
                Log.d("EmailInputViewModel", "Email không tồn tại: $currentEmail")
            }
        }
    }


    // Reset state
    fun resetState() {
        _state.value = ForgotPasswordEmailState.Idle
        _emailError.value = null
    }

    // Clear all data
    fun clear() {
        _email.value = ""
        _emailError.value = null
        _state.value = ForgotPasswordEmailState.Idle
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EmailInputViewModel(
                        UserFirebaseRepository(context),
                        AuthRepository(),
                        context
                    ) as T
                }
            }
        }
    }
}
