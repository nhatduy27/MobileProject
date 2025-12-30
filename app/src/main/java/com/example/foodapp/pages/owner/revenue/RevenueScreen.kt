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

    // Bắt đầu sửa đổi từ đây
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        RevenueHeader()

        // Bộ lọc thời gian (component riêng)
        PeriodFilter(
            selectedPeriod = uiState.selectedPeriod,
            onPeriodSelected = { revenueViewModel.onPeriodSelected(it) }
        )

        // Danh sách cuộn chung cho stats + chart + chi tiết
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth() // Chiếm toàn bộ chiều rộng
                .weight(1f) // <-- THÊM DÒNG NÀY: Để LazyColumn lấp đầy không gian còn lại
                .padding(horizontal = 16.dp), // Chỉ cần padding ngang ở đây
            contentPadding = PaddingValues(bottom = 80.dp), // Padding dưới để không bị thanh điều hướng che
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats + Chart ở đầu list, cuộn cùng các item khác
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Thống kê doanh thu (dùng RevenueStatCard component)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.stats.forEach { stat ->
                            RevenueStatCard(stat = stat)
                        }
                    }

                    // Biểu đồ (component riêng)
                    ChartSection()
                }
            }

            // Time Slots Section
            item {
                Text(
                    text = "Chi tiết doanh thu theo khung giờ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            items(uiState.timeSlots) { timeSlot ->
                TimeSlotCard(timeSlot = timeSlot)
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
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.topProducts) { product ->
                TopProductCard(product = product)
            }
        }
    }
}
