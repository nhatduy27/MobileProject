package com.example.foodapp.data.model.OTP

import com.google.gson.annotations.SerializedName

// Đổi tên thành ApiResult để tránh conflict với kotlin.Result
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

// Request models
data class SendOtpRequest(
    @SerializedName("email")
    val email: String
)

data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("otp")
    val otp: String
)

// Response models
data class SendOtpResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("expiresAt")
    val expiresAt: String
)

data class VerifyOtpResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("verified")
    val verified: Boolean
)

data class VerificationStatusResponse(
    @SerializedName("email")
    val email: String,

    @SerializedName("isVerified")
    val isVerified: Boolean,

    @SerializedName("verifiedAt")
    val verifiedAt: String? = null
)