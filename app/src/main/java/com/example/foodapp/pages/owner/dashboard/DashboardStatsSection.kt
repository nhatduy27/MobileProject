package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DashboardStatsSection() {
    Column {

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                DashboardStatCard(
                    icon = android.R.drawable.ic_menu_sort_by_size,
                    value = "124",
                    label = "Tổng đơn hôm nay",
                    color = Color(0xFF2196F3)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                DashboardStatCard(
                    icon = android.R.drawable.ic_dialog_email,
                    value = "1.250.000đ",
                    label = "Doanh thu hôm nay",
                    color = Color(0xFF4CAF50)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                DashboardStatCard(
                    icon = android.R.drawable.ic_menu_directions,
                    value = "8",
                    label = "Đơn đang giao",
                    color = Color(0xFFFF9800)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                DashboardStatCard(
                    icon = android.R.drawable.btn_star_big_on,
                    value = "Cơm gà xối mỡ",
                    label = "Món bán chạy nhất",
                    color = Color(0xFFFFC107)
                )
            }
        }
    }
}
