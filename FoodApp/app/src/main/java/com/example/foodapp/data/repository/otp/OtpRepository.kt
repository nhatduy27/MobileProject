package com.example.foodapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.model.shared.otp.*

class OtpRepository {
    private val apiService = ApiClient.otpApiService

    suspend fun sendOtp(email: String): ApiResult<SimpleMessageData> {
        return try {
            withContext(Dispatchers.IO) {
                val request = SendOtpRequest(email)
                val response = apiService.sendOtp(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        val data = apiResponse.data
                        if (data != null) {
                            ApiResult.Success(data) // SimpleMessageData
                        } else {
                            ApiResult.Failure(Exception("Không nhận được dữ liệu từ server"))
                        }
                    } else {
                        val errorMessage = apiResponse?.message ?: "Gửi OTP thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    handleHttpError(response)
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }

    suspend fun verifyOtp(email: String, otp: String, type: OTPType): ApiResult<SimpleMessageData> {
        return try {
            withContext(Dispatchers.IO) {
                val request = VerifyOtpRequest(email, otp, type)
                val response = apiService.verifyOtp(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        val data = apiResponse.data
                        if (data != null) {
                            ApiResult.Success(data) // SimpleMessageData
                        } else {
                            ApiResult.Failure(Exception("Không nhận được dữ liệu từ server"))
                        }
                    } else {
                        val errorMessage = apiResponse?.message ?: "Xác thực OTP thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    handleHttpError(response)
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }

    suspend fun sendOtpResetPassword(email: String): ApiResult<SimpleMessageData> {
        return try {
            withContext(Dispatchers.IO) {
                val request = SendOtpRequest(email)
                val response = apiService.sendOtp(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        val data = apiResponse.data
                        if (data != null) {
                            ApiResult.Success(data) // SimpleMessageData
                        } else {
                            ApiResult.Failure(Exception("Không nhận được dữ liệu từ server"))
                        }
                    } else {
                        val errorMessage = apiResponse?.message ?: "Gửi OTP đặt lại mật khẩu thất bại"
                        ApiResult.Failure(Exception(errorMessage))
                    }
                } else {
                    handleHttpError(response)
                }
            }
        } catch (e: HttpException) {
            ApiResult.Failure(Exception("Lỗi kết nối: ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            ApiResult.Failure(Exception("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
        }
    }

    // Helper function để xử lý HTTP errors
    private fun handleHttpError(response: retrofit2.Response<*>): ApiResult<Nothing> {
        val errorCode = response.code()
        val errorBody = response.errorBody()?.string()

        val errorMessage = when (errorCode) {
            400 -> "Yêu cầu không hợp lệ"
            401 -> "Không có quyền truy cập"
            404 -> "Tài nguyên không tồn tại"
            429 -> "Quá nhiều yêu cầu. Vui lòng thử lại sau"
            500 -> "Lỗi máy chủ, vui lòng thử lại sau"
            else -> errorBody ?: "Lỗi HTTP: $errorCode"
        }

        return ApiResult.Failure(Exception(errorMessage))
    }
}