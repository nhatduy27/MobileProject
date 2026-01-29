package com.example.foodapp.data.model.user

import com.google.gson.annotations.SerializedName

/**
 * User Profile từ API GET /me
 */
data class UserProfile(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("role") val role: String,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("addresses") val addresses: List<Address>? = null
)

/**
 * Address Entity
 */
data class Address(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("fullAddress") val fullAddress: String,
    @SerializedName("building") val building: String? = null,
    @SerializedName("room") val room: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null
)

/**
 * User Settings
 */
data class UserSettings(
    @SerializedName("notifications") val notifications: NotificationSettings,
    @SerializedName("language") val language: String,
    @SerializedName("currency") val currency: String
)

data class NotificationSettings(
    @SerializedName("orderUpdates") val orderUpdates: Boolean = true,
    @SerializedName("promotions") val promotions: Boolean = true,
    @SerializedName("email") val email: Boolean = true,
    @SerializedName("push") val push: Boolean = true
)

/**
 * Request DTOs
 */
data class UpdateProfileRequest(
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

data class CreateAddressRequest(
    @SerializedName("label") val label: String,
    @SerializedName("fullAddress") val fullAddress: String,
    @SerializedName("building") val building: String? = null,
    @SerializedName("room") val room: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean = false
)

data class UpdateAddressRequest(
    @SerializedName("label") val label: String? = null,
    @SerializedName("fullAddress") val fullAddress: String? = null,
    @SerializedName("building") val building: String? = null,
    @SerializedName("room") val room: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean? = null
)

data class UpdateSettingsRequest(
    @SerializedName("notifications") val notifications: NotificationSettings? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("currency") val currency: String? = null
)

/**
 * Response Wrappers - API trả về format { success: true, data: ... }
 */
data class ProfileResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: UserProfile? = null,
    // Fallback: some endpoints return user directly
    @SerializedName("id") val id: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("addresses") val addresses: List<Address>? = null
) {
    fun toUserProfile(): UserProfile {
        return data ?: UserProfile(
            id = id ?: "",
            email = email ?: "",
            displayName = displayName ?: "",
            phone = phone,
            avatarUrl = avatarUrl,
            role = role ?: "",
            status = status ?: "",
            createdAt = createdAt ?: "",
            addresses = addresses
        )
    }
}

data class AddressesResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: List<Address>? = null
)

data class AddressResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: Address? = null,
    // Fallback
    @SerializedName("id") val id: String? = null,
    @SerializedName("label") val label: String? = null,
    @SerializedName("fullAddress") val fullAddress: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean? = null
) {
    fun toAddress(): Address? {
        return data ?: if (id != null) Address(
            id = id,
            label = label ?: "",
            fullAddress = fullAddress ?: "",
            isDefault = isDefault ?: false
        ) else null
    }
}

data class SettingsResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: UserSettings? = null
)

data class AvatarUploadResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("data") val data: AvatarData? = null
)

data class AvatarData(
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

data class MessageResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null
)
