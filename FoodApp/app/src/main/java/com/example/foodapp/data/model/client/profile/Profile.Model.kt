package com.example.foodapp.data.model.client.profile

import com.google.gson.annotations.SerializedName


sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

//Model cho response từ API
data class GetProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: UserProfileData? = null,
)

data class UserProfileData(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("role")
    val role: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("addresses")
    val addresses: List<UserAddress> = emptyList()
)

data class UserAddress(
    @SerializedName("id")
    val id: String,

    @SerializedName("label")
    val label: String,

    @SerializedName("fullAddress")
    val fullAddress: String,

    @SerializedName("isDefault")
    val isDefault: Boolean = false
)