package com.example.foodapp.data.repository.client.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.example.foodapp.data.api.ApiClient
import com.example.foodapp.data.model.client.profile.*
import com.google.gson.Gson

class ProfileRepository {

    private val profileService = ApiClient.profileApiService
    private val gson = Gson()

    suspend fun getUserProfile(): ApiResult<UserProfileData> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting getUserProfile...")

                val response = profileService.getMe()
                println("DEBUG: [Repository] API Response - isSuccessful: ${response.isSuccessful}")
                println("DEBUG: [Repository] API Response - code: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    println("DEBUG: [Repository] Raw response: $body")

                    // Parse OuterProfileResponse
                    val outerResponse = gson.fromJson(body?.string(), OuterProfileResponse::class.java)
                    println("DEBUG: [Repository] Outer response parsed: success=${outerResponse.success}")

                    if (outerResponse.success) {
                        val innerResponse = outerResponse.data
                        println("DEBUG: [Repository] Inner response: success=${innerResponse?.success}")

                        if (innerResponse?.success == true) {
                            val userData = innerResponse.data
                            println("DEBUG: [Repository] User data found!")
                            println("DEBUG: [Repository]   - id: ${userData?.id}")
                            println("DEBUG: [Repository]   - email: ${userData?.email}")
                            println("DEBUG: [Repository]   - displayName: ${userData?.displayName}")
                            println("DEBUG: [Repository]   - phone: ${userData?.phone}")
                            println("DEBUG: [Repository]   - role: ${userData?.role}")
                            println("DEBUG: [Repository]   - status: ${userData?.status}")

                            if (userData != null) {
                                ApiResult.Success(userData)
                            } else {
                                println("DEBUG: [Repository] User data is null")
                                ApiResult.Failure(Exception("User data is null"))
                            }
                        } else {
                            println("DEBUG: [Repository] Inner response not successful: ${innerResponse?.message}")
                            ApiResult.Failure(Exception(innerResponse?.message ?: "Inner API failed"))
                        }
                    } else {
                        println("DEBUG: [Repository] Outer response not successful")
                        ApiResult.Failure(Exception("Outer API failed"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG: [Repository] API Error - ${response.code()}: ${errorBody}")
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                println("DEBUG: [Repository] IOException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi kết nối: ${e.message}"))
            } catch (e: HttpException) {
                println("DEBUG: [Repository] HttpException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi server: ${e.message}"))
            } catch (e: Exception) {
                println("DEBUG: [Repository] Exception: ${e.message}")
                ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
            }
        }
    }



    suspend fun updateProfile(updateRequest: UpdateProfileRequest): ApiResult<UpdatedUserData> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting updateProfile...")
                println("DEBUG: [Repository] Update request:")
                println("DEBUG: [Repository]   - displayName: ${updateRequest.displayName}")
                println("DEBUG: [Repository]   - phone: ${updateRequest.phone}")
                println("DEBUG: [Repository]   - avatarUrl: ${updateRequest.avatarUrl}")

                val response = profileService.updateMe(updateRequest)
                println("DEBUG: [Repository] Update API Response - isSuccessful: ${response.isSuccessful}")
                println("DEBUG: [Repository] Update API Response - code: ${response.code()}")

                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    println("DEBUG: [Repository] Update response: success=${updateResponse?.success}")

                    if (updateResponse?.success == true) {
                        val updatedUserData = updateResponse.data
                        println("DEBUG: [Repository] Update successful!")
                        println("DEBUG: [Repository]   - Updated displayName: ${updatedUserData.displayName}")
                        println("DEBUG: [Repository]   - Updated phone: ${updatedUserData.phone}")
                        println("DEBUG: [Repository]   - Updated avatarUrl: ${updatedUserData.avatarUrl}")
                        println("DEBUG: [Repository]   - Updated at: ${updatedUserData.updatedAt}")

                        ApiResult.Success(updatedUserData)
                    } else {
                        println("DEBUG: [Repository] Update failed: ${updateResponse?.message}")
                        ApiResult.Failure(Exception(updateResponse?.message ?: "Update failed"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG: [Repository] Update API Error - ${response.code()}: $errorBody")
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                println("DEBUG: [Repository] Update IOException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi kết nối khi cập nhật: ${e.message}"))
            } catch (e: HttpException) {
                println("DEBUG: [Repository] Update HttpException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi server khi cập nhật: ${e.message}"))
            } catch (e: Exception) {
                println("DEBUG: [Repository] Update Exception: ${e.message}")
                ApiResult.Failure(Exception("Lỗi không xác định khi cập nhật: ${e.message}"))
            }
        }
    }



}