package com.example.foodapp.data.remote.shared

import com.example.foodapp.data.model.shared.auth.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Request để set role
 */
data class SetRoleRequest(
    @SerializedName("role")
    val role: String
)

/**
 * Response từ set role API
 */
data class SetRoleResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("role")
    val role: String
)

/**
 * API Service cho Auth endpoints
 */
interface AuthApiService {
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<ApiResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Body request: LogoutRequest
    ): Response<ApiResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    /**
     * PUT /auth/set-role
     * Set user role và update Firebase Custom Claims
     */
    @PUT("auth/set-role")
    suspend fun setRole(@Body request: SetRoleRequest): Response<SetRoleResponse>
}
