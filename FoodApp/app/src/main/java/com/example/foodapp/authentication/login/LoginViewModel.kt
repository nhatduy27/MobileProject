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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ============= STATE CLASSES =============

sealed class LogInState {
    object Idle : LogInState()
    object Loading : LogInState()
    data class Success(
        val userId: String,
        val user: UserDetail? = null,
        val isNewUser: Boolean = false
    ) : LogInState()
    data class Error(val message: String, val code: String? = null) : LogInState()
}

sealed class GoogleLogInState {
    object Idle : GoogleLogInState()
    object Loading : GoogleLogInState()
    data class Success(
        val userId: String,
        val email: String? = null,
        val displayName: String? = null,
        val isNewUser: Boolean = false
    ) : GoogleLogInState()
    data class Error(val message: String, val code: String? = null) : GoogleLogInState()
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

// ============= VIEWMODEL =============

class LoginViewModel(
    private val repository: UserFirebaseRepository,
    private val authRepository: AuthRepository,
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

    private val _googleIdToken = MutableStateFlow<String?>(null)
    val googleIdToken: StateFlow<String?> = _googleIdToken.asStateFlow()

    // ============= GOOGLE SIGN-IN =============

    /**
     * Khởi tạo GoogleSignInClient
     */
    fun initializeGoogleSignIn(client: GoogleSignInClient) {
        this.googleSignInClient = client
    }

    /**
     * Get Google Sign-In Intent để start activity
     */
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
            Log.e("LoginViewModel", "Google Sign-In init error", e)
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
                Log.e("LoginViewModel", "Google Sign-In error", e)
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

        // Lưu thông tin tạm thời từ Google
        val tempUserInfo = GoogleUserDetail(
            id = account.id ?: "",
            email = account.email ?: "",
            displayName = account.displayName,
            photoUrl = account.photoUrl?.toString(),
            emailVerified = true,
            provider = "google"
        )

        saveTempGoogleInfo(tempUserInfo)

        // Gửi token lên server
        signInWithGoogleToken(idToken)
    }

