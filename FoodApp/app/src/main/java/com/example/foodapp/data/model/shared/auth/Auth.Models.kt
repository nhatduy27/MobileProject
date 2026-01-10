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
    val data: InnerResponse? = null,  // ← InnerResponse, không phải RegisterData

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