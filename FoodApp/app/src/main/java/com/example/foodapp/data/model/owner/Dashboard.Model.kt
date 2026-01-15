package com.example.foodapp.data.model.owner

import com.google.gson.annotations.SerializedName

/**
 * API Response Models for /api/owner/shop/dashboard
 */
data class GetDashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: DashboardData
)

data class DashboardData(
    @SerializedName("today") val today: PeriodStats,
    @SerializedName("thisWeek") val thisWeek: PeriodStats,
    @SerializedName("thisMonth") val thisMonth: PeriodStats,
    @SerializedName("ordersByStatus") val ordersByStatus: Map<String, Int>,
    @SerializedName("topProducts") val topProducts: List<TopProductStat>,
    @SerializedName("recentOrders") val recentOrders: List<RecentOrderStat>
)

data class PeriodStats(
    @SerializedName("revenue") val revenue: Double?,
    @SerializedName("orderCount") val orderCount: Int?,
    @SerializedName("avgOrderValue") val avgOrderValue: Double?,
    @SerializedName("pendingOrders") val pendingOrders: Int? // Only present in 'today' usually
)

data class TopProductStat(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("soldCount") val soldCount: Int,
    @SerializedName("revenue") val revenue: Double
)

data class RecentOrderStat(
    @SerializedName("id") val id: String,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("status") val status: String,
    @SerializedName("total") val total: Double,
    @SerializedName("createdAt") val createdAt: String
)
