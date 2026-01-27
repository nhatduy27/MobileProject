package com.example.foodapp.data.remote.client.response.profile

import com.google.gson.annotations.SerializedName

// ========== API RESULT ==========
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

// ========== PROFILE MODELS ==========
// API /api/me trả về: { "success": true, "data": { "id": "...", ... } }
data class ProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: UserProfileData? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

data class UserProfileData(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("emailVerified")
    val emailVerified: Boolean? = null,

    @SerializedName("fcmTokens")
    val fcmTokens: List<String>? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("addresses")
    val addresses: List<UserAddress>? = null
)

data class UserAddress(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("fullAddress")
    val fullAddress: String? = null,

    @SerializedName("isDefault")
    val isDefault: Boolean = false
)

// ========== UPDATE PROFILE MODELS ==========
data class UpdateProfileRequest(
    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)

// API update profile có thể trả về: { "success": true, "message": "...", "data": { ... } }
data class UpdateProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: UpdatedUserData? = null
)

data class UpdatedUserData(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("displayName")
    val displayName: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

// ========== CREATE ADDRESS MODELS ==========
data class CreateAddressRequest(
    @SerializedName("label")
    val label: String,

    @SerializedName("fullAddress")
    val fullAddress: String,

    @SerializedName("building")
    val building: String? = null,

    @SerializedName("room")
    val room: String? = null,

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("isDefault")
    val isDefault: Boolean = false
)

data class CreateAddressResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: AddressData? = null
)

data class AddressData(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("fullAddress")
    val fullAddress: String? = null,

    @SerializedName("building")
    val building: String? = null,

    @SerializedName("room")
    val room: String? = null,

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("isDefault")
    val isDefault: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

// ========== GET ADDRESSES RESPONSE MODELS ==========
// API /api/me/addresses trả về: { "success": true, "data": [] }
data class GetAddressesResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<AddressResponse>? = null,  // Data là array trực tiếp

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

data class AddressResponse(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("fullAddress")
    val fullAddress: String? = null,

    @SerializedName("building")
    val building: String? = null,

    @SerializedName("room")
    val room: String? = null,

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("isDefault")
    val isDefault: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

// ========== DELETE ADDRESS RESPONSE ==========
data class DeleteAddressResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null
)

// ========== UPDATE ADDRESS MODELS ==========
data class UpdateAddressRequest(
    @SerializedName("label")
    val label: String? = null,

    @SerializedName("fullAddress")
    val fullAddress: String? = null,

    @SerializedName("building")
    val building: String? = null,

    @SerializedName("room")
    val room: String? = null,

    @SerializedName("note")
    val note: String? = null,

    @SerializedName("isDefault")
    val isDefault: Boolean? = null
)

data class UpdateAddressResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: AddressData? = null
)

// ========== SET DEFAULT ADDRESS RESPONSE ==========
data class SetDefaultAddressResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null
)


data class UploadAvatarResponse(
    @SerializedName("avatarUrl")
    val avatarUrl: String
)