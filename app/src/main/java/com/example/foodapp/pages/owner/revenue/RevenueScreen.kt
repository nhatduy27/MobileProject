package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    // Data for each period
    val periodData = mapOf(
        "Hôm nay" to PeriodRevenueData(
            stats = listOf(
                RevenueStat("Doanh thu hôm nay", "1.25M", "↑ 12% so với hôm qua", Color(0xFFFF6B35)),
                RevenueStat("Số đơn hôm nay", "124", "↑ 8% so với hôm qua", Color(0xFF2196F3)),
                RevenueStat("Đơn trung bình", "101K", "↑ 5% so với hôm qua", Color(0xFF9C27B0)),
                RevenueStat("Doanh thu tháng", "38.5M", "11 ngày đầu tháng", Color(0xFFFF9800))
            ),
            timeSlots = listOf(
                TimeSlotRevenue("🌅", "Sáng (6:00 - 11:00)", 42, 35, "438K"),
                TimeSlotRevenue("☀️", "Trưa (11:00 - 14:00)", 52, 42, "525K"),
                TimeSlotRevenue("🌙", "Tối (17:00 - 21:00)", 30, 23, "287K")
            ),
            topProducts = listOf(
                TopProduct("🥇", "Cơm gà xối mỡ", 32, 45000, "1.44M"),
                TopProduct("🥈", "Phở bò", 28, 50000, "1.40M"),
                TopProduct("🥉", "Trà sữa trân châu", 45, 25000, "1.13M")
            )
        ),
        "Tuần này" to PeriodRevenueData(
            stats = listOf(
                RevenueStat("Doanh thu tuần", "8.75M", "↑ 15% so với tuần trước", Color(0xFFFF6B35)),
                RevenueStat("Số đơn tuần", "856", "↑ 12% so với tuần trước", Color(0xFF2196F3)),
                RevenueStat("Đơn trung bình", "102K", "↑ 3% so với tuần trước", Color(0xFF9C27B0)),
                RevenueStat("Doanh thu tháng", "38.5M", "11 ngày đầu tháng", Color(0xFFFF9800))
            ),
            timeSlots = listOf(
                TimeSlotRevenue("🌅", "Sáng (6:00 - 11:00)", 48, 38, "3.2M"),
                TimeSlotRevenue("☀️", "Trưa (11:00 - 14:00)", 65, 45, "3.8M"),
                TimeSlotRevenue("🌙", "Tối (17:00 - 21:00)", 35, 24, "1.75M")
            ),
            topProducts = listOf(
                TopProduct("🥇", "Cơm gà xối mỡ", 120, 45000, "5.4M"),
                TopProduct("🥈", "Phở bò", 95, 50000, "4.75M"),
                TopProduct("🥉", "Trà sữa trân châu", 150, 25000, "3.75M")
            )
        ),
        "Tháng này" to PeriodRevenueData(
            stats = listOf(
                RevenueStat("Doanh thu tháng", "38.5M", "↑ 18% so với tháng trước", Color(0xFFFF6B35)),
                RevenueStat("Số đơn tháng", "3.8K", "↑ 14% so với tháng trước", Color(0xFF2196F3)),
                RevenueStat("Đơn trung bình", "101K", "↑ 3% so với tháng trước", Color(0xFF9C27B0)),
                RevenueStat("Doanh thu TB", "1.78M", "Doanh thu hàng ngày", Color(0xFFFF9800))
            ),
            timeSlots = listOf(
                TimeSlotRevenue("🌅", "Sáng (6:00 - 11:00)", 240, 40, "14.2M"),
                TimeSlotRevenue("☀️", "Trưa (11:00 - 14:00)", 312, 42, "16.8M"),
                TimeSlotRevenue("🌙", "Tối (17:00 - 21:00)", 168, 22, "7.5M")
            ),
            topProducts = listOf(
                TopProduct("🥇", "Cơm gà xối mỡ", 520, 45000, "23.4M"),
                TopProduct("🥈", "Phở bò", 420, 50000, "21M"),
                TopProduct("🥉", "Trà sữa trân châu", 680, 25000, "17M")
            )
        ),
        "Năm nay" to PeriodRevenueData(
            stats = listOf(
                RevenueStat("Doanh thu năm", "450M", "↑ 25% so với năm trước", Color(0xFFFF6B35)),
                RevenueStat("Số đơn năm", "48K", "↑ 20% so với năm trước", Color(0xFF2196F3)),
                RevenueStat("Đơn trung bình", "103K", "↑ 4% so với năm trước", Color(0xFF9C27B0)),
                RevenueStat("Doanh thu TB", "1.23M", "Doanh thu hàng ngày", Color(0xFFFF9800))
            ),
            timeSlots = listOf(
                TimeSlotRevenue("🌅", "Sáng (6:00 - 11:00)", 3200, 38, "168M"),
                TimeSlotRevenue("☀️", "Trưa (11:00 - 14:00)", 4100, 43, "198M"),
                TimeSlotRevenue("🌙", "Tối (17:00 - 21:00)", 2240, 23, "84M")
            ),
            topProducts = listOf(
                TopProduct("🥇", "Cơm gà xối mỡ", 6800, 45000, "306M"),
                TopProduct("🥈", "Phở bò", 5600, 50000, "280M"),
                TopProduct("🥉", "Trà sữa trân châu", 8900, 25000, "222.5M")
            )
        )
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
                        when(selectedPeriod) {
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
