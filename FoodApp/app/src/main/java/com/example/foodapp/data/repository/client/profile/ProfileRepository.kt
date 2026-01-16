package com.example.foodapp.data.repository.client.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.profile.*
import com.google.gson.Gson

class ProfileRepository {

    private val profileService = ApiClient.profileApiService
    private val gson = Gson()

    suspend fun getUserProfile(): ApiResult<UserProfileData> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting getUserProfile...")

                val response = profileService.getMe()

                if (response.isSuccessful) {
                    val body = response.body()

                    // Parse OuterProfileResponse
                    val outerResponse = gson.fromJson(body?.string(), OuterProfileResponse::class.java)

                    if (outerResponse.success) {
                        val innerResponse = outerResponse.data
                        println("DEBUG: [Repository] Inner response: success=${innerResponse?.success}")

                        if (innerResponse?.success == true) {
                            val userData = innerResponse.data

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
                val response = profileService.updateMe(updateRequest)

                if (response.isSuccessful) {
                    val updateResponse = response.body()

                    if (updateResponse?.success == true) {
                        val updatedUserData = updateResponse.data

                        ApiResult.Success(updatedUserData)
                    } else {
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


    suspend fun createAddress(addressRequest: CreateAddressRequest): ApiResult<AddressData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = profileService.createAddress(addressRequest)
                if (response.isSuccessful) {
                    val addressResponse = response.body()

                    if (addressResponse?.success == true) {
                        val addressData = addressResponse.data

                        ApiResult.Success(addressData)
                    } else {
                        println("DEBUG: [Repository] Address creation failed: ${addressResponse?.message}")
                        ApiResult.Failure(Exception(addressResponse?.message ?: "Address creation failed"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG: [Repository] Create Address API Error - ${response.code()}: $errorBody")
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                println("DEBUG: [Repository] Create Address IOException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi kết nối khi tạo địa chỉ: ${e.message}"))
            } catch (e: HttpException) {
                println("DEBUG: [Repository] Create Address HttpException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi server khi tạo địa chỉ: ${e.message}"))
            } catch (e: Exception) {
                println("DEBUG: [Repository] Create Address Exception: ${e.message}")
                ApiResult.Failure(Exception("Lỗi không xác định khi tạo địa chỉ: ${e.message}"))
            }
        }
    }


    suspend fun getAddresses(): ApiResult<List<AddressResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting getAddresses...")

                val response = profileService.getAddresses()

                if (response.isSuccessful) {
                    val addressesResponse = response.body()

                    if (addressesResponse?.success == true) {
                        // Sửa ở đây: addressesResponse.data là InnerAddressData,
                        // cần lấy addressesResponse.data?.data để lấy List<AddressResponse>
                        val addresses = addressesResponse.data?.data ?: emptyList()
                        println("DEBUG: [Repository] Retrieved ${addresses.size} addresses")

                        // Debug log để kiểm tra dữ liệu
                        addresses.forEach { address ->
                            println("DEBUG: [Repository] Address: ${address.id}, ${address.label}, ${address.fullAddress}")
                        }

                        ApiResult.Success(addresses)
                    } else {
                        println("DEBUG: [Repository] API response not successful")
                        ApiResult.Failure(Exception("Failed to get addresses"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG: [Repository] Get Addresses API Error - ${response.code()}: $errorBody")
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                println("DEBUG: [Repository] Get Addresses IOException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi kết nối khi lấy danh sách địa chỉ: ${e.message}"))
            } catch (e: HttpException) {
                println("DEBUG: [Repository] Get Addresses HttpException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi server khi lấy danh sách địa chỉ: ${e.message}"))
            } catch (e: Exception) {
                println("DEBUG: [Repository] Get Addresses Exception: ${e.message}")
                ApiResult.Failure(Exception("Lỗi không xác định khi lấy danh sách địa chỉ: ${e.message}"))
            }
        }
    }

}


