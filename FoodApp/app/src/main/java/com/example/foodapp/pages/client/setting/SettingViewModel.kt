package com.example.foodapp.pages.client.setting


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.shared.AuthRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.foodapp.data.model.shared.auth.ApiResult

// Sealed class cho các trạng thái
sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

sealed class DeleteAccountState {
    object Idle : DeleteAccountState()
    object Loading : DeleteAccountState()
    object ConfirmRequired : DeleteAccountState()
    data class Success(val message: String) : DeleteAccountState()
    data class Error(val message: String) : DeleteAccountState()
}


class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager
) : ViewModel() {

    // State cho đổi mật khẩu
    private val _changePasswordState = MutableLiveData<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: LiveData<ChangePasswordState> = _changePasswordState

    // State cho xóa tài khoản
    private val _deleteAccountState = MutableLiveData<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: LiveData<DeleteAccountState> = _deleteAccountState

    // State logout (kế thừa từ ProfileViewModel nếu cần)
    private val _logoutState = MutableLiveData<Boolean>(false)
    val logoutState: LiveData<Boolean> = _logoutState

    // Thông báo tổng quát
    private val _notification = MutableLiveData<String?>()
    val notification: LiveData<String?> = _notification




    //--------------CÁC HÀM LOGIC------------------



    fun changePassword(oldPassword: String, newPassword: String) {
        _changePasswordState.value = ChangePasswordState.Loading
        viewModelScope.launch {
            try {
                // Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken == null) {
                    _changePasswordState.value = ChangePasswordState.Error("Vui lòng đăng nhập lại")
                    return@launch
                }

                // Gọi repository để đổi mật khẩu
                val result = authRepository.changePassword(
                    accessToken = accessToken,
                    oldPassword = oldPassword,
                    newPassword = newPassword
                )

                when (result) {
                    is ApiResult.Success -> {
                        val response = result.data
                        val message = response.message ?: "Đổi mật khẩu thành công"
                        _changePasswordState.value = ChangePasswordState.Success(message)

                        // 4. Reset state sau 2 giây
                        resetPasswordStateAfterDelay()
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Đổi mật khẩu thất bại"
                        _changePasswordState.value = ChangePasswordState.Error(errorMessage)
                    }
                }

            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error("Lỗi hệ thống: ${e.message}")
            }
        }
    }

    private fun resetPasswordStateAfterDelay() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            _changePasswordState.value = ChangePasswordState.Idle
        }
    }

    fun DeleteAccount() {
        _deleteAccountState.value = DeleteAccountState.ConfirmRequired
    }



    //Hàm đăng xuất
    fun logout() {
        viewModelScope.launch {
            try {
                // 1. Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken == null) {
                    handleLocalLogout()
                    return@launch
                }

                // 2. Lấy FCM token nếu có
                val fcmToken = try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    null
                }

                authRepository.logout(
                    accessToken = accessToken,
                    fcmToken = fcmToken
                )
                handleLocalLogout()

            } catch (e: Exception) {
                println("Logout exception: ${e.message}")
                handleLocalLogout()
            }
        }
    }

    private fun handleLocalLogout() {
        // Xóa dữ liệu local
        authManager.clearAuthData()
        _logoutState.value = true
    }




    // ============== FACTORY ==============

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                val authRepository = AuthRepository()
                val authManager = AuthManager(context)
                return SettingsViewModel(authRepository, authManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

        companion object {
            @Volatile
            private var INSTANCE: Factory? = null

            fun getInstance(context: Context): Factory {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: Factory(context.applicationContext).also {
                        INSTANCE = it
                    }
                }
            }
        }
    }
}