package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RevenueScreen(
    revenueViewModel: RevenueViewModel = viewModel()
) {
    val uiState by revenueViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Period Filter
        PeriodFilter(
            selectedPeriod = uiState.selectedPeriod,
            onPeriodSelected = { revenueViewModel.onPeriodSelected(it) }
        )

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.stats.forEach { stat ->
                        RevenueStatCard(stat = stat)
                    }
                }
            }

            // Chart
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ChartSection()
                }
            }

            // Time Slots Section
            item {
                Text(
                    text = "Chi tiết doanh thu theo khung giờ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items(uiState.timeSlots) { timeSlot ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TimeSlotCard(timeSlot = timeSlot)
                }
            }

            // Top Products Section
            item {
                Text(
                    text = "Sản phẩm bán chạy ${
                        when (uiState.selectedPeriod) {
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
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                )
            }

            items(uiState.topProducts) { product ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TopProductCard(product = product)
                }
            }
        }
    }
}
