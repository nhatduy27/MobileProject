package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
fun RevenueScreen() {
    var selectedPeriod by remember { mutableStateOf("Hôm nay") }

    val revenueStats = listOf(
        RevenueStat("Doanh thu hôm nay", "1.25M", "↑ 12% so với hôm qua", Color(0xFFFF6B35)),
        RevenueStat("Số đơn hôm nay", "124", "↑ 8% so với hôm qua", Color(0xFF2196F3)),
        RevenueStat("Đơn trung bình", "101K", "↑ 5% so với hôm qua", Color(0xFF9C27B0)),
        RevenueStat("Doanh thu tháng", "38.5M", "11 ngày đầu tháng", Color(0xFFFF9800))
    )

    val timeSlots = listOf(
        TimeSlotRevenue("🌅", "Sáng (6:00 - 11:00)", 42, 35, "438K"),
        TimeSlotRevenue("☀️", "Trưa (11:00 - 14:00)", 52, 42, "525K"),
        TimeSlotRevenue("🌙", "Tối (17:00 - 21:00)", 30, 23, "287K")
    )

    val topProducts = listOf(
        TopProduct("🥇", "Cơm gà xối mỡ", 32, 45000, "1.44M"),
        TopProduct("🥈", "Phở bò", 28, 50000, "1.40M"),
        TopProduct("🥉", "Trà sữa trân châu", 45, 25000, "1.13M")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        RevenueHeader()

        // Period Filter
        PeriodFilter(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { selectedPeriod = it }
        )

        // Statistics Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            revenueStats.forEach { stat ->
                RevenueStatCard(stat = stat)
            }
        }

        // Chart Section
        ChartSection()

        // Details List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time Slots Section
            Text(
                text = "Chi tiết doanh thu theo khung giờ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            timeSlots.forEach { timeSlot ->
                TimeSlotCard(timeSlot = timeSlot)
            }

            // Top Products Section
            Text(
                text = "Sản phẩm bán chạy hôm nay",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(top = 8.dp)
            )

            topProducts.forEach { product ->
                TopProductCard(product = product)
            }
        }
    }
}
