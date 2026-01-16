package com.example.foodapp.data.repository.shared

import com.example.foodapp.data.remote.api.ApiClient
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



    suspend fun logout(accessToken: String, fcmToken: String? = null): ApiResult<ApiResponse> {
        return try {
            withContext(Dispatchers.IO) {
                // Tạo request với FCM token
                val request = LogoutRequest(fcmToken)

                // Gọi API với Authorization header
                val response = apiService.logout("Bearer $accessToken", request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)
                    } else {
                        val errorMessage = body?.message ?: "Đăng xuất thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string()

                    val errorMessage = when (errorCode) {
                        401 -> "Token không hợp lệ hoặc đã hết hạn"
                        403 -> "Không có quyền truy cập"
                        else -> errorBody ?: "HTTP error: $errorCode"
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


    suspend fun changePassword(
        accessToken: String,
        oldPassword: String,
        newPassword: String
    ): ApiResult<ChangePasswordResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val request = ChangePasswordRequest(oldPassword, newPassword)
                val response = apiService.changePassword("Bearer $accessToken", request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success == true) {
                        ApiResult.Success(body)
                    } else {
                        val errorMessage = body?.message ?: "Thay đổi mật khẩu thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string()

                    val errorMessage = when (errorCode) {
                        400 -> "Mật khẩu cũ không đúng"
                        401 -> "Token không hợp lệ hoặc đã hết hạn"
                        422 -> "Mật khẩu mới không đáp ứng yêu cầu bảo mật"
                        else -> errorBody ?: "HTTP error: $errorCode"
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


    // AuthRepository.kt
    suspend fun deleteAccount(
        accessToken: String
    ): ApiResult<DeleteAccountResponse> {
        println("DEBUG [AuthRepository] deleteAccount called")
        println("DEBUG [AuthRepository] Access token: ${accessToken.take(20)}...")

        return try {
            withContext(Dispatchers.IO) {
                println("DEBUG [AuthRepository] Making API call to DELETE /api/me")

                val response = apiService.deleteAccount("Bearer $accessToken")

                println("DEBUG [AuthRepository] Response code: ${response.code()}")
                println("DEBUG [AuthRepository] Response message: ${response.message()}")
                println("DEBUG [AuthRepository] Is successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val body = response.body()
                    println("DEBUG [AuthRepository] Response body: $body")

                    if (body != null && body.success) {
                        ApiResult.Success(body)
                    } else {
                        val errorMessage = body?.message ?: "Xóa tài khoản thất bại"
                        println("DEBUG [AuthRepository] Error: $errorMessage")
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string()

                    println("DEBUG [AuthRepository] HTTP Error $errorCode: $errorBody")

                    val errorMessage = when (errorCode) {
                        401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại"
                        403 -> "Không có quyền xóa tài khoản"
                        404 -> "Tài khoản không tồn tại"
                        409 -> "Không thể xóa tài khoản vì có dữ liệu liên quan"
                        500 -> "Lỗi máy chủ. Vui lòng thử lại sau"
                        else -> errorBody ?: "Lỗi không xác định (Mã: $errorCode)"
                    }
                    ApiResult.Failure(Exception(errorMessage))
                }
            }
        } catch (e: HttpException) {
            println("DEBUG [AuthRepository] HttpException: ${e.message}")
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
        } catch (e: IOException) {
            println("DEBUG [AuthRepository] IOException: ${e.message}")
            ApiResult.Failure(Exception("Không có kết nối mạng. Vui lòng kiểm tra internet"))
        } catch (e: Exception) {
            println("DEBUG [AuthRepository] Exception: ${e.message}")
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }



}