package com.example.foodapp.data.model.owner

import com.google.gson.annotations.SerializedName

/**
 * API Response Models for /api/owner/shop/dashboard
 */
data class GetDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: DashboardData = DashboardData()
)

data class DashboardData(
    @SerializedName("today") val today: PeriodStats = PeriodStats(),
    @SerializedName("thisWeek") val thisWeek: PeriodStats = PeriodStats(),
    @SerializedName("thisMonth") val thisMonth: PeriodStats = PeriodStats(),
    @SerializedName("ordersByStatus") val ordersByStatus: Map<String, Int> = emptyMap(),
    @SerializedName("topProducts") val topProducts: List<TopProductStat> = emptyList(),
    @SerializedName("recentOrders") val recentOrders: List<RecentOrderStat> = emptyList()
)

data class PeriodStats(
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("orderCount") val orderCount: Int = 0,
    @SerializedName("avgOrderValue") val avgOrderValue: Double = 0.0,
    @SerializedName("pendingOrders") val pendingOrders: Int = 0
)

data class TopProductStat(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("soldCount") val soldCount: Int = 0,
    @SerializedName("revenue") val revenue: Double = 0.0
)

data class RecentOrderStat(
    @SerializedName("id") val id: String = "",
    @SerializedName("orderNumber") val orderNumber: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("createdAt") val createdAt: String = ""
)

