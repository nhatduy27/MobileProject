package com.example.foodapp.pages.owner.revenue

import com.example.foodapp.data.model.owner.RevenueStat
import com.example.foodapp.data.model.owner.TimeSlotRevenue
import com.example.foodapp.data.model.owner.TopProduct

/**
 * UI state cho màn hình doanh thu.
 */
data class RevenueUiState(
    val selectedPeriod: String = "Hôm nay",
    val periods: List<String> = emptyList(),
    val stats: List<RevenueStat> = emptyList(),
    val timeSlots: List<TimeSlotRevenue> = emptyList(),
    val topProducts: List<TopProduct> = emptyList()
)
