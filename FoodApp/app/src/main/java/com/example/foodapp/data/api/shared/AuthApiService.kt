package com.example.foodapp.data.api.shared

import com.example.foodapp.data.model.shared.auth.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse>

    @POST("auth/google")
    suspend fun googleLogin(@Body request : GoogleAuthRequest) : Response<ApiResponse>


    @POST("auth/logout")
    @Headers("Content-Type: application/json")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Body request: LogoutRequest
    ): Response<ApiResponse>


    @PUT("auth/change-password")
    @Headers("Content-Type: application/json")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>
}
