package com.example.foodapp.authentication.login

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.shared.auth.*
import com.example.foodapp.data.repository.firebase.UserFirebaseRepository
import com.example.foodapp.data.repository.shared.AuthRepository
import com.example.foodapp.data.repository.firebase.AuthManager
import com.example.foodapp.data.repository.client.notification.NotificationRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay


class LoginViewModel(
    private val repository: UserFirebaseRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val context: Context
) : ViewModel() {

    // Thêm AuthManager
    private val authManager = AuthManager(context)

    private var googleSignInClient: GoogleSignInClient? = null

    // State flows
    private val _logInState = MutableStateFlow<LogInState>(LogInState.Idle)
    val logInState: StateFlow<LogInState> = _logInState.asStateFlow()

    private val _googleLogInState = MutableStateFlow<GoogleLogInState>(GoogleLogInState.Idle)
    val googleLogInState: StateFlow<GoogleLogInState> = _googleLogInState.asStateFlow()

    private val _existAccountState = MutableStateFlow<Boolean?>(null)
    val existAccountState: StateFlow<Boolean?> = _existAccountState.asStateFlow()

    // ============= GOOGLE SIGN-IN =============


    fun initializeGoogleSignIn(client: GoogleSignInClient) {
        this.googleSignInClient = client
    }
    fun getGoogleSignInIntent() = googleSignInClient?.signInIntent

    /**
     * Xử lý khi nhấn nút Google Sign-In
     */
    fun onGoogleSignInButtonClicked(): Boolean {
        return try {
            _googleLogInState.value = GoogleLogInState.Loading
            googleSignInClient?.signInIntent != null
        } catch (e: Exception) {
            _googleLogInState.value = GoogleLogInState.Error(
                "Không thể khởi tạo Google Sign-In",
                "GOOGLE_INIT_ERROR"
            )
            false
        }
    }

    /**
     * Xử lý kết quả từ Google Sign-In Activity
     */
    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            _googleLogInState.value = GoogleLogInState.Loading

            try {
                val account = task.getResult(ApiException::class.java)
                handleGoogleAccount(account)
            } catch (e: ApiException) {
                handleGoogleSignInError(e)
            } catch (e: Exception) {
                _googleLogInState.value = GoogleLogInState.Error(
                    "Lỗi đăng nhập Google: ${e.message}",
                    "UNKNOWN_ERROR"
                )
            }
        }
    }

    private suspend fun handleGoogleAccount(account: GoogleSignInAccount) {
        val idToken = account.idToken

        if (idToken == null) {
            _googleLogInState.value = GoogleLogInState.Error(
                "Không thể lấy ID Token từ Google",
                "MISSING_TOKEN"
            )
            return
        }
        signInWithGoogleToken(idToken)
    }

    private suspend fun signInWithGoogleToken(idToken: String) {
        try {
            val result = authRepository.signInWithGoogle(idToken)

            when (result) {
                is ApiResult.Success -> {
                    // result.data bây giờ là AuthData (trực tiếp)
                    val authData = result.data

                    if (authData.isValid) {
                        val userInfo = authData.user

                        // Lưu thông tin user
                        authManager.saveUserInfo(
                            userId = userInfo.id,
                            email = userInfo.email,
                            name = userInfo.displayName,
                            role = userInfo.role,
                            status = userInfo.status
                        )

                        // Xử lý custom token để sign in Firebase
                        handleGoogleCustomToken(authData.customToken, userInfo)

                    } else {
                        _googleLogInState.value = GoogleLogInState.Error(
                            "Dữ liệu người dùng không hợp lệ",
                            "INVALID_USER_DATA"
                        )
                    }
                }

                is ApiResult.Failure -> {
                    val errorMsg = parseGoogleErrorMessage(result.exception)
                    _googleLogInState.value = GoogleLogInState.Error(
                        errorMsg.first,
                        errorMsg.second
                    )
                }
            }
        } catch (e: Exception) {
            _googleLogInState.value = GoogleLogInState.Error(
                "Lỗi kết nối: ${e.message ?: "Vui lòng thử lại sau"}",
                "NETWORK_ERROR"
            )
        }
    }

    private fun handleGoogleCustomToken(customToken: String, userInfo: UserInfo) {
        authManager.signInWithCustomToken(customToken) { isSuccessful, idToken, error ->
            if (isSuccessful) {
                if (!idToken.isNullOrEmpty()) {
                    authManager.saveFirebaseToken(idToken)
                    Log.d("LoginViewModel", "✅ Đã lưu Firebase token: ${idToken.take(10)}...")

                    // Cập nhật token cho ApiClient ngay lập tức
                    updateApiClientToken(idToken)
                }

                // Cập nhật state
                _googleLogInState.value = GoogleLogInState.Success(
                    userId = userInfo.id,
                    email = userInfo.email,
                    displayName = userInfo.displayName,
                    role = userInfo.role
                )
                _existAccountState.value = true

                // GỌI ĐĂNG KÝ DEVICE TOKEN (sau khi đã cập nhật token)
                delayAndRegisterDeviceToken()

            } else {
                // Vẫn coi là thành công vì đã có user info
                _googleLogInState.value = GoogleLogInState.Success(
                    userId = userInfo.id,
                    email = userInfo.email,
                    displayName = userInfo.displayName,
                    role = userInfo.role
                )
                _existAccountState.value = true
            }
        }
    }

    /**
     * Xử lý lỗi từ Google Sign-In
     */
    private fun handleGoogleSignInError(exception: ApiException) {
        val (errorMessage, errorCode) = parseGoogleApiException(exception)
        _googleLogInState.value = GoogleLogInState.Error(errorMessage, errorCode)
    }

    /**
     * Parse lỗi từ Google ApiException
     */
    private fun parseGoogleApiException(exception: ApiException): Pair<String, String> {
        return when (exception.statusCode) {
            4 -> Pair("Không thể kết nối đến Google", "NETWORK_ERROR")
            7 -> Pair("Lỗi kết nối mạng", "NETWORK_UNAVAILABLE")
            8 -> Pair("Lỗi nội bộ", "INTERNAL_ERROR")
            10 -> Pair("Tài khoản không hợp lệ", "INVALID_ACCOUNT")
            13 -> Pair("Timeout", "TIMEOUT")
            14 -> Pair("Yêu cầu đăng nhập lại", "SIGN_IN_REQUIRED")
            16 -> Pair("Đã hủy", "CANCELLED")
            17 -> Pair("API không khả dụng", "API_UNAVAILABLE")
            12501 -> Pair("Đăng nhập bị hủy", "SIGN_IN_CANCELLED")
            12502 -> Pair("Đăng nhập hiện tại đang chờ", "IN_PROGRESS")
            else -> Pair("Đăng nhập Google thất bại: ${exception.message}", "UNKNOWN")
        }
    }

    /**
     * Parse thông báo lỗi từ API Google
     */
    private fun parseGoogleErrorMessage(exception: Exception): Pair<String, String> {
        val message = exception.message ?: "Đăng nhập Google thất bại"

        return when {
            message.contains("401", ignoreCase = true) ->
                Pair("Token Google không hợp lệ hoặc đã hết hạn", "INVALID_TOKEN")
            message.contains("id-token-expired", ignoreCase = true) ->
                Pair("Token Google đã hết hạn. Vui lòng đăng nhập lại", "TOKEN_EXPIRED")
            message.contains("invalid-id-token", ignoreCase = true) ->
                Pair("Token Google không hợp lệ", "INVALID_TOKEN")
            message.contains("Network", ignoreCase = true) ->
                Pair("Lỗi mạng. Vui lòng kiểm tra kết nối", "NETWORK_ERROR")
            message.contains("Timeout", ignoreCase = true) ->
                Pair("Kết nối quá lâu. Vui lòng thử lại", "TIMEOUT")
            message.contains("no internet", ignoreCase = true) ->
                Pair("Không có kết nối internet", "NO_INTERNET")
            else -> Pair(message, "API_ERROR")
        }
    }

    /**
     * Đăng xuất Google
     */
    fun signOutGoogle() {
        googleSignInClient?.signOut()?.addOnCompleteListener {
            // Clear user info
            authManager.clearAuthData()

            _googleLogInState.value = GoogleLogInState.Idle
            _existAccountState.value = null
        }
    }

    // -------------------ĐĂNG NHẬP BẰNG EMAIL/PASSWORD -----------------------
    fun login(email: String, password: String) {
        when (val validation = validateInput(email, password)) {
            is ValidationResult.Error -> {
                _logInState.value = LogInState.Error(validation.message, "VALIDATION_ERROR")
                return
            }
            else -> {
                // Tiếp tục xử lí
            }
        }

        viewModelScope.launch {
            _logInState.value = LogInState.Loading
            try {
                val result = authRepository.login(email, password)

                when (result) {
                    is ApiResult.Success -> {
                        // result.data bây giờ là AuthData (trực tiếp)
                        val authData = result.data

                        if (authData.isValid) {
                            val userInfo = authData.user

                            // Kiểm tra trạng thái tài khoản
                            when {
                                userInfo.status.equals("BANNED", ignoreCase = true) -> {
                                    _logInState.value = LogInState.Error("Tài khoản đã bị khóa", "ACCOUNT_BANNED")
                                }
                                !userInfo.isActive() -> {
                                    _logInState.value = LogInState.Error("Tài khoản chưa được kích hoạt", "ACCOUNT_INACTIVE")
                                }
                                else -> {
                                    // Lưu thông tin user
                                    authManager.saveUserInfo(
                                        userId = userInfo.id,
                                        email = userInfo.email,
                                        name = userInfo.displayName,
                                        role = userInfo.role,
                                        status = userInfo.status
                                    )

                                    // Xử lý custom token để sign in Firebase
                                    handleLoginCustomToken(authData.customToken, userInfo)
                                }
                            }
                        } else {
                            _logInState.value = LogInState.Error(
                                "Dữ liệu đăng nhập không hợp lệ",
                                "INVALID_LOGIN_DATA"
                            )
                        }
                    }

                    is ApiResult.Failure -> {
                        val errorMsg = parseErrorMessage(result.exception.message)
                        _logInState.value = LogInState.Error(errorMsg, "LOGIN_FAILED")
                    }
                }
            } catch (e: Exception) {
                _logInState.value = LogInState.Error(
                    "Lỗi hệ thống: ${e.message ?: "Vui lòng thử lại sau"}",
                    "SYSTEM_ERROR"
                )
            }
        }
    }

    private fun handleLoginCustomToken(customToken: String, userInfo: UserInfo) {
        authManager.signInWithCustomToken(customToken) { isSuccessful, idToken, error ->
            if (isSuccessful) {
                if (!idToken.isNullOrEmpty()) {
                    authManager.saveFirebaseToken(idToken)
                    Log.d("LoginViewModel", "✅ Đã lưu Firebase token: ${idToken.take(10)}...")

                    // Cập nhật token cho ApiClient ngay lập tức
                    updateApiClientToken(idToken)
                }

                _logInState.value = LogInState.Success(
                    userId = userInfo.id,
                    email = userInfo.email,
                    displayName = userInfo.displayName,
                    role = userInfo.role
                )
                _existAccountState.value = true

                // GỌI ĐĂNG KÝ DEVICE TOKEN (sau khi đã cập nhật token)
                delayAndRegisterDeviceToken()

            } else {
                // Vẫn coi là thành công vì đã có user info
                _logInState.value = LogInState.Success(
                    userId = userInfo.id,
                    email = userInfo.email,
                    displayName = userInfo.displayName,
                    role = userInfo.role
                )
                _existAccountState.value = true
            }
        }
    }

    /**
     * Cập nhật token cho ApiClient
     */
    private fun updateApiClientToken(token: String) {
        try {
            // Lưu vào SharedPreferences
            val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            sharedPref.edit().putString("firebase_id_token", token).apply()

            // Đồng thời, tạo một static method trong ApiClient để update token
            // Nếu chưa có, bạn cần thêm vào ApiClient:
            // ApiClient.updateCurrentToken(token)

            Log.d("LoginViewModel", "💾 Đã cập nhật token cho ApiClient: ${token.take(10)}...")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "❌ Lỗi khi cập nhật token: ${e.message}")
        }
    }

    /**
     * Đợi một chút rồi mới đăng ký device token
     */
    private fun delayAndRegisterDeviceToken() {
        viewModelScope.launch {
            // Đợi 1 giây để đảm bảo token đã được cập nhật trong ApiClient
            delay(1000)
            registerDeviceTokenForUser()
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

    private fun parseErrorMessage(errorMessage: String?): String {
        return when {
            errorMessage == null -> "Đăng nhập thất bại"
            errorMessage.contains("401", ignoreCase = true) -> "Email hoặc mật khẩu không chính xác"
            errorMessage.contains("403", ignoreCase = true) -> "Truy cập bị từ chối"
            errorMessage.contains("404", ignoreCase = true) -> "Tài khoản không tồn tại"
            errorMessage.contains("Network", ignoreCase = true) -> "Lỗi mạng. Vui lòng kiểm tra kết nối"
            errorMessage.contains("Timeout", ignoreCase = true) -> "Kết nối quá lâu. Vui lòng thử lại"
            errorMessage.contains("no internet", ignoreCase = true) -> "Không có kết nối internet"
            else -> errorMessage
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

    fun validateInput(email: String, password: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Vui lòng nhập email")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error("Email không hợp lệ")
            password.isBlank() -> ValidationResult.Error("Vui lòng nhập mật khẩu")
            password.length < 6 -> ValidationResult.Error("Mật khẩu phải có ít nhất 6 ký tự")
            else -> ValidationResult.Success
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(
                        repository = UserFirebaseRepository(context),
                        authRepository = AuthRepository(),
                        notificationRepository = NotificationRepository(),
                        context = context
                    ) as T
                }
            }
        }
    }
}