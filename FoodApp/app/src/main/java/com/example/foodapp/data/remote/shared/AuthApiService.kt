package com.example.foodapp.data.remote.shared

import com.example.foodapp.data.model.shared.auth.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

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


data class GoogleAuthRequest(
    @SerializedName("idToken")
    val idToken: String,

    @SerializedName("role")
    val role: String? = null
)

data class GoogleUserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("photoUrl")
    val photoUrl: String?,

    @SerializedName("role")
    val role: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("emailVerified")
    val emailVerified: Boolean
)

// ⭐ THÊM DATA WRAPPER
data class GoogleAuthData(
    @SerializedName("user")
    val user: GoogleUserDto,

    @SerializedName("isNewUser")
    val isNewUser: Boolean
)

// ⭐ UPDATE RESPONSE STRUCTURE
data class GoogleAuthResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: GoogleAuthData,  // ⬅️ Thay đổi từ user thành data

    @SerializedName("timestamp")
    val timestamp: String? = null
)
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


    @POST("auth/google")
    @Headers("Content-Type: application/json")
    suspend fun googleLoginRaw(
        @Body request: GoogleAuthRequest
    ): Response<GoogleAuthResponse>


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

    @PUT("auth/set-role")
    suspend fun setRole(@Body request: SetRoleRequest): Response<SetRoleResponse>
}