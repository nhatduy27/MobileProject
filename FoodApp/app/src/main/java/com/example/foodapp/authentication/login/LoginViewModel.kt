package com.example.foodapp.authentication.login

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.model.client.Client
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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

    private val authManager = AuthManager(context)
    private val firebaseAuth = FirebaseAuth.getInstance()
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
        val googleIdToken = account.idToken

        if (googleIdToken == null) {
            _googleLogInState.value = GoogleLogInState.Error(
                "Không thể lấy ID Token từ Google",
                "MISSING_TOKEN"
            )
            return
        }

        Log.d("LoginViewModel", "📱 Google ID Token: ${googleIdToken.take(20)}...")
        Log.d("LoginViewModel", "📧 Google Account Email: ${account.email}")

        try {
            // 1. Sign in với Firebase để lấy Firebase ID token
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            // 2. Lấy Firebase ID token
            val tokenResult = authResult.user?.getIdToken(true)?.await()
            val firebaseIdToken = tokenResult?.token

            if (firebaseIdToken == null) {
                _googleLogInState.value = GoogleLogInState.Error(
                    "Không thể lấy Firebase ID token",
                    "FIREBASE_TOKEN_ERROR"
                )
                return
            }

            Log.d("LoginViewModel", "✅ Firebase ID token: ${firebaseIdToken.take(20)}...")
            Log.d("LoginViewModel", "👤 Firebase User UID: ${authResult.user?.uid}")

            // 3. Gửi Firebase ID token lên backend của bạn
            signInWithGoogleToken(firebaseIdToken, "CUSTOMER")

        } catch (e: Exception) {
            Log.e("LoginViewModel", "❌ Lỗi xác thực Firebase", e)
            _googleLogInState.value = GoogleLogInState.Error(
                "Lỗi xác thực Firebase: ${e.message ?: "Không rõ nguyên nhân"}",
                "FIREBASE_AUTH_ERROR"
            )
        }
    }

    private suspend fun signInWithGoogleToken(firebaseIdToken: String, role: String = "CUSTOMER") {
        try {
            // Gọi repository với Firebase ID token
            val result = authRepository.signInWithGoogle(firebaseIdToken, role)

            when (result) {
                is ApiResult.Success -> {
                    val client = result.data

                    // Lưu thông tin user từ Client
                    authManager.saveUserInfo(
                        userId = client.id,
                        email = client.email,
                        name = client.fullName ?: client.email.split("@")[0],
                        role = client.role ?: role,
                        status = "ACTIVE"
                    )

                    // Lưu Firebase token
                    authManager.saveFirebaseToken(firebaseIdToken)

                    // Cập nhật state thành công
                    updateGoogleSignInSuccess(client, role)

                    Log.d("LoginViewModel", "✅ Google Sign-In thành công: ${client.email}")
                }

                is ApiResult.Failure -> {
                    val errorMsg = parseGoogleApiErrorMessage(result.exception)
                    _googleLogInState.value = GoogleLogInState.Error(
                        errorMsg.first,
                        errorMsg.second
                    )
                    Log.e("LoginViewModel", "❌ Google Sign-In thất bại: ${result.exception.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "❌ Lỗi kết nối API", e)
            _googleLogInState.value = GoogleLogInState.Error(
                "Lỗi kết nối: ${e.message ?: "Vui lòng thử lại sau"}",
                "NETWORK_ERROR"
            )
        }
    }

    private fun updateGoogleSignInSuccess(client: Client, role: String) {
        _googleLogInState.value = GoogleLogInState.Success(
            userId = client.id,
            email = client.email,
            displayName = client.fullName ?: client.email.split("@")[0],
            role = client.role ?: role
        )
        _existAccountState.value = true

        // Cập nhật ApiClient token
        val currentToken = authManager.getCurrentToken()
        if (!currentToken.isNullOrEmpty()) {
            updateApiClientToken(currentToken)
        }

        // Đăng ký device token
        delayAndRegisterDeviceToken()
    }

    /**
     * Parse lỗi từ Google ApiException
     */
    private fun handleGoogleSignInError(exception: ApiException) {
        val (errorMessage, errorCode) = parseGoogleApiException(exception)
        _googleLogInState.value = GoogleLogInState.Error(errorMessage, errorCode)
        Log.e("LoginViewModel", "❌ Google Sign-In API Error: $errorCode - $errorMessage")
    }

    private fun parseGoogleApiException(exception: ApiException): Pair<String, String> {
        return when (exception.statusCode) {
            4 -> Pair("Không thể kết nối đến Google", "NETWORK_ERROR")
            7 -> Pair("Lỗi kết nối mạng", "NETWORK_UNAVAILABLE")
            8 -> Pair("Lỗi nội bộ", "INTERNAL_ERROR")
            10 -> Pair("Tài khoản không hợp lệ", "INVALID_ACCOUNT")
            13 -> Pair("Timeout", "TIMEOUT")
            14 -> Pair("Yêu cầu đăng nhập lại", "SIGN_IN_REQUIRED")
            16 -> Pair("Đăng nhập bị hủy", "CANCELLED")
            17 -> Pair("API không khả dụng", "API_UNAVAILABLE")
            12501 -> Pair("Đăng nhập Google bị hủy", "SIGN_IN_CANCELLED")
            12502 -> Pair("Đăng nhập hiện tại đang chờ", "IN_PROGRESS")
            else -> Pair("Đăng nhập Google thất bại: ${exception.message}", "UNKNOWN_ERROR")
        }
    }

    /**
     * Parse thông báo lỗi từ API Google Sign-In
     */
    private fun parseGoogleApiErrorMessage(exception: Exception): Pair<String, String> {
        val message = exception.message ?: "Đăng nhập Google thất bại"

        return when {
            message.contains("Firebase ID token has incorrect", ignoreCase = true) ->
                Pair("Token không hợp lệ. Vui lòng thử lại", "INVALID_FIREBASE_TOKEN")
            message.contains("id-token-expired", ignoreCase = true) ->
                Pair("Token đã hết hạn. Vui lòng đăng nhập lại", "TOKEN_EXPIRED")
            message.contains("invalid-id-token", ignoreCase = true) ->
                Pair("Token không hợp lệ", "INVALID_TOKEN")
            message.contains("400", ignoreCase = true) ->
                Pair("Yêu cầu không hợp lệ", "BAD_REQUEST")
            message.contains("401", ignoreCase = true) ->
                Pair("Token không hợp lệ hoặc đã hết hạn", "UNAUTHORIZED")
            message.contains("429", ignoreCase = true) ->
                Pair("Quá nhiều yêu cầu. Vui lòng thử lại sau", "TOO_MANY_REQUESTS")
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
            firebaseAuth.signOut()
            authManager.clearAuthData()
            _googleLogInState.value = GoogleLogInState.Idle
            _existAccountState.value = null
            Log.d("LoginViewModel", "✅ Đã đăng xuất Google")
        }
    }

    // ============= ĐĂNG NHẬP EMAIL/PASSWORD =============

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
                    updateApiClientToken(idToken)
                }

                _logInState.value = LogInState.Success(
                    userId = userInfo.id,
                    email = userInfo.email,
                    displayName = userInfo.displayName,
                    role = userInfo.role
                )
                _existAccountState.value = true
                delayAndRegisterDeviceToken()

            } else {
                _logInState.value = LogInState.Success(
                    userId = userInfo.id,
                    email = userInfo.email,
                    displayName = userInfo.displayName,
                    role = userInfo.role
                )
                _existAccountState.value = true
                delayAndRegisterDeviceToken()
            }
        }
    }

    /**
     * Cập nhật token cho ApiClient
     */
    private fun updateApiClientToken(token: String) {
        try {
            val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            sharedPref.edit().putString("firebase_id_token", token).apply()
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
            delay(1000)
            registerDeviceTokenForUser()
        }
    }

    private fun registerDeviceTokenForUser() {
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                val deviceModel = android.os.Build.MODEL
                val osVersion = android.os.Build.VERSION.RELEASE

                val result = notificationRepository.registerDeviceToken(
                    token = fcmToken,
                    platform = "android",
                    model = deviceModel,
                    osVersion = osVersion
                )

                when (result) {
                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Success -> {
                        Log.d("LoginViewModel", "✅ Đã đăng ký device token")
                    }
                    is com.example.foodapp.data.remote.client.response.notification.ApiResult.Failure -> {
                        Log.e("LoginViewModel", "❌ Lỗi đăng ký device token", result.exception)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "❌ Lỗi khi lấy FCM token", e)
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

// ============= SEALED CLASSES =============
