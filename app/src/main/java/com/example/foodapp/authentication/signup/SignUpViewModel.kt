package com.example.foodapp.authentication.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodapp.authentication.login.LoginViewModel
import com.example.foodapp.data.repository.FirebaseRepository

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

class SignUpViewModel(private val repository: FirebaseRepository) : ViewModel() {

    private val _signUpState = MutableLiveData<SignUpState>(SignUpState.Idle)
    val signUpState: LiveData<SignUpState> = _signUpState

    private val _googleSignInState = MutableLiveData<GoogleSignInState>(GoogleSignInState.Idle)
    val googleSignInState: LiveData<GoogleSignInState> = _googleSignInState

    private val _saveUserState = MutableLiveData<Boolean?>(null)
    val saveUserState: LiveData<Boolean?> = _saveUserState

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

    fun authWithGoogle(idToken: String, displayName: String?, email: String?) {
        _googleSignInState.value = GoogleSignInState.Loading
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

    fun saveUserToFirestore(userId: String, fullName: String, email: String) {
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

    fun saveGoogleUserToFirestore(userId: String, displayName: String?, email: String?) {
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

    fun resetStates() {
        _signUpState.value = SignUpState.Idle
        _googleSignInState.value = GoogleSignInState.Idle
        _saveUserState.value = null
    }

    companion object {
        fun factory(context: android.content.Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SignUpViewModel(FirebaseRepository(context)) as T
                }
            }
        }
    }
}