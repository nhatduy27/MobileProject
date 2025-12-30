package com.example.foodapp.pages.owner.dashboard

import com.example.foodapp.data.model.owner.dashboard.DashboardDayRevenue
import com.example.foodapp.data.model.owner.dashboard.DashboardRecentOrder
import com.example.foodapp.data.model.owner.dashboard.DashboardStat
import com.example.foodapp.data.model.owner.dashboard.DashboardTopProduct

/**
 * UI state cho màn hình Dashboard.
 */
data class DashboardUiState(
    val stats: List<DashboardStat> = emptyList(),
    val weeklyRevenue: List<DashboardDayRevenue> = emptyList(),
    val recentOrders: List<DashboardRecentOrder> = emptyList(),
    val topProducts: List<DashboardTopProduct> = emptyList()
)
