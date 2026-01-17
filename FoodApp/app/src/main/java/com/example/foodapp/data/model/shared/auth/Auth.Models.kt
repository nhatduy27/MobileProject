// File: data/model/shared/auth/models.kt

package com.example.foodapp.data.model.shared.auth

import com.google.gson.annotations.SerializedName

// ============== API RESULT ==============

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

// ============== UNIVERSAL API RESPONSE ==============

/**
 * Universal response cho TẤT CẢ các API endpoints
 * Format: {
 *   "success": boolean,
 *   "data": T?,
 *   "message": string?,
 *   "timestamp": string
 * }
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String = ""
)

// ============== COMMON DATA MODELS ==============

/**
 * Dữ liệu trả về cho Auth APIs (Register, Login, Google Auth)
 * Theo JSON mẫu: { "user": {...}, "customToken": "..." }
 */
data class AuthData(
    @SerializedName("user")
    val user: UserInfo,  // Dùng UserInfo vì JSON mẫu chỉ có các field cơ bản

    @SerializedName("customToken")
    val customToken: String
) {
    val isValid: Boolean get() = user.isValid && customToken.isNotBlank()
}

/**
 * Thông tin user cơ bản - KHỚP với JSON mẫu bạn cung cấp
 */
data class UserInfo(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("displayName")
    val displayName: String = "",

    @SerializedName("role")
    val role: String = "",

    @SerializedName("status")
    val status: String = ""
) {
    val isValid: Boolean get() = id.isNotBlank() && email.isNotBlank()
    fun isActive(): Boolean = status.equals("ACTIVE", ignoreCase = true)
}

// ============== REQUEST MODELS ==============

data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("password")
    val password: String
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class GoogleAuthRequest(
    @SerializedName("idToken")
    val idToken: String,

    @SerializedName("role")
    val role: String? = null
)

data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("newPassword")
    val newPassword: String
)

data class ChangePasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String,

    @SerializedName("newPassword")
    val newPassword: String
)

data class LogoutRequest(
    @SerializedName("fcmToken")
    val fcmToken: String? = null
)

// ============== SIMPLE RESPONSE MODELS ==============

/**
 * Dùng cho các API chỉ trả về success + message
 * VD: Reset password, Change password, Delete account
 */
data class SimpleResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null
)

// ============== TYPE ALIASES FOR CLARITY ==============

// Giúp code dễ đọc hơn
typealias RegisterResponse = ApiResponse<AuthData>
typealias LoginResponse = ApiResponse<AuthData>
typealias GoogleAuthResponse = ApiResponse<AuthData>
typealias ResetPasswordResponse = SimpleResponse
typealias ChangePasswordResponse = SimpleResponse
typealias DeleteAccountResponse = SimpleResponse