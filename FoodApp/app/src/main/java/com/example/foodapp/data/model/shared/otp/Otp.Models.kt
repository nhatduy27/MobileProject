package com.example.foodapp.data.model.shared.otp

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

// ============== SIMPLE RESPONSE MODELS ==============

/**
 * Dùng cho các API chỉ trả về success + message trong data
 * VD: Verify OTP, Reset Password
 */
data class SimpleMessageData(
    @SerializedName("message")
    val message: String = ""
)

/**
 * Dùng cho các API chỉ trả về success + message
 * VD: Send OTP
 */
data class SimpleResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null
)

// ============== ENUMS ==============

// Enum cho OTP Type
enum class OTPType {
    @SerializedName("EMAIL_VERIFICATION")
    EMAIL_VERIFICATION,

    @SerializedName("PASSWORD_RESET")
    PASSWORD_RESET
}

// ============== REQUEST MODELS ==============

data class SendOtpRequest(
    @SerializedName("email")
    val email: String
)

data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("type")
    val otpType: OTPType
)

// ============== TYPE ALIASES FOR CLARITY ==============

// Giúp code dễ đọc hơn
typealias SendOtpResponse = ApiResponse<SimpleMessageData>
typealias VerifyOtpResponse = ApiResponse<SimpleMessageData>
typealias SendOtpResetPasswordResponse = ApiResponse<SimpleMessageData>