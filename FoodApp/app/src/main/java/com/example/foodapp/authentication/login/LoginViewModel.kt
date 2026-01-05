package com.example.foodapp.authentication.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

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
    private val repository: UserFirebaseRepository
) : ViewModel() {

    private val _logInState = MutableStateFlow<LogInState>(LogInState.Idle)
    val logInState: StateFlow<LogInState> = _logInState.asStateFlow()

    private val _googleLogInState = MutableStateFlow<GoogleLogInState>(GoogleLogInState.Idle)
    val googleLogInState: StateFlow<GoogleLogInState> = _googleLogInState.asStateFlow()

    private val _existAccountState = MutableStateFlow<Boolean?>(null)
    val existAccountState: StateFlow<Boolean?> = _existAccountState.asStateFlow()

    // Expose GoogleSignInClient từ repository
    fun getGoogleSignInClient(): GoogleSignInClient {
        return repository.getGoogleSignInClient()
    }

    // Handle Google Sign-In result - TẤT CẢ LOGIC ở đây
    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            val displayName = account.displayName
            val email = account.email

            if (idToken != null) {
                _googleLogInState.value = GoogleLogInState.Loading
                authWithGoogle(idToken, displayName, email)
            } else {
                _googleLogInState.value = GoogleLogInState.Error("Không thể lấy token từ Google")
            }
        } catch (e: ApiException) {
            Timber.e(e, "Google sign in failed")
            val errorMessage = when (e.statusCode) {
                10 -> "Ứng dụng chưa được cấu hình Google Sign-In"
                12501 -> "Người dùng đã hủy đăng nhập"
                12502 -> "Lỗi mạng hoặc timeout"
                else -> "Đăng nhập Google thất bại (Mã lỗi: ${e.statusCode})"
            }
            _googleLogInState.value = GoogleLogInState.Error(errorMessage)
        }
    }

    // Kiểm tra nếu có pending Google sign-in
    fun checkPendingGoogleSignIn(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && googleLogInState.value is GoogleLogInState.Loading
    }

    // Xử lý pending Google sign-in
    fun handlePendingGoogleSignIn(context: Context) {
        if (checkPendingGoogleSignIn(context)) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(null)
                handleGoogleSignInResult(task)
            } catch (e: Exception) {
                _googleLogInState.value = GoogleLogInState.Idle
            }
        }
    }

    // Đăng nhập với email/password
    fun logInWithEmail(email: String, password: String) {
        _logInState.value = LogInState.Loading
        viewModelScope.launch {
            repository.logInWithEmail(email, password) { isSuccessful, result ->
                if (isSuccessful && result != null) {
                    _logInState.value = LogInState.Success(result)
                    _existAccountState.value = true
                } else {
                    _logInState.value = LogInState.Error(result ?: "Đăng nhập thất bại")
                    _existAccountState.value = false
                }
            }
        }
    }

    // Xác thực với Google
    private fun authWithGoogle(idToken: String, displayName: String?, email: String?) {
        viewModelScope.launch {
            repository.authWithGoogle(idToken) { isSuccessful, userId ->
                if (isSuccessful && userId != null) {
                    repository.checkUserExists(userId) { exists ->
                        if (exists) {
                            _googleLogInState.value = GoogleLogInState.Success(userId)
                            _existAccountState.value = true
                        } else {
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

    fun getUserRole(userId: String, onComplete: (String?) -> Unit) {
        repository.getUserRole(userId, onComplete)
    }

    fun resetStates() {
        _logInState.value = LogInState.Idle
        _googleLogInState.value = GoogleLogInState.Idle
        _existAccountState.value = null
    }

    // Validation input
    fun validateInput(email: String, password: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Vui lòng nhập email")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error("Email không hợp lệ")
            password.isBlank() -> ValidationResult.Error("Vui lòng nhập mật khẩu")
            password.length < 6 -> ValidationResult.Error("Mật khẩu phải có ít nhất 6 ký tự")
            else -> ValidationResult.Success
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(UserFirebaseRepository(context)) as T
                }
            }
        }
    }
}