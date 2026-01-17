package com.example.foodapp.data.repository.client.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.example.foodapp.data.remote.api.ApiClient
import com.example.foodapp.data.remote.client.response.profile.*

class ProfileRepository {

    private val profileService = ApiClient.profileApiService

    suspend fun getUserProfile(): ApiResult<UserProfileData> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting getUserProfile...")

                val response = profileService.getMe()

                if (response.isSuccessful) {
                    val body = response.body()
                    println("DEBUG: [Repository] Response received: ${body != null}")

                    if (body != null) {
                        println("DEBUG: [Repository] Response success: ${body.success}")
                        println("DEBUG: [Repository] Response data: ${body.data}")

                        if (body.success) {
                            val userData = body.data
                            if (userData != null) {
                                println("DEBUG: [Repository] User profile loaded: ${userData.displayName}, email: ${userData.email}")
                                ApiResult.Success(userData)
                            } else {
                                println("DEBUG: [Repository] User data is null")
                                ApiResult.Failure(Exception("Dữ liệu người dùng trống"))
                            }
                        } else {
                            println("DEBUG: [Repository] API returned success=false")
                            ApiResult.Failure(Exception(body.message ?: "Không thể lấy thông tin người dùng"))
                        }
                    } else {
                        println("DEBUG: [Repository] Response body is null")
                        ApiResult.Failure(Exception("Không có dữ liệu trả về"))
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
                println("DEBUG: [Repository] HttpException: ${e.code()} - ${e.message()}")
                ApiResult.Failure(Exception("Lỗi server: ${e.code()} - ${e.message()}"))
            } catch (e: Exception) {
                println("DEBUG: [Repository] Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(Exception("Lỗi không xác định: ${e.message}"))
            }
        }
    }

    suspend fun updateProfile(updateRequest: UpdateProfileRequest): ApiResult<UpdatedUserData> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting updateProfile...")

                val response = profileService.updateMe(updateRequest)

                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    println("DEBUG: [Repository] Update response: ${updateResponse != null}")

