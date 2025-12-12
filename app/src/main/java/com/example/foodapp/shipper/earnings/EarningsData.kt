package com.example.foodapp.shipper.earnings

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class EarningsData(
    val date: String,
    val totalOrders: Int,
    val totalEarnings: Int,
    val bonusEarnings: Int
)

data class EarningsSummary(
    val todayEarnings: Int,
    val weekEarnings: Int,
    val monthEarnings: Int,
    val totalOrders: Int,
    val completedOrders: Int,
    val averagePerOrder: Int
)

enum class EarningsPeriod(val displayName: String) {
    TODAY("Hôm nay"),
    WEEK("Tuần này"),
    MONTH("Tháng này"),
    ALL("Tất cả")
}
