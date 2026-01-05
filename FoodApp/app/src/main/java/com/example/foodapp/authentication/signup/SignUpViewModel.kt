// File: SignUpViewModel.kt
package com.example.foodapp.authentication.signup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import timber.log.Timber

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val userId: String) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

sealed class GoogleSignInState {
    object Idle : GoogleSignInState()
    object Loading : GoogleSignInState()
    data class Success(val userId: String) : GoogleSignInState()
    data class Error(val message: String) : GoogleSignInState()
}

class SignUpViewModel(private val repository: UserFirebaseRepository) : ViewModel() {

    private val _signUpState = MutableLiveData<SignUpState>(SignUpState.Idle)
    val signUpState: LiveData<SignUpState> = _signUpState

    private val _googleSignInState = MutableLiveData<GoogleSignInState>(GoogleSignInState.Idle)
    val googleSignInState: LiveData<GoogleSignInState> = _googleSignInState

    private val _saveUserState = MutableLiveData<Boolean?>(null)
    val saveUserState: LiveData<Boolean?> = _saveUserState

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
                // Bắt đầu loading state
                _googleSignInState.value = GoogleSignInState.Loading
                authWithGoogle(idToken, displayName, email)
            } else {
                _googleSignInState.value = GoogleSignInState.Error("Không thể lấy token từ Google")
            }
        } catch (e: ApiException) {
            Timber.e(e, "Google sign in failed")
            val errorMessage = when (e.statusCode) {
                10 -> "Ứng dụng chưa được cấu hình Google Sign-In"
                12501 -> "Người dùng đã hủy đăng nhập"
                12502 -> "Lỗi mạng hoặc timeout"
                else -> "Đăng nhập Google thất bại (Mã lỗi: ${e.statusCode})"
            }
            _googleSignInState.value = GoogleSignInState.Error(errorMessage)
        }
    }


    fun checkPendingGoogleSignIn(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && googleSignInState.value is GoogleSignInState.Loading
    }

    // Xử lý pending Google sign-in
    fun handlePendingGoogleSignIn(context: Context) {
        if (checkPendingGoogleSignIn(context)) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(null)
                handleGoogleSignInResult(task)
            } catch (e: Exception) {
                _googleSignInState.value = GoogleSignInState.Idle
            }
        }
    }

    // Đăng ký với email/password
    fun registerWithEmail(fullName: String, email: String, password: String) {
        _signUpState.value = SignUpState.Loading
        repository.registerWithEmail(email, password) { isSuccessful, result ->
            if (isSuccessful && result != null) {
                saveUserToFirestore(result, fullName, email)
            } else {
                _signUpState.postValue(SignUpState.Error(result ?: "Đăng ký thất bại"))
            }
        }
    }

    // Xác thực với Google
    private fun authWithGoogle(idToken: String, displayName: String?, email: String?) {
        repository.authWithGoogle(idToken) { isSuccessful, userId ->
            if (isSuccessful && userId != null) {
                repository.checkUserExists(userId) { exists ->
                    if (exists) {
                        _googleSignInState.postValue(GoogleSignInState.Success(userId))
                        _saveUserState.postValue(true)
                    } else {
                        saveGoogleUserToFirestore(userId, displayName, email)
                    }
                }
            } else {
                _googleSignInState.postValue(GoogleSignInState.Error("Xác thực Google thất bại"))
            }
        }
    }

    // Lưu user từ email/password vào Firestore
    private fun saveUserToFirestore(userId: String, fullName: String, email: String) {
        repository.saveUserToFirestore(userId, fullName, email) { isSuccessful, errorMessage ->
            if (isSuccessful) {
                _signUpState.postValue(SignUpState.Success(userId))
                _saveUserState.postValue(true)
            } else {
                _saveUserState.postValue(false)
                _signUpState.postValue(SignUpState.Error(errorMessage ?: "Lỗi lưu dữ liệu"))
            }
        }
    }

    // Lưu user từ Google vào Firestore
    private fun saveGoogleUserToFirestore(userId: String, displayName: String?, email: String?) {
        repository.saveGoogleUserToFirestore(userId, displayName, email) { isSuccessful, errorMessage ->
            if (isSuccessful) {
                _googleSignInState.postValue(GoogleSignInState.Success(userId))
                _saveUserState.postValue(true)
            } else {
                _saveUserState.postValue(false)
                _googleSignInState.postValue(GoogleSignInState.Error(errorMessage ?: "Lỗi lưu dữ liệu Google"))
            }
        }
    }

    // Reset tất cả state
    fun resetStates() {
        _signUpState.value = SignUpState.Idle
        _googleSignInState.value = GoogleSignInState.Idle
        _saveUserState.value = null
    }

    // Validate input data
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

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SignUpViewModel(UserFirebaseRepository(context)) as T
                }
            }
        }
    }
}