                    if (updateResponse != null) {
                        println("DEBUG: [Repository] Update success: ${updateResponse.success}")

                        if (updateResponse.success) {
                            val updatedUserData = updateResponse.data
                            if (updatedUserData != null) {
                                println("DEBUG: [Repository] Profile updated successfully")
                                ApiResult.Success(updatedUserData)
                            } else {
                                println("DEBUG: [Repository] Updated data is null")
                                ApiResult.Failure(Exception("Dữ liệu cập nhật trống"))
                            }
                        } else {
                            println("DEBUG: [Repository] Update failed: ${updateResponse.message}")
                            ApiResult.Failure(Exception(updateResponse.message ?: "Cập nhật thất bại"))
                        }
                    } else {
                        println("DEBUG: [Repository] Update response body is null")
                        ApiResult.Failure(Exception("Không có phản hồi từ server"))
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
                e.printStackTrace()
                ApiResult.Failure(Exception("Lỗi không xác định khi cập nhật: ${e.message}"))
            }
        }
    }

    suspend fun createAddress(addressRequest: CreateAddressRequest): ApiResult<AddressData> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting createAddress...")

                val response = profileService.createAddress(addressRequest)

                if (response.isSuccessful) {
                    val addressResponse = response.body()
                    println("DEBUG: [Repository] Create address response: ${addressResponse != null}")

                    if (addressResponse != null) {
                        println("DEBUG: [Repository] Create address success: ${addressResponse.success}")

                        if (addressResponse.success) {
                            val addressData = addressResponse.data
                            if (addressData != null) {
                                println("DEBUG: [Repository] Address created successfully: ${addressData.id}")
                                ApiResult.Success(addressData)
                            } else {
                                println("DEBUG: [Repository] Created address data is null")
                                ApiResult.Failure(Exception("Dữ liệu địa chỉ trống"))
                            }
                        } else {
                            println("DEBUG: [Repository] Address creation failed: ${addressResponse.message}")
                            ApiResult.Failure(Exception(addressResponse.message ?: "Tạo địa chỉ thất bại"))
                        }
                    } else {
                        println("DEBUG: [Repository] Create address response body is null")
                        ApiResult.Failure(Exception("Không có phản hồi từ server"))
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
                e.printStackTrace()
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
                    println("DEBUG: [Repository] Get addresses response: ${addressesResponse != null}")

                    if (addressesResponse != null) {
                        println("DEBUG: [Repository] Get addresses success: ${addressesResponse.success}")

                        if (addressesResponse.success) {
                            val addresses = addressesResponse.data ?: emptyList()
                            println("DEBUG: [Repository] Retrieved ${addresses.size} addresses")

                            // Debug log để kiểm tra dữ liệu
                            addresses.forEachIndexed { index, address ->
                                println("DEBUG: [Repository] Address $index: id=${address.id}, label=${address.label}, fullAddress=${address.fullAddress}")
                            }

                            ApiResult.Success(addresses)
                        } else {
                            println("DEBUG: [Repository] Get addresses failed: ${addressesResponse.message}")
                            ApiResult.Failure(Exception(addressesResponse.message ?: "Lấy danh sách địa chỉ thất bại"))
                        }
                    } else {
                        println("DEBUG: [Repository] Get addresses response body is null")
                        ApiResult.Failure(Exception("Không có phản hồi từ server"))
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
                println("DEBUG: [Repository] Get Addresses HttpException: ${e.code()} - ${e.message()}")
                ApiResult.Failure(Exception("Lỗi server khi lấy danh sách địa chỉ: ${e.code()} - ${e.message()}"))
            } catch (e: Exception) {
                println("DEBUG: [Repository] Get Addresses Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(Exception("Lỗi không xác định khi lấy danh sách địa chỉ: ${e.message}"))
            }
        }
    }

    suspend fun updateAddress(addressId: String, updateRequest: UpdateAddressRequest): ApiResult<AddressData> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting updateAddress for ID: $addressId")

                val response = profileService.updateAddress(addressId, updateRequest)

                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    println("DEBUG: [Repository] Update address response: ${updateResponse != null}")

                    if (updateResponse != null) {
                        println("DEBUG: [Repository] Update address success: ${updateResponse.success}")

                        if (updateResponse.success) {
                            val addressData = updateResponse.data
                            if (addressData != null) {
                                println("DEBUG: [Repository] Address updated successfully: ${addressData.id}")
                                ApiResult.Success(addressData)
                            } else {
                                println("DEBUG: [Repository] Updated address data is null")
                                ApiResult.Failure(Exception("Dữ liệu địa chỉ cập nhật trống"))
                            }
                        } else {
                            println("DEBUG: [Repository] Address update failed: ${updateResponse.message}")
                            ApiResult.Failure(Exception(updateResponse.message ?: "Cập nhật địa chỉ thất bại"))
                        }
                    } else {
                        println("DEBUG: [Repository] Update address response body is null")
                        ApiResult.Failure(Exception("Không có phản hồi từ server"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG: [Repository] Update Address API Error - ${response.code()}: $errorBody")
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                println("DEBUG: [Repository] Update Address IOException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi kết nối khi cập nhật địa chỉ: ${e.message}"))
            } catch (e: HttpException) {
                println("DEBUG: [Repository] Update Address HttpException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi server khi cập nhật địa chỉ: ${e.message}"))
            } catch (e: Exception) {
                println("DEBUG: [Repository] Update Address Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(Exception("Lỗi không xác định khi cập nhật địa chỉ: ${e.message}"))
            }
        }
    }

    suspend fun deleteAddress(addressId: String): ApiResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting deleteAddress for ID: $addressId")

                val response = profileService.deleteAddress(addressId)

                if (response.isSuccessful) {
                    val deleteResponse = response.body()
                    println("DEBUG: [Repository] Delete address response: ${deleteResponse != null}")

                    if (deleteResponse != null) {
                        println("DEBUG: [Repository] Delete address success: ${deleteResponse.success}")

                        if (deleteResponse.success) {
                            println("DEBUG: [Repository] Address deleted successfully")
                            ApiResult.Success(true)
                        } else {
                            println("DEBUG: [Repository] Address deletion failed: ${deleteResponse.message}")
                            ApiResult.Failure(Exception(deleteResponse.message ?: "Xóa địa chỉ thất bại"))
                        }
                    } else {
                        println("DEBUG: [Repository] Delete address response body is null")
                        ApiResult.Failure(Exception("Không có phản hồi từ server"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG: [Repository] Delete Address API Error - ${response.code()}: $errorBody")
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                println("DEBUG: [Repository] Delete Address IOException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi kết nối khi xóa địa chỉ: ${e.message}"))
            } catch (e: HttpException) {
                println("DEBUG: [Repository] Delete Address HttpException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi server khi xóa địa chỉ: ${e.message}"))
            } catch (e: Exception) {
                println("DEBUG: [Repository] Delete Address Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(Exception("Lỗi không xác định khi xóa địa chỉ: ${e.message}"))
            }
        }
    }

    suspend fun setDefaultAddress(addressId: String): ApiResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: [Repository] Starting setDefaultAddress for ID: $addressId")

                val response = profileService.setDefaultAddress(addressId)

                if (response.isSuccessful) {
                    val defaultResponse = response.body()
                    println("DEBUG: [Repository] Set default address response: ${defaultResponse != null}")

                    if (defaultResponse != null) {
                        println("DEBUG: [Repository] Set default address success: ${defaultResponse.success}")

                        if (defaultResponse.success) {
                            println("DEBUG: [Repository] Default address set successfully")
                            ApiResult.Success(true)
                        } else {
                            println("DEBUG: [Repository] Set default address failed: ${defaultResponse.message}")
                            ApiResult.Failure(Exception(defaultResponse.message ?: "Đặt địa chỉ mặc định thất bại"))
                        }
                    } else {
                        println("DEBUG: [Repository] Set default address response body is null")
                        ApiResult.Failure(Exception("Không có phản hồi từ server"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG: [Repository] Set Default Address API Error - ${response.code()}: $errorBody")
                    ApiResult.Failure(
                        Exception("Lỗi ${response.code()}: ${errorBody ?: response.message()}")
                    )
                }
            } catch (e: IOException) {
                println("DEBUG: [Repository] Set Default Address IOException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi kết nối khi đặt địa chỉ mặc định: ${e.message}"))
            } catch (e: HttpException) {
                println("DEBUG: [Repository] Set Default Address HttpException: ${e.message}")
                ApiResult.Failure(Exception("Lỗi server khi đặt địa chỉ mặc định: ${e.message}"))
            } catch (e: Exception) {
                println("DEBUG: [Repository] Set Default Address Exception: ${e.message}")
                e.printStackTrace()
                ApiResult.Failure(Exception("Lỗi không xác định khi đặt địa chỉ mặc định: ${e.message}"))
            }
        }
    }
}