    private suspend fun signInWithGoogleToken(idToken: String) {
        try {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is ApiResult.Success -> {
                    handleGoogleApiResponse(result.data)
                }
                is ApiResult.Failure -> {
                    val errorMsg = parseGoogleErrorMessage(result.exception)
                    _googleLogInState.value = GoogleLogInState.Error(
                        errorMsg.first,
                        errorMsg.second
                    )
                    Log.e("LoginViewModel", "Google API error", result.exception)
                }
            }
        } catch (e: Exception) {
            _googleLogInState.value = GoogleLogInState.Error(
                "Lỗi kết nối: ${e.message ?: "Vui lòng thử lại sau"}",
                "NETWORK_ERROR"
            )
        }
    }

    /**
     * Xử lý response từ API Google Sign-In
     */
    private fun handleGoogleApiResponse(apiResponse: ApiResponse) {
        if (!apiResponse.success) {
            _googleLogInState.value = GoogleLogInState.Error(
                apiResponse.message ?: "Đăng nhập Google thất bại",
                "API_ERROR"
            )
            return
        }

        // Trích xuất thông tin user từ response
        val (googleResponse, googleUser) = extractGoogleAuthData(apiResponse)

        when {
            googleUser == null -> {
                _googleLogInState.value = GoogleLogInState.Error(
                    "Không có thông tin người dùng",
                    "USER_DATA_NULL"
                )
            }
            !googleUser.isValid -> {
                _googleLogInState.value = GoogleLogInState.Error(
                    googleResponse?.message ?: "Đăng nhập Google thất bại",
                    "INVALID_USER"
                )
            }
            else -> {
                val isNewUser = googleResponse?.isNewUser ?: false
                handleSuccessfulGoogleSignIn(googleUser, isNewUser)
            }
        }
    }

    /**
     * Trích xuất dữ liệu Google Auth từ response
     */
    private fun extractGoogleAuthData(apiResponse: ApiResponse): Pair<GoogleAuthResponse?, GoogleUserDetail?> {
        return try {
            when (val data = apiResponse.data) {
                is Map<*, *> -> {
                    val userMap = data["user"] as? Map<*, *>
                    val isNewUser = data["isNewUser"] as? Boolean ?: false
                    val message = data["message"] as? String
                    
                    val googleUser = userMap?.let { uMap ->
                        GoogleUserDetail(
                            id = uMap["id"] as? String ?: "",
                            email = uMap["email"] as? String ?: "",
                            displayName = uMap["displayName"] as? String,
                            photoUrl = uMap["photoUrl"] as? String,
                            role = uMap["role"] as? String ?: "CUSTOMER",
                            status = uMap["status"] as? String ?: "ACTIVE",
                            emailVerified = uMap["emailVerified"] as? Boolean ?: false
                        )
                    }
                    
                    val googleResponse = GoogleAuthResponse(
                        success = true,
                        user = googleUser,
                        isNewUser = isNewUser,
                        message = message
                    )
                    
                    Pair(googleResponse, googleUser)
                }
                else -> {
                    Log.e("LoginViewModel", "Unexpected Google data type: ${data?.javaClass?.name}")
                    Pair(null, null)
                }
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error extracting Google auth data", e)
            Pair(null, null)
        }
    }

    /**
     * Xử lý khi Google Sign-In thành công
     */
    private fun handleSuccessfulGoogleSignIn(
        googleUser: GoogleUserDetail,
        isNewUser: Boolean
    ) {
        // SỬA: Dùng AuthManager để lưu user info
        authManager.saveUserInfo(
            userId = googleUser.id,
            email = googleUser.email,
            name = googleUser.displayName ?: "",
            role = googleUser.role ?: "CUSTOMER",
            status = googleUser.status ?: "ACTIVE"
        )

        // Lưu thêm thông tin Google-specific
        saveGoogleSpecificInfo(googleUser, isNewUser)

        // Cập nhật state
        _googleLogInState.value = GoogleLogInState.Success(
            userId = googleUser.id,
            email = googleUser.email,
            displayName = googleUser.displayName,
            isNewUser = isNewUser
        )

        _existAccountState.value = !isNewUser
    }

    /**
     * Lưu thông tin Google-specific
     */
    private fun saveGoogleSpecificInfo(user: GoogleUserDetail, isNewUser: Boolean) {
        try {
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            with(editor) {
                putString("user_photo_url", user.photoUrl ?: "")
                putBoolean("user_email_verified", user.emailVerified)
                putString("auth_provider", "google")
                putBoolean("is_new_user", isNewUser)
                apply()
            }

        } catch (e: Exception) {
            Log.e("LoginViewModel", "Lỗi khi lưu thông tin Google", e)
        }
    }

    /**
     * Lưu thông tin tạm thời từ Google trước khi xác thực server
     */
    private fun saveTempGoogleInfo(user: GoogleUserDetail) {
        try {
            val sharedPref = context.getSharedPreferences("temp_google_info", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            with(editor) {
                putString("temp_email", user.email)
                putString("temp_name", user.displayName ?: "")
                putString("temp_photo", user.photoUrl ?: "")
                apply()
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Lỗi lưu temp Google info", e)
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
            clearGoogleUserInfo()
            _googleLogInState.value = GoogleLogInState.Idle
            _googleIdToken.value = null
            Log.d("LoginViewModel", "Đã đăng xuất Google")
        }
    }

    /**
     * Xoá thông tin Google user
     */
    private fun clearGoogleUserInfo() {
        try {
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            with(editor) {
                remove("auth_provider")
                remove("google_id_token")
                putBoolean("is_logged_in", false)
                apply()
            }

            // Xóa temp info
            context.getSharedPreferences("temp_google_info", Context.MODE_PRIVATE)
                .edit().clear().apply()

        } catch (e: Exception) {
            Log.e("LoginViewModel", "Lỗi khi xoá thông tin Google user", e)
        }
    }

    /**
     * Kiểm tra xem có đang đăng nhập bằng Google không
     */
    fun isGoogleSignedIn(): Boolean {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("auth_provider", "") == "google" &&
                sharedPref.getBoolean("is_logged_in", false)
    }

    /**
     * Lấy thông tin user hiện tại (nếu đã đăng nhập)
     */
    fun getCurrentUser(): GoogleUserDetail? {
        return try {
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

            val id = sharedPref.getString("user_id", null) ?: return null
            val email = sharedPref.getString("user_email", null) ?: return null
            val provider = sharedPref.getString("auth_provider", null)

            if (provider != "google") return null

            GoogleUserDetail(
                id = id,
                email = email,
                displayName = sharedPref.getString("user_name", null),
                photoUrl = sharedPref.getString("user_photo_url", null),
                role = sharedPref.getString("user_role", "CUSTOMER") ?: "CUSTOMER",
                status = sharedPref.getString("user_status", "ACTIVE") ?: "ACTIVE",
                emailVerified = sharedPref.getBoolean("user_email_verified", false),
                provider = provider
            )
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Lỗi khi lấy thông tin user", e)
            null
        }
    }

    // -------------------ĐĂNG NHẬP BẰNG EMAIL/PASSWORD -----------------------
    fun login(email: String, password: String) {
        when (val validation = validateInput(email, password)) {
            is ValidationResult.Error -> {       //Nếu có lỗi
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
                when (val result = authRepository.login(email, password)) { //Gọi API
                    is ApiResult.Success -> {
                        handleApiResponse(result.data)  //Xử lí dữ liệu trả về
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

    private fun handleApiResponse(apiResponse: ApiResponse) {
        if (!apiResponse.success) {
            _logInState.value = LogInState.Error(
                apiResponse.message ?: "Đăng nhập thất bại",
                "API_RESPONSE_ERROR"
            )
            return
        }

        val loginResponse = extractLoginResponse(apiResponse)
        when {
            loginResponse == null -> {
                _logInState.value = LogInState.Error("Dữ liệu không hợp lệ", "INVALID_DATA")
            }
            !loginResponse.isValid -> {
                _logInState.value = LogInState.Error(
                    loginResponse.message ?: "Đăng nhập thất bại",
                    "INVALID_LOGIN"
                )
            }
            loginResponse.user?.isBanned == true -> {
                _logInState.value = LogInState.Error("Tài khoản đã bị khóa", "ACCOUNT_BANNED")
            }
            loginResponse.user?.isActive == false -> {
                _logInState.value = LogInState.Error("Tài khoản chưa được kích hoạt", "ACCOUNT_INACTIVE")
            }
            else -> {
                handleSuccessfulLogin(loginResponse)
            }
        }
    }

    //Xử lí dữ liệu từ backend thành object bên frontend
    // Backend returns: {"success": true, "data": {"user": {...}, "customToken": "...", "message": "..."}}
    private fun extractLoginResponse(apiResponse: ApiResponse): LoginResponse? {
        return try {
            when (val data = apiResponse.data) {
                is Map<*, *> -> {
                    // Parse user from Map
                    val userMap = data["user"] as? Map<*, *>
                    val customToken = data["customToken"] as? String
                    val message = data["message"] as? String
                    
                    val userDetail = userMap?.let { uMap ->
                        UserDetail(
                            id = uMap["id"] as? String ?: "",
                            email = uMap["email"] as? String ?: "",
                            displayName = uMap["displayName"] as? String,
                            role = uMap["role"] as? String ?: "",
                            status = uMap["status"] as? String ?: ""
                        )
                    }
                    
                    LoginResponse(
                        success = true,
                        user = userDetail,
                        customToken = customToken,
                        message = message
                    )
                }
                is LoginResponse -> data
                else -> {
                    Log.e("LoginViewModel", "Unexpected data type: ${data?.javaClass?.name}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error extracting login response", e)
            null
        }
    }

    private fun handleSuccessfulLogin(loginResponse: LoginResponse) {
        val userDetail = loginResponse.user
        val customToken = loginResponse.customToken

        if (userDetail == null || customToken == null) {
            _logInState.value = LogInState.Error("Thiếu thông tin đăng nhập", "MISSING_DATA")
            return
        }

        // SỬA: Dùng AuthManager để lưu user info
        authManager.saveUserInfo(
            userId = userDetail.id,
            email = userDetail.email,
            name = userDetail.displayName ?: "",
            role = userDetail.role,
            status = userDetail.status
        )

        //Lấy Firebase ID Token và lưu
        authManager.signInWithCustomToken(customToken) { isSuccessful, idToken, error ->
            if (isSuccessful) {
                // SỬA: Dùng AuthManager để lưu token
                if (!idToken.isNullOrEmpty()) {
                    authManager.saveFirebaseToken(idToken)
                    authManager.debugTokenInfo() // Debug
                    Log.d("LoginViewModel", "✅ Đã lưu Firebase ID Token")
                }

                _logInState.value = LogInState.Success(userDetail.id, userDetail)
                _existAccountState.value = true
                Log.d("LoginViewModel", "✅ Đăng nhập thành công")
            } else {
                _logInState.value = LogInState.Success(userDetail.id, userDetail)
                _existAccountState.value = true
                Log.w("LoginViewModel", "⚠ Không thể sign in Firebase: $error")
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

    // XÓA hàm saveAuthToken (đã chuyển sang AuthManager)
    // private fun saveAuthToken(token: String) { ... }

    // XÓA hàm saveUserInfoLocally (đã chuyển sang AuthManager)
    // private fun saveUserInfoLocally(userDetail: UserDetail) { ... }

    fun getUserRole(userId: String, onComplete: (String?) -> Unit) {
        repository.getUserRole(userId, onComplete)
    }

    fun resetStates() {
        _logInState.value = LogInState.Idle
        _googleLogInState.value = GoogleLogInState.Idle
        _existAccountState.value = null
        _googleIdToken.value = null
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
                        UserFirebaseRepository(context),
                        AuthRepository(),
                        context
                    ) as T
                }
            }
        }
    }
}