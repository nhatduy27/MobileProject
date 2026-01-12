// File: data/model/shared/auth/models.kt

package com.example.foodapp.data.model.shared.auth

import com.google.gson.annotations.SerializedName

// Sealed class cho kết quả từ repository
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

// ============== API RESPONSE WRAPPER ==============

/**
 * Wrapper response chính
 * Format: {
 *   "success": boolean,
 *   "data": InnerResponse,
 *   "message": string,
 *   "timestamp": string
 * }
 */
data class ApiResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: InnerResponse? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

// ============= INNER RESPONSE (WRAPPER TRONG DATA) =============

/**
 * Inner response wrapper
 * Format: {
 *   "success": boolean,
 *   "data": RegisterData,
 *   "message": string
 * }
 */
data class InnerResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("data")
    val data: RegisterData? = null,  // ← RegisterData thực sự

    @SerializedName("message")
    val message: String? = null
)

// ============= REGISTER SPECIFIC MODELS =============

// DTO cho yêu cầu đăng ký
data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phone")
    val phone: String? = null
)

// Dữ liệu user trong phản hồi đăng ký
data class RegisterData @JvmOverloads constructor(
    @SerializedName("user")
    val user: UserInfo? = null,

    @SerializedName("customToken")
    val customToken: String? = null
) {
    // Helper để lấy user info dễ dàng
    fun isValid(): Boolean = user?.isValid == true
}

// Thông tin user
data class UserInfo @JvmOverloads constructor(
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

// ============= RESET PASSWORD MODELS =============

data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("newPassword")
    val newPassword: String
)

data class ResetPasswordResponse @JvmOverloads constructor(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String = ""
)

// ============= LOGIN SPECIFIC MODELS =============

// DTO cho yêu cầu đăng nhập
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

// Dữ liệu từ phản hồi đăng nhập
data class LoginResponse  @JvmOverloads constructor(

    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("user")
    val user: UserDetail? = null,

    @SerializedName("customToken")
    val customToken: String? = null,

    @SerializedName("message")
    val message: String? = null
) {
    val isValid: Boolean get() = user?.isValid == true && !customToken.isNullOrBlank()
}

// Chi tiết user từ đăng nhập (đầy đủ thông tin)
data class UserDetail @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("photoUrl")
    val photoUrl: String? = null,

    @SerializedName("role")
    val role: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("emailVerified")
    val emailVerified: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: String? = null
) {
    val isValid: Boolean get() = id.isNotBlank() && email.isNotBlank()
    val isActive: Boolean get() = status.equals("ACTIVE", ignoreCase = true)
    val isBanned: Boolean get() = status.equals("BANNED", ignoreCase = true)


    fun getDisplayNameOrEmail(): String = displayName ?: email.split("@").first()
}


data class GoogleAuthRequest(
    @SerializedName("idToken")
    val idToken: String,

    @SerializedName("role")
    val role: String? = null
)

// Dữ liệu từ phản hồi đăng nhập Google
data class GoogleAuthResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("user")
    val user: GoogleUserDetail? = null,

    @SerializedName("isNewUser")
    val isNewUser: Boolean = false,

    @SerializedName("message")
    val message: String? = null
)

// Chi tiết user từ Google Sign-In
data class GoogleUserDetail @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("photoUrl")
    val photoUrl: String? = null,

    @SerializedName("role")
    val role: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("emailVerified")
    val emailVerified: Boolean = false,

    @SerializedName("provider")
    val provider: String = "google"
){
    // THÊM DÒNG NÀY
    val isValid: Boolean get() = id.isNotBlank() && email.isNotBlank()
}


