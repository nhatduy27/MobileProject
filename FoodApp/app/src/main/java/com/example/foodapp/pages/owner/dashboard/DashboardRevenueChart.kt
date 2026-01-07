package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.owner.DashboardDayRevenue

@Composable
fun DashboardRevenueChart(
    weeklyRevenue: List<DashboardDayRevenue>
) {
    val maxRevenue = weeklyRevenue.maxOfOrNull { it.amount } ?: 1
    val totalRevenue = weeklyRevenue.sumOf { it.amount }
    val avgRevenue = if (weeklyRevenue.isNotEmpty()) totalRevenue / weeklyRevenue.size else 0

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with title and summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Doanh thu 7 ngày",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tổng: ${totalRevenue}K | TB: ${avgRevenue}K",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chart area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                // Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyRevenue.forEach { revenue ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Amount label
                            if (revenue.amount > 0) {
                                Text(
                                    "${revenue.amount}K",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF757575),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            // Bar with gradient
                            val barHeight = if (maxRevenue > 0) {
                                (revenue.amount.toFloat() / maxRevenue * 120).dp
                            } else {
                                0.dp
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(barHeight.coerceAtLeast(4.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B35),
                                                Color(0xFFFF8C5A)
                                            )
                                        ),
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weeklyRevenue.forEach { revenue ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                revenue.day,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }
            }
        }
    }
}
