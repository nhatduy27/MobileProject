package com.example.foodapp.pages.shipper.notifications

import androidx.compose.ui.graphics.Color
import com.example.foodapp.pages.shipper.theme.ShipperColors

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType(val displayName: String, val color: Color) {
    NEW_ORDER("Đơn mới", ShipperColors.Primary),
    ORDER_UPDATE("Cập nhật đơn", ShipperColors.Warning),
    PAYMENT("Thanh toán", ShipperColors.Success),
    SYSTEM("Hệ thống", ShipperColors.TextSecondary),
    PROMOTION("Khuyến mãi", ShipperColors.Info)
}
