package com.example.foodapp.data.api


import com.example.foodapp.data.model.shared.otp.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OtpApiService {
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<ApiResponse<SendOtpResponse>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<VerifyOtpResponse>>

    @POST("auth/forgot-password")
    suspend fun sendOtpResetPassword(@Body request: SendOtpResetPasswordRequest): Response<ApiResponse<SendOtpResetPasswordResponse>>
}