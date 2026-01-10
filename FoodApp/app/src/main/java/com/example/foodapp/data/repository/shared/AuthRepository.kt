package com.example.foodapp.data.repository.shared

import com.example.foodapp.data.api.ApiClient
import com.example.foodapp.data.model.shared.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository {

    private val apiService = ApiClient.authApiService

    suspend fun register(
        email: String,
        displayName: String,
        password: String
    ): ApiResult<ApiResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val request = RegisterRequest(email, displayName, password)
                val response = apiService.register(request)

                // Kiểm tra HTTP status code
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)  // body là ApiResponse
                    } else {
                        val errorMessage = body?.message ?: "Đăng ký thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = errorBody ?: "HTTP error: ${response.code()}"
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    suspend fun resetPassword(
        email: String,
        newPassword: String
    ): ApiResult<ApiResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val request = ResetPasswordRequest(email, newPassword)
                val response = apiService.resetPassword(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)
                    } else {
                        val errorMessage = body?.message ?: "Đặt lại mật khẩu thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = errorBody ?: "HTTP error: ${response.code()}"
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }
}