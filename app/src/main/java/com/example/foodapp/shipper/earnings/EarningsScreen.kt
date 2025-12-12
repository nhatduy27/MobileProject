package com.example.foodapp.shipper.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EarningsScreen() {
    var selectedPeriod by remember { mutableStateOf(EarningsPeriod.MONTH) }

    val summary = EarningsSummary(
        todayEarnings = 85000,
        weekEarnings = 420000,
        monthEarnings = 1850000,
        totalOrders = 152,
        completedOrders = 148,
        averagePerOrder = 12500
    )

    val earningsHistory = listOf(
        EarningsData("Hôm nay, 12/12", 12, 85000, 10000),
        EarningsData("11/12/2024", 15, 102000, 0),
        EarningsData("10/12/2024", 18, 125000, 15000),
        EarningsData("09/12/2024", 14, 95000, 0),
        EarningsData("08/12/2024", 16, 110000, 10000),
        EarningsData("07/12/2024", 13, 88000, 0),
        EarningsData("06/12/2024", 17, 118000, 12000)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        EarningsSummaryCard(summary)

        PeriodFilterChips(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { selectedPeriod = it }
        )

        Text(
            text = "Lịch sử thu nhập",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            earningsHistory.forEach { earnings ->
                EarningsHistoryCard(earnings)
            }
        }
    }
}
