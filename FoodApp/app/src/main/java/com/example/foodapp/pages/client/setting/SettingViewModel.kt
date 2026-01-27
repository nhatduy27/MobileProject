package com.example.foodapp.pages.client.setting

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.repository.shared.AuthRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.model.shared.auth.ApiResult
import com.example.foodapp.data.repository.client.notification.NotificationRepository
import com.example.foodapp.data.remote.client.response.notification.NotificationPreferencesResponse
import com.example.foodapp.data.remote.client.response.notification.UpdateNotificationPreferencesData
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

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

// State cho notification preferences
sealed class NotificationPreferencesState {
    object Idle : NotificationPreferencesState()
    object Loading : NotificationPreferencesState()
    data class Success(val preferences: NotificationPreferencesResponse) : NotificationPreferencesState()
    data class Error(val message: String) : NotificationPreferencesState()
}

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val authManager: AuthManager
) : ViewModel() {

    // State cho đổi mật khẩu
    private val _changePasswordState = MutableLiveData<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: LiveData<ChangePasswordState> = _changePasswordState

    // State cho xóa tài khoản
    private val _deleteAccountState = MutableLiveData<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: LiveData<DeleteAccountState> = _deleteAccountState

    // State logout
    private val _logoutState = MutableLiveData<Boolean>(false)
    val logoutState: LiveData<Boolean> = _logoutState

    // State điều hướng về login (sau khi xóa tài khoản thành công)
    private val _navigateToLogin = MutableLiveData<Boolean>(false)
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    // Thông báo tổng quát
    private val _notification = MutableLiveData<String?>()
    val notification: LiveData<String?> = _notification

    // Biến tạm lưu trạng thái xác nhận
    private var _deleteAccountConfirmed = false

    // State cho notification preferences
    private val _notificationPreferencesState = MutableLiveData<NotificationPreferencesState>(NotificationPreferencesState.Idle)
    val notificationPreferencesState: LiveData<NotificationPreferencesState> = _notificationPreferencesState

    // Data cho notification preferences
    private val _notificationPreferences = MutableLiveData<NotificationPreferencesResponse?>()
    val notificationPreferences: LiveData<NotificationPreferencesResponse?> = _notificationPreferences

    // ==================== NOTIFICATION PREFERENCES ====================

    /**
     * Tải notification preferences từ server
     */
    fun loadNotificationPreferences() {
        _notificationPreferencesState.value = NotificationPreferencesState.Loading

        viewModelScope.launch {
            try {
                // Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken == null) {
                    _notificationPreferencesState.value = NotificationPreferencesState.Error(
                        "Vui lòng đăng nhập lại"
                    )
                    return@launch
                }

                // Gọi API lấy notification preferences
                val result = notificationRepository.getNotificationPreferences(accessToken)

                when (result) {
                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Success -> {
                        val preferences = result.data
                        _notificationPreferences.value = preferences
                        _notificationPreferencesState.value = NotificationPreferencesState.Success(preferences)
                    }

                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Không thể tải cài đặt thông báo"
                        _notificationPreferencesState.value = NotificationPreferencesState.Error(errorMessage)
                    }

                    // Xử lý loading state
                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Loading -> {
                        // Đã set loading state ở đầu, không cần làm gì thêm
                    }
                }
            } catch (e: Exception) {
                _notificationPreferencesState.value = NotificationPreferencesState.Error(
                    "Lỗi hệ thống: ${e.message}"
                )
            }
        }
    }

    /**
     * Cập nhật notification preferences lên server
     */
    fun updateNotificationPreferences(
        informational: Boolean? = null,
        marketing: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                // Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken == null) {
                    showNotification("Vui lòng đăng nhập lại")
                    return@launch
                }

                // Gọi API cập nhật preferences
                val result = notificationRepository.updateNotificationPreferences(
                    accessToken = accessToken,
                    informational = informational,
                    marketing = marketing
                )

                when (result) {
                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Success -> {
                        // Xử lý thành công
                        handleUpdatePreferencesSuccess(result.data)
                    }

                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Failure -> {
                        // Xử lý lỗi
                        handleUpdatePreferencesFailure(result.exception)
                    }

                    // Xử lý loading state
                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Loading -> {
                        // Có thể hiển thị loading indicator nếu cần
                    }
                }

            } catch (e: Exception) {
                handleUpdatePreferencesFailure(e)
            }
        }
    }

    private fun handleUpdatePreferencesSuccess(updateData: UpdateNotificationPreferencesData) {
        // Tạo NotificationPreferencesResponse từ UpdateNotificationPreferencesData
        val notificationPreferences = NotificationPreferencesResponse(
            userId = updateData.userId,
            transactional = updateData.transactional,
            informational = updateData.informational,
            marketing = updateData.marketing,
            updatedAt = updateData.updatedAt
        )

        // Cập nhật cả hai state
        _notificationPreferences.value = notificationPreferences
        _notificationPreferencesState.value = NotificationPreferencesState.Success(notificationPreferences)

        showNotification("Đã cập nhật cài đặt thông báo")
    }

    private fun handleUpdatePreferencesFailure(exception: Exception) {
        val errorMessage = exception.message ?: "Cập nhật thất bại"
        showNotification("Lỗi: $errorMessage")
        _notificationPreferencesState.value = NotificationPreferencesState.Error(errorMessage)
    }

    // ==================== CHANGE PASSWORD ====================

    fun changePassword(oldPassword: String, newPassword: String) {
        _changePasswordState.value = ChangePasswordState.Loading
        viewModelScope.launch {
            try {
                // Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken == null) {
                    _changePasswordState.value = ChangePasswordState.Error("Vui lòng đăng nhập lại")
                    resetChangePasswordStateAfterDelay()
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

                        // Reset state sau 2 giây
                        resetChangePasswordStateAfterDelay()
                    }

                    is ApiResult.Failure -> {
                        val errorMessage = result.exception.message ?: "Đổi mật khẩu thất bại"
                        _changePasswordState.value = ChangePasswordState.Error(errorMessage)
                        resetChangePasswordStateAfterDelay()
                    }
                }

            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error("Lỗi hệ thống: ${e.message}")
                resetChangePasswordStateAfterDelay()
            }
        }
    }

    private fun resetChangePasswordStateAfterDelay() {
        viewModelScope.launch {
            delay(2000)
            _changePasswordState.value = ChangePasswordState.Idle
        }
    }

    // ==================== DELETE ACCOUNT ====================

    // Hiển thị dialog xác nhận xóa tài khoản
    fun showDeleteAccountConfirmation() {
        _deleteAccountState.value = DeleteAccountState.ConfirmRequired
    }

    // Xác nhận xóa tài khoản (sau khi người dùng đã confirm)
    fun confirmDeleteAccount() {
        if (_deleteAccountState.value == DeleteAccountState.ConfirmRequired) {
            performDeleteAccount()
        }
    }

    // Hủy xóa tài khoản
    fun cancelDeleteAccount() {
        _deleteAccountState.value = DeleteAccountState.Idle
    }

    // Thực hiện xóa tài khoản
    private fun performDeleteAccount() {
        _deleteAccountState.value = DeleteAccountState.Loading
        viewModelScope.launch {
            try {
                // 1. Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken == null) {
                    _deleteAccountState.value = DeleteAccountState.Error(
                        "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại"
                    )
                    resetDeleteAccountStateAfterDelay()
                    return@launch
                }

                // 2. Gọi repository để xóa tài khoản
                val result = authRepository.deleteAccount(accessToken)

                when (result) {
                    is ApiResult.Success -> {
                        val response = result.data
                        if (response.success) {
                            val message = response.message ?: "Tài khoản đã được xóa thành công"

                            // 3. Xử lý sau khi xóa thành công
                            onDeleteAccountSuccess(message)
                        } else {
                            // Trường hợp success = false trong response
                            val errorMessage = response.message ?: "Xóa tài khoản thất bại"
                            _deleteAccountState.value = DeleteAccountState.Error(errorMessage)
                            resetDeleteAccountStateAfterDelay()
                        }
                    }

                    is ApiResult.Failure -> {
                        // 4. Xử lý lỗi chi tiết hơn
                        handleDeleteAccountFailure(result.exception)
                    }
                }

            } catch (e: Exception) {
                _deleteAccountState.value = DeleteAccountState.Error(
                    "Lỗi hệ thống: ${e.message ?: "Không xác định"}"
                )
                resetDeleteAccountStateAfterDelay()
            }
        }
    }

    private suspend fun onDeleteAccountSuccess(message: String) {
        // 1. Xóa FCM token để không nhận thông báo nữa
        revokeFCMToken()

        // 2. Xóa tất cả dữ liệu local
        clearAllLocalData()

        // 3. Cập nhật state thành công
        _deleteAccountState.value = DeleteAccountState.Success(message)

        // 4. Delay 2 giây rồi điều hướng về login
        delay(2000)
        _navigateToLogin.value = true

        // 5. Reset state về idle
        _deleteAccountState.value = DeleteAccountState.Idle
    }

    private suspend fun revokeFCMToken() {
        try {
            FirebaseMessaging.getInstance().deleteToken().await()
        } catch (e: Exception) {
            // Log lỗi nhưng không ảnh hưởng đến flow chính
            println("Failed to revoke FCM token: ${e.message}")
        }
    }

    private fun clearAllLocalData() {
        // Xóa dữ liệu auth
        authManager.clearAuthData()

        // Clear notification preferences
        _notificationPreferences.value = null
        _notificationPreferencesState.value = NotificationPreferencesState.Idle
    }

    private fun handleDeleteAccountFailure(exception: Exception) {
        val errorMessage = when {
            exception.message?.contains("401") == true ->
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại"
            exception.message?.contains("403") == true ->
                "Bạn không có quyền xóa tài khoản"
            exception.message?.contains("404") == true ->
                "Tài khoản không tồn tại"
            exception.message?.contains("409") == true ->
                "Không thể xóa tài khoản vì có đơn hàng đang xử lý"
            exception.message?.contains("500") == true ->
                "Lỗi máy chủ. Vui lòng thử lại sau"
            exception.message?.contains("mạng") == true ||
                    exception.message?.contains("kết nối") == true ->
                "Vui lòng kiểm tra kết nối internet"
            else -> exception.message ?: "Không thể xóa tài khoản lúc này"
        }

        _deleteAccountState.value = DeleteAccountState.Error(errorMessage)
        resetDeleteAccountStateAfterDelay()
    }

    private fun resetDeleteAccountStateAfterDelay() {
        viewModelScope.launch {
            delay(2000)
            // Chỉ reset nếu không phải trạng thái Success
            if (_deleteAccountState.value !is DeleteAccountState.Success) {
                _deleteAccountState.value = DeleteAccountState.Idle
            }
        }
    }

    // Reset state về idle (gọi từ UI khi cần)
    fun resetDeleteAccountState() {
        if (_deleteAccountState.value !is DeleteAccountState.Success) {
            _deleteAccountState.value = DeleteAccountState.Idle
        }
    }

    fun resetNavigateToLogin() {
        _navigateToLogin.value = false
    }

    // ==================== LOGOUT ====================

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
                unRegisterDeviceTokenForUser()
                handleLocalLogout()

            } catch (e: Exception) {
                println("Logout exception: ${e.message}")
                handleLocalLogout()
            }
        }
    }

    private fun unRegisterDeviceTokenForUser() {
        viewModelScope.launch {
            try {
                // 1. Lấy FCM token
                val fcmToken = FirebaseMessaging.getInstance().token.await()

                // 2. Lấy access token từ AuthManager
                val accessToken = authManager.getCurrentToken()

                if (accessToken != null && fcmToken.isNotBlank()) {
                    notificationRepository.unRegisterDeviceToken(
                        accessToken = accessToken,
                        fcmToken = fcmToken
                    )
                } else {
                    if (accessToken == null) {
                        println("DEBUG: Cannot unregister FCM token - access token is null")
                    }
                    if (fcmToken.isBlank()) {
                        println("DEBUG: Cannot unregister FCM token - FCM token is empty")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleLocalLogout() {
        // Xóa dữ liệu local
        authManager.clearAuthData()

        // Clear notification preferences khi logout
        _notificationPreferences.value = null
        _notificationPreferencesState.value = NotificationPreferencesState.Idle
        _logoutState.value = true
    }

    // Reset logout state
    fun resetLogoutState() {
        _logoutState.value = false
    }

    // ==================== NOTIFICATION ====================

    fun showNotification(message: String) {
        _notification.value = message
        viewModelScope.launch {
            delay(3000)
            _notification.value = null
        }
    }

    // ==================== FACTORY ====================

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                        return SettingsViewModel(
                            authRepository = AuthRepository(),
                            notificationRepository = NotificationRepository(),
                            authManager = AuthManager(context)
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}