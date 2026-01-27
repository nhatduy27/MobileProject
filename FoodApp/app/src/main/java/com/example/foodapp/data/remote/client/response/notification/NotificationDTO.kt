package com.example.foodapp.data.remote.client.response.notification

import com.google.gson.annotations.SerializedName

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()

    object Loading : ApiResult<Nothing>()
}

/**
 * Request đăng ký device token
 * Format:
 * POST /api/notifications/devices
 * {
 *   "token": "cRLstRFyj7k:APA91bHi8L...",
 *   "platform": "android",
 *   "deviceInfo": {
 *     "model": "Samsung Galaxy S21",
 *     "osVersion": "Android 12"
 *   }
 * }
 */
data class RegisterDeviceTokenRequest(
    @SerializedName("token")
    val token: String,

    @SerializedName("platform")
    val platform: String,

    @SerializedName("deviceInfo")
    val deviceInfo: DeviceInfo
)

/**
 * Thông tin thiết bị
 */
data class DeviceInfo(
    @SerializedName("model")
    val model: String? = null,

    @SerializedName("osVersion")
    val osVersion: String? = null
)

/**
 * Response cho đăng ký device token
 * Format:
 * {
 *   "id": "device_1",
 *   "userId": "user_1",
 *   "token": "cRLstRFyj7k:APA91bHi8L...",
 *   "platform": "android",
 *   "deviceInfo": {
 *     "model": "string",
 *     "osVersion": "string"
 *   },
 *   "createdAt": "2024-01-08T10:00:00Z",
 *   "lastUsedAt": "2024-01-08T10:00:00Z"
 * }
 */
data class RegisterDeviceTokenResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("token")
    val token: String,

    @SerializedName("platform")
    val platform: String,

    @SerializedName("deviceInfo")
    val deviceInfo: DeviceInfo,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("lastUsedAt")
    val lastUsedAt: String
)


/**
 * Response lấy danh sách thông báo
 * Format:
 * {
 *   "items": [],
 *   "total": 50,
 *   "page": 1,
 *   "limit": 20,
 *   "unreadCount": 15
 * }
 */
data class NotificationListResponse(
    @SerializedName("items")
    val items: List<NotificationResponse>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("unreadCount")
    val unreadCount: Int
)

// NotificationResponse.kt
/**
 * Thông báo chi tiết
 * Format từ response thực tế:
 * {
 *   "id": "sELBdDM2mxQk7snTSIs3",
 *   "userId": "X6dK9NqP8YV1e8FsPpZMhN2HyKD2",
 *   "title": "Order confirmed",
 *   "body": "Your order #ORD-1769321888543-7YU9B8 has been confirmed and will be prepared shortly",
 *   "type": "ORDER_CONFIRMED",
 *   "read": false,
 *   "createdAt": "2026-01-25T09:17:39.059Z",
 *   "data": {
 *     "orderId": "UE2bE41pFlWjuErbX4Yq",
 *     "orderNumber": "ORD-1769321888543-7YU9B8",
 *     "shopId": "nzIfau9GtqIPyWkmLyku"
 *   },
 *   "orderId": "UE2bE41pFlWjuErbX4Yq",
 *   "shopId": "nzIfau9GtqIPyWkmLyku",
 *   "sentAt": "2026-01-25T09:17:38.871Z",
 *   "deliveryStatus": "FAILED",
 *   "deliveryErrorCode": "NO_TOKENS",
 *   "deliveryErrorMessage": "No device tokens registered for user"
 * }
 */
data class NotificationResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

    @SerializedName("type")
    val type: String,

    @SerializedName("data")
    val data: NotificationData? = null,

    @SerializedName("read")
    val read: Boolean,

    @SerializedName("readAt")
    val readAt: String? = null,

    @SerializedName("orderId")
    val orderId: String? = null,

    @SerializedName("shopId")
    val shopId: String? = null,

    @SerializedName("createdAt")
    val createdAt: String,

    // THÊM CÁC FIELD MỚI TỪ RESPONSE
    @SerializedName("sentAt")
    val sentAt: String? = null,

    @SerializedName("deliveryStatus")
    val deliveryStatus: String? = null,

    @SerializedName("deliveryErrorCode")
    val deliveryErrorCode: String? = null,

    @SerializedName("deliveryErrorMessage")
    val deliveryErrorMessage: String? = null
)

/**
 * Dữ liệu bổ sung của thông báo
 * Format từ response thực tế:
 * {
 *   "orderId": "UE2bE41pFlWjuErbX4Yq",
 *   "orderNumber": "ORD-1769321888543-7YU9B8",
 *   "shopId": "nzIfau9GtqIPyWkmLyku"
 * }
 */
data class NotificationData(
    @SerializedName("orderId")
    val orderId: String? = null,
)

/**
 * Response của API đánh dấu thông báo đã đọc
 * Format:
 * {
 *   "id": "notif_1",
 *   "userId": "user_1",
 *   "title": "string",
 *   "body": "string",
 *   "type": "string",
 *   "read": true,
 *   "readAt": "2024-01-08T10:05:00Z",
 *   "createdAt": "string"
 * }
 */
data class MarkNotificationReadResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("body")
    val body: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("read")
    val read: Boolean,

    @SerializedName("readAt")
    val readAt: String? = null,

    @SerializedName("createdAt")
    val createdAt: String
)


data class MarkAllNotificationsReadResponse(
    @SerializedName("updated")
    val updated: Int
)


/**
 * Base response structure cho tất cả API
 */
data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T,

    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * Data object cho notification preferences GET
 * Format trong data field:
 * {
 *   "userId": "X6dK9NqP8YV1e8FsPpZMhN2HyKD2",
 *   "transactional": true,
 *   "marketing": false,
 *   "informational": true,
 *   "updatedAt": "2026-01-27T15:10:40.446Z"
 * }
 */
data class NotificationPreferencesData(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("transactional")
    val transactional: Boolean,

    @SerializedName("informational")
    val informational: Boolean,

    @SerializedName("marketing")
    val marketing: Boolean,

    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * Response object cho backward compatibility (cho ViewModel cũ)
 * Có thể giữ hoặc không, tuỳ vào việc bạn muốn refactor
 */
data class NotificationPreferencesResponse(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("transactional")
    val transactional: Boolean,

    @SerializedName("informational")
    val informational: Boolean,

    @SerializedName("marketing")
    val marketing: Boolean,

    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * Request to update notification preferences
 * Format:
 * PATCH /notifications/preferences
 * {
 *   "informational": true,
 *   "marketing": false
 * }
 */
data class UpdateNotificationPreferencesRequest(
    @SerializedName("informational")
    val informational: Boolean? = null,

    @SerializedName("marketing")
    val marketing: Boolean? = null
)

/**
 * Data object cho notification preferences UPDATE
 * Format trong data field:
 * {
 *   "userId": "X6dK9NqP8YV1e8FsPpZMhN2HyKD2",
 *   "transactional": true,
 *   "informational": true,
 *   "marketing": true,
 *   "updatedAt": "2026-01-27T15:02:11.699Z"
 * }
 */
data class UpdateNotificationPreferencesData(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("transactional")
    val transactional: Boolean,

    @SerializedName("informational")
    val informational: Boolean,

    @SerializedName("marketing")
    val marketing: Boolean,

    @SerializedName("updatedAt")
    val updatedAt: String
)