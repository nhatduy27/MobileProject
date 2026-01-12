package com.example.foodapp.data.repository.client.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.example.foodapp.data.api.ApiClient
import com.example.foodapp.data.model.client.profile.*

class ProfileRepository {

    private val profileService = ApiClient.profileApiService

    suspend fun getUserProfile(): ApiResult<GetProfileResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = profileService.getMe()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)
                    } else {
                        ApiResult.Failure(
                            Exception("Không thể lấy thông tin người dùng")
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
            } catch (e: HttpException) {
                ApiResult.Failure(Exception("Lỗi server: ${e.message}"))
            } catch (e: Exception) {
                ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
            }
        }
    }

    /*

    suspend fun updateProfile(
        displayName: String?,
        phone: String?
    ): ApiResult<GetProfileResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateProfileRequest(
                    displayName = displayName,
                    phone = phone
                )

                val response = profileService.updateProfile(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)
                    } else {
                        ApiResult.Failure(
                            Exception(body?.message ?: "Cập nhật thất bại")
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
            } catch (e: HttpException) {
                ApiResult.Failure(Exception("Lỗi server: ${e.message}"))
            } catch (e: Exception) {
                ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
            }
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): ApiResult<GetProfileResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )

                val response = profileService.changePassword(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        ApiResult.Success(body)
                    } else {
                        ApiResult.Failure(
                            Exception(body?.message ?: "Đổi mật khẩu thất bại")
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
            } catch (e: HttpException) {
                ApiResult.Failure(Exception("Lỗi server: ${e.message}"))
            } catch (e: Exception) {
                ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
            }
        }
    }

     */
}