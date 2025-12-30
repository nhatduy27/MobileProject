package com.example.foodapp.pages.owner.dashboard

import com.example.foodapp.data.model.owner.DashboardDayRevenue
import com.example.foodapp.data.model.owner.DashboardRecentOrder
import com.example.foodapp.data.model.owner.DashboardStat
import com.example.foodapp.data.model.owner.DashboardTopProduct

/**
 * UI state cho màn hình Dashboard.
 */
data class DashboardUiState(
    val stats: List<DashboardStat> = emptyList(),
    val weeklyRevenue: List<DashboardDayRevenue> = emptyList(),
    val recentOrders: List<DashboardRecentOrder> = emptyList(),
    val topProducts: List<DashboardTopProduct> = emptyList()
)
