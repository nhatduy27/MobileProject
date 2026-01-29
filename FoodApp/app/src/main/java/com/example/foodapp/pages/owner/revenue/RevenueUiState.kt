package com.example.foodapp.pages.owner.revenue

import com.example.foodapp.data.model.owner.revenue.KpiStat
import com.example.foodapp.data.model.owner.revenue.RevenuePeriod
import com.example.foodapp.data.model.owner.revenue.TimeSlotData
import com.example.foodapp.data.model.owner.revenue.TopProductData

/**
 * UI state cho màn hình doanh thu.
 */
data class RevenueUiState(
    val selectedPeriod: RevenuePeriod = RevenuePeriod.TODAY,
    val periods: List<RevenuePeriod> = RevenuePeriod.values().toList(),
    val stats: List<KpiStat> = emptyList(),
    val timeSlots: List<TimeSlotData> = emptyList(),
    val topProducts: List<TopProductData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
