package com.example.foodapp.pages.shipper.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun ShipperHomeScreen(
    shipperName: String = "Nguyễn Văn A",
    shipperHomeViewModel: ShipperHomeViewModel = viewModel()
) {
    val uiState by shipperHomeViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        ShipperHomeHeader(shipperName = shipperName)

        val stats = uiState.stats

        if (stats != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    title = "Đơn hôm nay",
                    value = stats.todayOrders.toString(),
                    subtitle = "đơn",
                    color = Color(0xFF2196F3),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                )
                StatsCard(
                    title = "Thu nhập",
                    value = "${stats.todayEarnings / 1000}K",
                    subtitle = "hôm nay",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    title = "Hoàn thành",
                    value = "${stats.completionRate}%",
                    subtitle = "tỷ lệ",
                    color = Color(0xFFFF9800),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                )
                StatsCard(
                    title = "Đánh giá",
                    value = "⭐ ${stats.rating}",
                    subtitle = "trung bình",
                    color = Color(0xFFFF6B35),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                )
            }
        }

        Text(
            text = "Đơn hàng của bạn",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.tasks.forEach { task ->
                DeliveryTaskCard(task = task)
            }
        }
    }
}
