package com.example.foodapp.pages.owner.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.owner.DashboardDayRevenue

@Composable
fun DashboardRevenueChart(
    weeklyRevenue: List<DashboardDayRevenue>
) {
    val maxRevenue = weeklyRevenue.maxOfOrNull { it.amount } ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Doanh thu 7 ngày gần nhất", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp)
            ) {
                // Chart area with bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyRevenue.forEach { revenue ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            // Amount at top
                            Text(
                                "${revenue.amount}K",
                                fontSize = 9.sp,
                                color = Color(0xFF757575)
                            )

                            // Spacer to push bar down
                            Spacer(modifier = Modifier.weight(1f))

                            // Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((revenue.amount.toFloat() / maxRevenue * 120).dp)
                                    .background(Color(0xFFFF6B35), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }

                // Labels row at bottom (aligned)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    weeklyRevenue.forEach { revenue ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                revenue.day,
                                fontSize = 11.sp,
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

