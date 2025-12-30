package com.example.foodapp.data.model.owner

import androidx.compose.ui.graphics.Color

enum class ShipperStatus(val displayName: String, val color: Color) {
    AVAILABLE("Đang rảnh", Color(0xFF4CAF50)),
    DELIVERING("Đang giao", Color(0xFF2196F3)),
    OFFLINE("Nghỉ", Color(0xFF999999))
}

/**
 * Model đại diện cho một shipper trong hệ thống (phía chủ quán quản lý).
 */
data class Shipper(
    val id: String,
    val name: String,
    val phone: String,
    val rating: Double,
    val totalDeliveries: Int,
    val todayDeliveries: Int,
    val status: ShipperStatus,
    val avatarUrl: String
)
