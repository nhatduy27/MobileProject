package com.example.foodapp.data.model.shipper

import androidx.compose.ui.graphics.Color

// Model cho lịch sử giao hàng của Shipper

data class DeliveryHistory(
    val orderId: String,
    val customerName: String,
    val date: String,
    val time: String,
    val pickupAddress: String,
    val deliveryAddress: String,
    val distance: String,
    val earnings: Int,
    val status: HistoryStatus,
    val rating: Double? = null
)

enum class HistoryStatus(val displayName: String, val color: Color) {
    COMPLETED("Hoàn thành", Color(0xFF4CAF50)),
    CANCELLED("Đã hủy", Color(0xFFE53935)),
    FAILED("Thất bại", Color(0xFFFF9800))
}
