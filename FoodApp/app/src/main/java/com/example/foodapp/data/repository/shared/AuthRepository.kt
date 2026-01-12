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

    suspend fun login(email: String, password: String): ApiResult<ApiResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val request = LoginRequest(email, password)
                val response = apiService.login(request)

                // Kiểm tra HTTP status code
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)  // body là ApiResponse
                    } else {
                        // Parse error message từ response body nếu có
                        ApiResult.Failure(Exception(response.message()))
                    }
                } else {
                    // Xử lý HTTP error (401, 500, etc.)
                    ApiResult.Failure(Exception(response.message()))
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



    suspend fun signInWithGoogle(
        idToken: String,
        role: String? = null
    ): ApiResult<ApiResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val request = GoogleAuthRequest(idToken, role)
                val response = apiService.googleLogin(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)  // body là ApiResponse
                    } else {
                        val errorMessage = body?.message ?: "Đăng nhập Google thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string()

                    val errorMessage = when (errorCode) {
                        400 -> "Yêu cầu không hợp lệ. Token Google có thể bị lỗi"
                        401 -> "Token Google không hợp lệ hoặc đã hết hạn"
                        403 -> "Truy cập bị từ chối"
                        422 -> "Dữ liệu không hợp lệ"
                        else -> errorBody ?: "HTTP error: ${response.code()}"
                    }

                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }


}