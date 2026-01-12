package com.example.foodapp.data.api.shared

import com.example.foodapp.data.model.shared.auth.ApiResponse
import com.example.foodapp.data.model.shared.auth.GoogleAuthRequest
import com.example.foodapp.data.model.shared.auth.LoginRequest
import com.example.foodapp.data.model.shared.auth.RegisterRequest
import com.example.foodapp.data.model.shared.auth.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse>

    @POST("auth/google")
    suspend fun googleLogin(@Body request : GoogleAuthRequest) : Response<ApiResponse>
}