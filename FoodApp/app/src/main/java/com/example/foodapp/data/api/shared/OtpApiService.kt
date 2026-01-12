package com.example.foodapp.data.api.shared

import com.example.foodapp.data.model.shared.otp.ApiResponse
import com.example.foodapp.data.model.shared.otp.SendOtpRequest
import com.example.foodapp.data.model.shared.otp.SendOtpResetPasswordRequest
import com.example.foodapp.data.model.shared.otp.SendOtpResetPasswordResponse
import com.example.foodapp.data.model.shared.otp.SendOtpResponse
import com.example.foodapp.data.model.shared.otp.VerifyOtpRequest
import com.example.foodapp.data.model.shared.otp.VerifyOtpResponse
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