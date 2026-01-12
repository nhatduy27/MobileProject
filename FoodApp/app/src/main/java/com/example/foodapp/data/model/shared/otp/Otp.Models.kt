package com.example.foodapp.data.model.shared.otp

import com.google.gson.annotations.SerializedName


sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

// Common response wrapper từ API
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("error")
    val error: String? = null
)

// Enum cho OTP Type
enum class OTPType {
    @SerializedName("EMAIL_VERIFICATION")
    EMAIL_VERIFICATION,

    @SerializedName("PASSWORD_RESET")
    PASSWORD_RESET
}

// Request models
data class SendOtpRequest(
    @SerializedName("email")
    val email: String
)

data class SendOtpResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("expiresAt")
    val expiresAt: String
)

data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("type")
    val otpType: OTPType
)

// Response models
data class VerifyOtpResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

)

data class SendOtpResetPasswordRequest(
    @SerializedName("email")
    val email: String
)

data class SendOtpResetPasswordResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("expiresAt")
    val expiresAt: String
)


