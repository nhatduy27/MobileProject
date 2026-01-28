package com.example.foodapp.data.model.owner.notification

/**
 * Notification model for Owner
 */
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val type: NotificationType,
    val data: Map<String, Any>? = null,
    val read: Boolean = false,
    val readAt: String? = null,
    val orderId: String? = null,
    val shopId: String? = null,
    val createdAt: String
)

/**
 * Device Token model
 */
data class DeviceToken(
    val id: String,
    val userId: String,
    val token: String,
    val platform: String,
    val deviceInfo: DeviceInfo? = null,
    val createdAt: String,
    val lastUsedAt: String
)

data class DeviceInfo(
    val model: String? = null,
    val osVersion: String? = null
)

/**
 * Notification Preferences model
 */
data class NotificationPreferences(
    val userId: String,
    val transactional: Boolean = true, // Always true, cannot be disabled
    val informational: Boolean = true,
    val marketing: Boolean = true,
    val updatedAt: String
)

/**
 * Notification types from backend
 */
enum class NotificationType(val displayName: String) {
    // Order
    ORDER_CONFIRMED("Đơn hàng đã xác nhận"),
    ORDER_PREPARING("Đang chuẩn bị"),
    ORDER_READY("Sẵn sàng"),
    ORDER_SHIPPING("Đang giao"),
    ORDER_DELIVERED("Đã giao"),
    ORDER_CANCELLED("Đã hủy"),
    
    // Payment
    PAYMENT_SUCCESS("Thanh toán thành công"),
    PAYMENT_FAILED("Thanh toán thất bại"),
    PAYMENT_REFOUNDED("Hoàn tiền"),
    
    // Shipper
    SHIPPER_ASSIGNED("Đã phân shipper"),
    SHIPPER_APPLICATION_APPROVED("Đơn ứng tuyển đã duyệt"),
    SHIPPER_APPLICATION_REJECTED("Đơn ứng tuyển bị từ chối"),
    
    // Owner specific
    NEW_ORDER("Đơn hàng mới"),
    SHIPPER_APPLIED("Shipper ứng tuyển"),
    DAILY_SUMMARY("Tổng kết hàng ngày"),
    SUBSCRIPTION_EXPIRING("Gói đăng ký sắp hết hạn"),
    
    // Promotions
    PROMOTION("Khuyến mãi"),
    VOUCHER_AVAILABLE("Voucher mới"),
    
    // Unknown
    UNKNOWN("Thông báo");

    companion object {
        fun fromString(value: String): NotificationType {
            return entries.find { it.name == value } ?: UNKNOWN
        }
    }
}

/**
 * Paginated notifications result
 */
data class PaginatedNotifications(
    val items: List<Notification>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val unreadCount: Int
)
