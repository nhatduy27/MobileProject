package com.example.foodapp.data.repository.user.profile

import android.util.Log
import com.example.foodapp.data.model.user.*
import com.example.foodapp.data.remote.user.UserProfileApiService
import com.example.foodapp.data.repository.user.base.UserProfileRepository
import com.example.foodapp.utils.ErrorParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Real implementation của UserProfileRepository
 * Gọi API thực sự từ backend
 */
class RealUserProfileRepository(
    private val apiService: UserProfileApiService
) : UserProfileRepository {

    companion object {
        private const val TAG = "RealUserProfileRepo"
    }

    override suspend fun getProfile(): Result<UserProfile> {
        return try {
            Log.d(TAG, "🔄 Fetching user profile")

            val response = apiService.getProfile()

            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!
                val profile = profileResponse.toUserProfile()
                Log.d(TAG, "✅ Got profile: ${profile.displayName}")
                Result.success(profile)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error fetching profile: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching profile", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun updateProfile(displayName: String?, phone: String?): Result<UserProfile> {
        return try {
            Log.d(TAG, "🔄 Updating profile: name=$displayName, phone=$phone")

            val request = UpdateProfileRequest(displayName = displayName, phone = phone)
            val response = apiService.updateProfile(request)

            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!
                val profile = profileResponse.toUserProfile()
                Log.d(TAG, "✅ Profile updated")
                Result.success(profile)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error updating profile: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception updating profile", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun uploadAvatar(imageFile: File): Result<String> {
        return try {
            Log.d(TAG, "🔄 Uploading avatar: ${imageFile.name}")

            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("avatar", imageFile.name, requestBody)

            val response = apiService.uploadAvatar(part)

            if (response.isSuccessful && response.body() != null) {
                val avatarUrl = response.body()!!.data?.avatarUrl
                if (avatarUrl != null) {
                    Log.d(TAG, "✅ Avatar uploaded: $avatarUrl")
                    Result.success(avatarUrl)
                } else {
                    Log.e(TAG, "❌ Avatar URL is null in response")
                    Result.failure(Exception("Server không trả về đường dẫn ảnh"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error uploading avatar: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception uploading avatar", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun getSettings(): Result<UserSettings> {
        return try {
            Log.d(TAG, "🔄 Fetching settings")

            val response = apiService.getSettings()

            if (response.isSuccessful && response.body() != null) {
                val settingsResponse = response.body()!!
                val settings = settingsResponse.data ?: UserSettings(
                    notifications = NotificationSettings(),
                    language = "vi",
                    currency = "VND"
                )
                Log.d(TAG, "✅ Got settings")
                Result.success(settings)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error fetching settings: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching settings", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun updateSettings(settings: UpdateSettingsRequest): Result<UserSettings> {
        return try {
            Log.d(TAG, "🔄 Updating settings")

            val response = apiService.updateSettings(settings)

            if (response.isSuccessful && response.body() != null) {
                val settingsResponse = response.body()!!
                val updatedSettings = settingsResponse.data ?: UserSettings(
                    notifications = NotificationSettings(),
                    language = "vi",
                    currency = "VND"
                )
                Log.d(TAG, "✅ Settings updated")
                Result.success(updatedSettings)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error updating settings: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception updating settings", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun getAddresses(): Result<List<Address>> {
        return try {
            Log.d(TAG, "🔄 Fetching addresses")

            val response = apiService.getAddresses()

            if (response.isSuccessful && response.body() != null) {
                val addresses = response.body()!!.data ?: emptyList()
                Log.d(TAG, "✅ Got ${addresses.size} addresses")
                Result.success(addresses)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error fetching addresses: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching addresses", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun createAddress(
        label: String,
        fullAddress: String,
        isDefault: Boolean
    ): Result<Address> {
        return try {
            Log.d(TAG, "🔄 Creating address: $label")

            val request = CreateAddressRequest(
                label = label,
                fullAddress = fullAddress,
                isDefault = isDefault
            )
            val response = apiService.createAddress(request)

            if (response.isSuccessful && response.body() != null) {
                val address = response.body()!!.toAddress()
                if (address != null) {
                    Log.d(TAG, "✅ Address created: ${address.id}")
                    Result.success(address)
                } else {
                    Result.failure(Exception("Không thể tạo địa chỉ"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error creating address: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception creating address", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun updateAddress(
        id: String,
        label: String?,
        fullAddress: String?,
        isDefault: Boolean?
    ): Result<Address> {
        return try {
            Log.d(TAG, "🔄 Updating address: $id")

            val request = UpdateAddressRequest(
                label = label,
                fullAddress = fullAddress,
                isDefault = isDefault
            )
            val response = apiService.updateAddress(id, request)

            if (response.isSuccessful && response.body() != null) {
                val address = response.body()!!.toAddress()
                if (address != null) {
                    Log.d(TAG, "✅ Address updated")
                    Result.success(address)
                } else {
                    Result.failure(Exception("Không thể cập nhật địa chỉ"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error updating address: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception updating address", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun deleteAddress(id: String): Result<String> {
        return try {
            Log.d(TAG, "🔄 Deleting address: $id")

            val response = apiService.deleteAddress(id)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Address deleted")
                Result.success("Đã xóa địa chỉ")
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error deleting address: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception deleting address", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun setDefaultAddress(id: String): Result<String> {
        return try {
            Log.d(TAG, "🔄 Setting default address: $id")

            val response = apiService.setDefaultAddress(id)

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Default address set")
                Result.success("Đã đặt địa chỉ mặc định")
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error setting default address: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception setting default address", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }

    override suspend fun deleteAccount(): Result<String> {
        return try {
            Log.d(TAG, "🔄 Deleting account")

            val response = apiService.deleteAccount()

            if (response.isSuccessful) {
                Log.d(TAG, "✅ Account deleted")
                Result.success("Đã xóa tài khoản")
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = ErrorParser.parseError(errorBody)
                Log.e(TAG, "❌ Error deleting account: $errorBody")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception deleting account", e)
            Result.failure(Exception(ErrorParser.parseException(e)))
        }
    }
}
