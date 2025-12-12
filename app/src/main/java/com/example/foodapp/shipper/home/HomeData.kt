package com.example.foodapp.shipper.home

import androidx.compose.ui.graphics.Color

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

data class ShipperStats(
    val todayOrders: Int,
    val todayEarnings: Int,
    val completionRate: Int,
    val rating: Double
)
