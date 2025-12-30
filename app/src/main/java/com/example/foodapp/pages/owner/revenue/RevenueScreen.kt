package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RevenueScreen() {
    var selectedPeriod by remember { mutableStateOf("Hôm nay") }

    // Dữ liệu cho từng giai đoạn (tách ra ở RevenueData)
    val periodData = mapOf(
        "Hôm nay" to todayRevenueData,
        "Tuần này" to weekRevenueData,
        "Tháng này" to monthRevenueData,
        "Năm nay" to yearRevenueData
    )

    val periodRevenueData = periodData[selectedPeriod] ?: periodData["Hôm nay"]!!
    val revenueStats = periodRevenueData.stats
    val timeSlots = periodRevenueData.timeSlots
    val topProducts = periodRevenueData.topProducts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        RevenueHeader()

        // Bộ lọc thời gian (component riêng)
        PeriodFilter(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { selectedPeriod = it }
        )

        // Thống kê doanh thu (dùng RevenueStatCard component)
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

        // Biểu đồ (component riêng)
        ChartSection()

        // Danh sách chi tiết (dùng TimeSlotCard, TopProductCard component)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time Slots Section
            item {
                Text(
                    text = "Chi tiết doanh thu theo khung giờ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            items(timeSlots) { timeSlot ->
                TimeSlotCard(timeSlot = timeSlot)
            }

            // Top Products Section
            item {
                Text(
                    text = "Sản phẩm bán chạy ${
                        when (selectedPeriod) {
                            "Hôm nay" -> "hôm nay"
                            "Tuần này" -> "tuần này"
                            "Tháng này" -> "tháng này"
                            "Năm nay" -> "năm nay"
                            else -> "hôm nay"
                        }
                    }",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(topProducts) { product ->
                TopProductCard(product = product)
            }
        }
    }
}