package com.example.foodapp.pages.shipper.notifications

import androidx.compose.ui.graphics.Color

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType(val displayName: String, val icon: String, val color: Color) {
    NEW_ORDER("Đơn mới", "📦", Color(0xFF2196F3)),
    ORDER_UPDATE("Cập nhật đơn", "🔄", Color(0xFFFF9800)),
    PAYMENT("Thanh toán", "💰", Color(0xFF4CAF50)),
    SYSTEM("Hệ thống", "⚙️", Color(0xFF757575)),
    PROMOTION("Khuyến mãi", "🎁", Color(0xFFE91E63))
}
