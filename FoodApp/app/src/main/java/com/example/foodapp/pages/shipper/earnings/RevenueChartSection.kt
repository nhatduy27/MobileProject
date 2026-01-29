package com.example.foodapp.pages.shipper.earnings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shipper.wallet.DailyRevenue
import com.example.foodapp.pages.shipper.theme.ShipperColors

/**
 * Hiển thị biểu đồ doanh thu theo ngày
 */
@Composable
fun RevenueChartSection(
    dailyBreakdown: List<DailyRevenue>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Biểu đồ doanh thu",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShipperColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ShipperColors.Primary,
                        strokeWidth = 2.dp
                    )
                }
            } else if (dailyBreakdown.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có dữ liệu",
                        color = ShipperColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Simple bar chart
                RevenueBarChart(
                    data = dailyBreakdown.takeLast(7), // Hiển thị 7 ngày gần nhất
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ShipperColors.Primary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Doanh thu (đ)",
                        fontSize = 12.sp,
                        color = ShipperColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RevenueBarChart(
    data: List<DailyRevenue>,
    modifier: Modifier = Modifier
) {
    val maxAmount = remember(data) { 
        data.maxOfOrNull { it.amount }?.coerceAtLeast(1000) ?: 1000 
    }
    
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            BarChartItem(
                label = formatDateLabel(item.date),
                value = item.amount,
                maxValue = maxAmount,
                orderCount = item.orderCount
            )
        }
    }
}

@Composable
private fun BarChartItem(
    label: String,
    value: Long,
    maxValue: Long,
    orderCount: Int
) {
    val heightFraction = if (maxValue > 0) (value.toFloat() / maxValue.toFloat()).coerceIn(0.05f, 1f) else 0.05f
    val barMaxHeight = 100.dp
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(42.dp)
    ) {
        // Value on top
        if (value > 0) {
            Text(
                text = formatShortAmount(value),
                fontSize = 10.sp,
                color = ShipperColors.TextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Bar
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(barMaxHeight * heightFraction)
                .background(
                    color = if (value > 0) ShipperColors.Primary else ShipperColors.BorderLight,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Label (date)
        Text(
            text = label,
            fontSize = 10.sp,
            color = ShipperColors.TextSecondary,
            maxLines = 1
        )
        
        // Order count
        if (orderCount > 0) {
            Text(
                text = "${orderCount}đ",
                fontSize = 9.sp,
                color = ShipperColors.TextTertiary
            )
        }
    }
}

/**
 * Format date from YYYY-MM-DD to DD/MM
 */
private fun formatDateLabel(date: String): String {
    return try {
        val parts = date.split("-")
        if (parts.size >= 3) {
            "${parts[2]}/${parts[1]}"
        } else if (parts.size == 2) {
            // Monthly format YYYY-MM
            "T${parts[1]}"
        } else {
            date.takeLast(5)
        }
    } catch (e: Exception) {
        date.takeLast(5)
    }
}

/**
 * Format amount to short form (e.g., 150K, 1.2M)
 */
private fun formatShortAmount(amount: Long): String {
    return when {
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000.0)
        amount >= 1_000 -> String.format("%.0fK", amount / 1_000.0)
        else -> amount.toString()
    }
}
