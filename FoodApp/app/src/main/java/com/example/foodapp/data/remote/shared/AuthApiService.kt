package com.example.foodapp.data.remote.shared

import com.example.foodapp.data.model.shared.auth.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    // REGISTER - Trả về ApiResponse<AuthData>
    @POST("auth/register")
    @Headers("Content-Type: application/json")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthData>>

    // LOGIN - Trả về ApiResponse<AuthData>
    @POST("auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthData>>

    // GOOGLE SIGN-IN - Trả về ApiResponse<AuthData>
    @POST("auth/google")
    @Headers("Content-Type: application/json")
    suspend fun googleLogin(
        @Body request: GoogleAuthRequest
    ): Response<ApiResponse<AuthData>>

    // RESET PASSWORD - Trả về SimpleResponse
    @POST("auth/reset-password")
    @Headers("Content-Type: application/json")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<SimpleResponse>

    // LOGOUT - Trả về SimpleResponse
    @POST("auth/logout")
    @Headers("Content-Type: application/json")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Body request: LogoutRequest
    ): Response<SimpleResponse>

    // CHANGE PASSWORD - Trả về SimpleResponse
    @PUT("auth/change-password")
    @Headers("Content-Type: application/json")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<SimpleResponse>

    // DELETE ACCOUNT - Trả về SimpleResponse
    @DELETE("me")
    @Headers("Content-Type: application/json")
    suspend fun deleteAccount(
        @Header("Authorization") token: String
    ): Response<SimpleResponse>
}