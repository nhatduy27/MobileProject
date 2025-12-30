package com.example.foodapp.data.model.owner

import androidx.compose.ui.graphics.Color

enum class OrderStatus(val displayName: String, val color: Color) {
    PENDING("Chờ xác nhận", Color(0xFFFFA500)),
    PROCESSING("Đang chuẩn bị", Color(0xFF2196F3)),
    DELIVERING("Đang giao", Color(0xFF4CAF50)),
    COMPLETED("Hoàn thành", Color(0xFF607D8B)),
    CANCELLED("Đã hủy", Color(0xFFF44336))
}

/**
 * Model đơn hàng phía chủ quán quản lý.
 */
data class Order(
    val id: String,
    val customerName: String,
    val location: String,
    val items: String,
    val time: String,
    val price: Int,
    val status: OrderStatus
)
