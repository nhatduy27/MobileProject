package com.example.foodapp.data.model.client.profile

import com.google.gson.annotations.SerializedName

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}


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

//-------------UPDATE PROFILE-----------------

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

// Dữ liệu user được update
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
