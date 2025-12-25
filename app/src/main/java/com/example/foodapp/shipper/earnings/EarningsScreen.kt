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

    // Dữ liệu mock nhiều ngày, nhiều tháng, nhiều tuần
    val allEarningsHistory = listOf(
        EarningsData("Hôm nay, 25/12/2025", 10, 90000, 5000),
        EarningsData("24/12/2025", 12, 85000, 10000),
        EarningsData("23/12/2025", 15, 102000, 0),
        EarningsData("22/12/2025", 18, 125000, 15000),
        EarningsData("21/12/2025", 14, 95000, 0),
        EarningsData("20/12/2025", 16, 110000, 10000),
        EarningsData("19/12/2025", 13, 88000, 0),
        EarningsData("18/12/2025", 17, 118000, 12000),
        EarningsData("10/12/2025", 11, 80000, 0),
        EarningsData("01/12/2025", 9, 70000, 0),
        EarningsData("25/11/2025", 8, 60000, 0),
        EarningsData("10/11/2025", 7, 50000, 0)
    )

    // Lọc dữ liệu theo chế độ
    val filteredEarningsHistory = when (selectedPeriod) {
        EarningsPeriod.TODAY -> allEarningsHistory.filter { it.date.contains("Hôm nay") || it.date.startsWith("25/12/2025") }
        EarningsPeriod.WEEK -> allEarningsHistory.take(7) // 7 ngày gần nhất
        EarningsPeriod.MONTH -> allEarningsHistory.filter { it.date.endsWith("12/2025") || it.date.contains("Hôm nay") }
        EarningsPeriod.ALL -> allEarningsHistory
    }

    val summary = EarningsSummary(
        todayEarnings = filteredEarningsHistory.firstOrNull()?.totalEarnings ?: 0,
        weekEarnings = allEarningsHistory.take(7).sumOf { it.totalEarnings },
        monthEarnings = allEarningsHistory.filter { it.date.endsWith("12/2025") || it.date.contains("Hôm nay") }.sumOf { it.totalEarnings },
        totalOrders = allEarningsHistory.sumOf { it.totalOrders },
        completedOrders = allEarningsHistory.sumOf { it.totalOrders },
        averagePerOrder = if (allEarningsHistory.sumOf { it.totalOrders } > 0) allEarningsHistory.sumOf { it.totalEarnings } / allEarningsHistory.sumOf { it.totalOrders } else 0
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
            filteredEarningsHistory.forEach { earnings ->
                EarningsHistoryCard(earnings)
            }
        }
    }
}
