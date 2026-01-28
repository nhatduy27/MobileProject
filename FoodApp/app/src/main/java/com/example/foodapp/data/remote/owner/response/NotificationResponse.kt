package com.example.foodapp.data.remote.owner.response

import com.example.foodapp.data.model.owner.notification.*
import com.google.gson.annotations.SerializedName

// ==================== Request DTOs ====================

/**
 * Request body for POST /notifications/tokens
 */
data class RegisterDeviceTokenRequest(
    val token: String,
    val platform: String, // "android" or "ios"
    val deviceInfo: DeviceInfoRequest? = null
)

data class DeviceInfoRequest(
    val model: String? = null,
    val osVersion: String? = null
)

/**
 * Request body for PUT /notifications/preferences
 * Note: transactional cannot be modified (always true)
 */
data class UpdateNotificationPreferencesRequest(
    val informational: Boolean? = null,
    val marketing: Boolean? = null
)

// ==================== Response Wrappers ====================

/**
 * Response wrapper for device token registration
 */
data class WrappedDeviceTokenResponse(
    val success: Boolean,
    val data: DeviceTokenDto?,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Response wrapper for notifications list with pagination
 */
data class WrappedNotificationsResponse(
    val success: Boolean,
    val data: NotificationPaginatedData?,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Paginated notifications data
 */
data class NotificationPaginatedData(
    val items: List<NotificationDto>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val unreadCount: Int
)

/**
 * Response wrapper for single notification
 */
data class WrappedNotificationResponse(
    val success: Boolean,
    val data: NotificationDto?,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Response wrapper for mark all as read
 */
data class WrappedMarkAllReadResponse(
    val success: Boolean,
    val data: MarkAllReadData?,
    val message: String? = null,
    val timestamp: String? = null
)

data class MarkAllReadData(
    val updated: Int
)

/**
 * Response wrapper for notification preferences
 */
data class WrappedNotificationPreferencesResponse(
    val success: Boolean,
    val data: NotificationPreferencesDto?,
    val message: String? = null,
    val timestamp: String? = null
)

// ==================== DTOs ====================

/**
 * DTO for Device Token from API
 */
data class DeviceTokenDto(
    val id: String,
    val userId: String,
    val token: String,
    val platform: String,
    val deviceInfo: DeviceInfoDto?,
    val createdAt: String,
    val lastUsedAt: String
) {
    fun toDeviceToken(): DeviceToken = DeviceToken(
        id = id,
        userId = userId,
        token = token,
        platform = platform,
        deviceInfo = deviceInfo?.toDeviceInfo(),
        createdAt = createdAt,
        lastUsedAt = lastUsedAt
    )
}

data class DeviceInfoDto(
    val model: String?,
    val osVersion: String?
) {
    fun toDeviceInfo(): DeviceInfo = DeviceInfo(
        model = model,
        osVersion = osVersion
    )
}

/**
 * DTO for Notification from API
 */
data class NotificationDto(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val imageUrl: String?,
    val type: String,
    val data: Map<String, Any>?,
    val read: Boolean,
    val readAt: String?,
    val orderId: String?,
    val shopId: String?,
    val createdAt: String
) {
    fun toNotification(): Notification = Notification(
        id = id,
        userId = userId,
        title = title,
        body = body,
        imageUrl = imageUrl,
        type = NotificationType.fromString(type),
        data = data,
        read = read,
        readAt = readAt,
        orderId = orderId,
        shopId = shopId,
        createdAt = createdAt
    )
}

/**
 * DTO for Notification Preferences from API
 */
data class NotificationPreferencesDto(
    val userId: String,
    val transactional: Boolean,
    val informational: Boolean,
    val marketing: Boolean,
    val updatedAt: String
) {
    fun toNotificationPreferences(): NotificationPreferences = NotificationPreferences(
        userId = userId,
        transactional = transactional,
        informational = informational,
        marketing = marketing,
        updatedAt = updatedAt
    )
}
