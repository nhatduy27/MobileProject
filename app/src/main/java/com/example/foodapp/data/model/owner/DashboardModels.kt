package com.example.foodapp.data.model.owner

import androidx.compose.ui.graphics.Color

/**
 * Models dữ liệu cho màn hình Dashboard của chủ quán.
 */

data class DashboardStat(
    val iconRes: Int,
    val value: String,
    val label: String,
    val color: Color
)

data class DashboardDayRevenue(
    val day: String,
    val amount: Int
)

data class DashboardRecentOrder(
    val orderId: String,
    val customer: String,
    val status: String,
    val amount: Int
)

data class DashboardTopProduct(
    val name: String,
    val quantity: Int,
    val revenue: String
)
