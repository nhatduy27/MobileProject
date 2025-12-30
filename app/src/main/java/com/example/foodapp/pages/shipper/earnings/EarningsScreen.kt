package com.example.foodapp.pages.shipper.earnings

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
import com.example.foodapp.data.model.shipper.EarningsPeriod
import com.example.foodapp.data.model.shipper.EarningsSummary

@Composable
fun EarningsScreen(
    earningsViewModel: EarningsViewModel = viewModel()
) {
    val uiState by earningsViewModel.uiState.collectAsState()

    // --- Phần logic tính toán không thay đổi ---
    val allEarningsHistory = uiState.allHistory

    val filteredEarningsHistory = when (uiState.selectedPeriod) {
        EarningsPeriod.TODAY -> allEarningsHistory.filter { it.date.contains("Hôm nay") || it.date.startsWith("25/12/2025") }
        EarningsPeriod.WEEK -> allEarningsHistory.take(7)
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

    // --- Bắt đầu phần thay đổi giao diện ---

    // 1. Tạo một Column lớn duy nhất, có thể cuộn cho toàn bộ màn hình
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState()) // Áp dụng cuộn ở đây
            .padding(bottom = 80.dp) // Thêm padding dưới để không bị BottomNavBar che
    ) {
        // 2. Đặt EarningsSummaryCard vào bên trong Column cuộn
        //    Thêm padding ngang cho nó
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            EarningsSummaryCard(summary)
        }

        // 3. Đặt PeriodFilterChips vào bên trong
        PeriodFilterChips(
            selectedPeriod = uiState.selectedPeriod,
            onPeriodSelected = { earningsViewModel.onPeriodSelected(it) }
        )

        // 4. Đặt Text vào bên trong
        Text(
            text = "Lịch sử thu nhập",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp) // Điều chỉnh padding
        )

        // 5. Đặt danh sách lịch sử vào bên trong, không cần cuộn riêng nữa
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Chỉ cần padding ngang
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filteredEarningsHistory.forEach { earnings ->
                EarningsHistoryCard(earnings)
            }
        }
    }
}
