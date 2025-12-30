package com.example.foodapp.data.model.owner

import androidx.compose.ui.graphics.Color

data class RevenueStat(
    val title: String,
    val value: String,
    val subtitle: String,
    val color: Color
)

data class TimeSlotRevenue(
    val emoji: String,
    val title: String,
    val ordersCount: Int,
    val percentage: Int,
    val amount: String
)

data class TopProduct(
    val rank: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Int,
    val totalRevenue: String
)

data class PeriodRevenueData(
    val stats: List<RevenueStat>,
    val timeSlots: List<TimeSlotRevenue>,
    val topProducts: List<TopProduct>
)
