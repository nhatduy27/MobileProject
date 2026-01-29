// File: data/repository/shared/AuthRepository.kt

package com.example.foodapp.data.repository.shared

import android.util.Log
import com.example.foodapp.data.model.client.Client
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.model.shared.auth.*
import com.example.foodapp.data.remote.shared.GoogleAuthRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository {

    private val apiService = ApiClient.authApiService

    // ============== REGISTER ==============
    suspend fun register(
        email: String,
        displayName: String,
        password: String
    ): ApiResult<AuthData> {
        return try {
            withContext(Dispatchers.IO) {
                val request = RegisterRequest(email, displayName, password)
                val response = apiService.register(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        val authData = apiResponse.data
                        if (authData != null && authData.isValid) {
                            ApiResult.Success(authData)
                        } else {
                            ApiResult.Failure(Exception("Không nhận được thông tin người dùng từ server"))
                        }
                    } else {
                        val errorMessage = apiResponse?.message ?: "Đăng ký thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    handleHttpError(response)
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }


    // ============== LOGIN ==============
    suspend fun login(email: String, password: String): ApiResult<AuthData> {
        return try {
            withContext(Dispatchers.IO) {
                val request = LoginRequest(email, password)
                val response = apiService.login(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        val authData = apiResponse.data
                        if (authData != null && authData.isValid) {
                            ApiResult.Success(authData)
                        } else {
                            ApiResult.Failure(Exception("Không nhận được thông tin đăng nhập từ server"))
                        }
                    } else {
                        val errorMessage = apiResponse?.message ?: "Đăng nhập thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    handleHttpError(response, authContext = true)
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }

    // ============== GOOGLE SIGN-IN ==============
    // ============== GOOGLE SIGN-IN (FIXED) ==============
    // ============== GOOGLE SIGN-IN (FIXED & CLEAN) ==============
    suspend fun signInWithGoogle(
        idToken: String,
        role: String? = null
    ): ApiResult<Client> {
        return try {
            withContext(Dispatchers.IO) {
                val request = GoogleAuthRequest(idToken, role)


                val response = apiService.googleLoginRaw(request)


                if (response.isSuccessful) {
                    val body = response.body()


                    if (body == null) {
                        return@withContext ApiResult.Failure(
                            Exception("Không nhận được dữ liệu từ server")
                        )
                    }

                    // ⭐ LẤY USER TỪ body.data.user (CẤU TRÚC MỚI)
                    val user = body.data?.user

                    Log.d("AuthRepository", "🔍 User object: $user")

                    if (user == null) {
                        Log.e("AuthRepository", "❌ User object là null")
                        return@withContext ApiResult.Failure(
                            Exception("Thông tin người dùng không tồn tại")
                        )
                    }

                    Log.d("AuthRepository", "✅ User ID: ${user.id}, Email: ${user.email}")

                    // Tạo Client object
                    val client = Client.fromGoogleAuth(
                        userId = user.id,
                        email = user.email,
                        displayName = user.displayName,
                        photoUrl = user.photoUrl,
                        role = user.role,
                        emailVerified = user.emailVerified
                    )

                    Log.d("AuthRepository", "✅ Client created: ${client.id}")
                    ApiResult.Success(client)
                } else {
                    // Xử lý lỗi HTTP
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("AuthRepository", "❌ HTTP Error ${response.code()}: $errorBody")

                    ApiResult.Failure(
                        Exception("Đăng nhập thất bại: HTTP ${response.code()}")
                    )
                }
            }
        } catch (e: HttpException) {
            Log.e("AuthRepository", "❌ HttpException", e)
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
        } catch (e: IOException) {
            Log.e("AuthRepository", "❌ IOException", e)
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception", e)
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }



    // ============== RESET PASSWORD ==============
    suspend fun resetPassword(
        email: String,
        newPassword: String
    ): ApiResult<SimpleResponse> {
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
                    handleHttpError(response)
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    }

    // ============== LOGOUT ==============
    suspend fun logout(accessToken: String, fcmToken: String? = null): ApiResult<SimpleResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val request = LogoutRequest(fcmToken)
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
                    handleHttpError(response)
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }

    // ============== CHANGE PASSWORD ==============
    suspend fun changePassword(
        accessToken: String,
        oldPassword: String,
        newPassword: String
    ): ApiResult<SimpleResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val request = ChangePasswordRequest(oldPassword, newPassword)
                val response = apiService.changePassword("Bearer $accessToken", request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)
                    } else {
                        val errorMessage = body?.message ?: "Thay đổi mật khẩu thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    handleHttpError(response)
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }

    // ============== DELETE ACCOUNT ==============
    suspend fun deleteAccount(
        accessToken: String
    ): ApiResult<SimpleResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.deleteAccount("Bearer $accessToken")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)
                    } else {
                        val errorMessage = body?.message ?: "Xóa tài khoản thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    handleHttpError(response)
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }

    // ============== HELPER FUNCTIONS ==============

    private fun handleHttpError(
        response: retrofit2.Response<*>,
        authContext: Boolean = false
    ): ApiResult<Nothing> {
        val errorCode = response.code()
        val errorBody = response.errorBody()?.string()

        val errorMessage = when (errorCode) {
            400 -> if (authContext) "Email hoặc mật khẩu không đúng" else "Yêu cầu không hợp lệ"
            401 -> if (authContext) "Tài khoản hoặc mật khẩu không đúng" else "Không có quyền truy cập"
            403 -> "Truy cập bị từ chối"
            404 -> "Tài nguyên không tồn tại"
            409 -> "Xung đột dữ liệu"
            422 -> "Dữ liệu không hợp lệ"
            500 -> "Lỗi máy chủ, vui lòng thử lại sau"
            else -> errorBody ?: "Lỗi HTTP: $errorCode"
        }

        return ApiResult.Failure(Exception(errorMessage))
    }
}