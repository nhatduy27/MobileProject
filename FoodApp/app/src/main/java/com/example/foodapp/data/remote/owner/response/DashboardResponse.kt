package com.example.foodapp.data.remote.owner.response

import com.example.foodapp.data.model.owner.DashboardDayRevenue
import com.example.foodapp.data.model.owner.DashboardRecentOrder
import com.example.foodapp.data.model.owner.DashboardStat
import com.example.foodapp.data.model.owner.DashboardTopProduct

/**
 * DTOs for Owner Dashboard API
 */

data class DashboardResponse(
    val stats: List<StatDto>,
    val weeklyRevenue: List<DayRevenueDto>,
    val recentOrders: List<RecentOrderDto>,
    val topProducts: List<TopProductDto>
) {
    data class StatDto(
        val iconRes: Int,
        val value: String,
        val label: String,
        val colorHex: Long // store color as hex ARGB long
    )

    data class DayRevenueDto(
        val day: String,
        val amount: Int
    )

    data class RecentOrderDto(
        val orderId: String,
        val customer: String,
        val status: String,
        val amount: Int
    )

    data class TopProductDto(
        val name: String,
        val quantity: Int,
        val revenue: String
    )

    // Conversion helpers to domain models
    fun toStats(): List<DashboardStat> = stats.map { dto ->
        DashboardStat(
            iconRes = dto.iconRes,
            value = dto.value,
            label = dto.label,
            color = androidx.compose.ui.graphics.Color(dto.colorHex)
        )
    }

    fun toWeeklyRevenue(): List<DashboardDayRevenue> = weeklyRevenue.map { dto ->
        DashboardDayRevenue(dto.day, dto.amount)
    }

    fun toRecentOrders(): List<DashboardRecentOrder> = recentOrders.map { dto ->
        DashboardRecentOrder(dto.orderId, dto.customer, dto.status, dto.amount)
    }

    fun toTopProducts(): List<DashboardTopProduct> = topProducts.map { dto ->
        DashboardTopProduct(dto.name, dto.quantity, dto.revenue)
    }
}
