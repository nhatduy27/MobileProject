package com.example.foodapp.data.remote.client.response.profile

import com.google.gson.annotations.SerializedName

// ========== API RESULT ==========
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

// ========== PROFILE MODELS ==========
data class OuterProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: InnerProfileResponse? = null,
)

data class InnerProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: UserProfileData? = null,

    @SerializedName("message")
    val message: String? = null
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

    @SerializedName("createdAt")
    val createdAt: String? = null,

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

data class UpdateProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: UpdatedUserData
)

data class UpdatedUserData(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("avatarUrl")
    val avatarUrl: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
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
    val message: String,

    @SerializedName("data")
    val data: AddressData
)

data class AddressData(
    @SerializedName("id")
    val id: String,

    @SerializedName("label")
    val label: String,

    @SerializedName("fullAddress")
    val fullAddress: String,

    @SerializedName("building")
    val building: String?,

    @SerializedName("room")
    val room: String?,

    @SerializedName("note")
    val note: String?,

    @SerializedName("isDefault")
    val isDefault: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)