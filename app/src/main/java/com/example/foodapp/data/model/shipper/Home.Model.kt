package com.example.foodapp.data.model.shipper

import androidx.compose.ui.graphics.Color

// Các model cho màn Home của Shipper

data class DeliveryTask(
    val orderId: String,
    val customerName: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val distance: String,
    val fee: Int,
    val status: TaskStatus
)

enum class TaskStatus(val displayName: String, val color: Color) {
    PENDING("Chờ nhận", Color(0xFFFFA500)),
    PICKING_UP("Đang lấy hàng", Color(0xFF2196F3)),
    DELIVERING("Đang giao", Color(0xFF4CAF50)),
    COMPLETED("Hoàn thành", Color(0xFF999999))
}

// Thống kê nhanh cho màn Home

data class ShipperStats(
    val todayOrders: Int,
    val todayEarnings: Int,
    val completionRate: Int,
    val rating: Double
)
