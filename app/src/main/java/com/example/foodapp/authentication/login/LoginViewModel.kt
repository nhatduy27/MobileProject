package com.example.foodapp.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LogInState {
    object Idle : LogInState()
    object Loading : LogInState()
    data class Success(val userId: String) : LogInState()
    data class Error(val message: String) : LogInState()
}

sealed class GoogleLogInState {
    object Idle : GoogleLogInState()
    object Loading : GoogleLogInState()
    data class Success(val userId: String) : GoogleLogInState()
    data class Error(val message: String) : GoogleLogInState()
}

class LoginViewModel(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _logInState = MutableStateFlow<LogInState>(LogInState.Idle)
    val logInState: StateFlow<LogInState> = _logInState.asStateFlow()

    private val _googleLogInState = MutableStateFlow<GoogleLogInState>(GoogleLogInState.Idle)
    val googleLogInState: StateFlow<GoogleLogInState> = _googleLogInState.asStateFlow()

    private val _existAccountState = MutableStateFlow<Boolean?>(null)
    val existAccountState: StateFlow<Boolean?> = _existAccountState.asStateFlow()

    fun logInWithEmail(email: String, password: String) {
        _logInState.value = LogInState.Loading
        viewModelScope.launch {
            repository.logInWithEmail(email, password) { isSuccessful, result ->
                if (isSuccessful && result != null) {
                    _logInState.value = LogInState.Success(result)
                    _existAccountState.value = true // Kích hoạt chuyển trang
                } else {
                    _logInState.value = LogInState.Error(result ?: "Đăng nhập thất bại")
                    _existAccountState.value = false
                }
            }
        }
    }

    fun authWithGoogle(idToken: String, displayName: String?, email: String?) {
        _googleLogInState.value = GoogleLogInState.Loading
        viewModelScope.launch {
            repository.authWithGoogle(idToken) { isSuccessful, userId ->
                if (isSuccessful && userId != null) {
                    // Sau khi Auth Google, kiểm tra Firestore xem đã có thông tin chưa
                    repository.checkUserExists(userId) { exists ->
                        if (exists) {
                            _googleLogInState.value = GoogleLogInState.Success(userId)
                            _existAccountState.value = true
                        } else {
                            // Nếu chưa có (người dùng mới đăng nhập bằng Google lần đầu), lưu vào Firestore
                            saveGoogleUserToFirestore(userId, displayName, email)
                        }
                    }
                } else {
                    _googleLogInState.value = GoogleLogInState.Error("Xác thực Google thất bại")
                }
            }
        }
    }

    private fun saveGoogleUserToFirestore(userId: String, displayName: String?, email: String?) {
        repository.saveGoogleUserToFirestore(userId, displayName, email) { isSuccessful, errorMessage ->
            if (isSuccessful) {
                _googleLogInState.value = GoogleLogInState.Success(userId)
                _existAccountState.value = true
            } else {
                _googleLogInState.value = GoogleLogInState.Error(errorMessage ?: "Lỗi lưu dữ liệu")
            }
        }
    }

    fun resetStates() {
        _logInState.value = LogInState.Idle
        _googleLogInState.value = GoogleLogInState.Idle
        _existAccountState.value = null
    }

    companion object {
        fun factory(context: android.content.Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(FirebaseRepository(context)) as T
                }
            }
        }
    }
}