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

@Composable
fun DashboardRevenueChart() {
    val revenueData = listOf(
        DayRevenue("T2", 1250),
        DayRevenue("T3", 1870),
        DayRevenue("T4", 1560),
        DayRevenue("T5", 2150),
        DayRevenue("T6", 1890),
        DayRevenue("T7", 2380),
        DayRevenue("CN", 2050)
    )
    
    val maxRevenue = revenueData.maxOf { it.amount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Doanh thu 7 ngày gần nhất", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                revenueData.forEach { revenue ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Label
                        Text(
                            revenue.day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        Text(
                            "${revenue.amount}K",
                            fontSize = 10.sp,
                            color = Color(0xFF757575)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((revenue.amount.toFloat() / maxRevenue * 200).dp)
                                .background(Color(0xFFFF6B35), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
    }
}

data class DayRevenue(val day: String, val amount: Int)
