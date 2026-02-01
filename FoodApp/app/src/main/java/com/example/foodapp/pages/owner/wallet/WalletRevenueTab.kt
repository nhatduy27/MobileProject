package com.example.foodapp.pages.owner.wallet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodapp.R
import com.example.foodapp.data.model.owner.wallet.DailyRevenue
import com.example.foodapp.data.model.owner.wallet.RevenuePeriod
import com.example.foodapp.data.model.owner.wallet.RevenueStats

/**
 * Revenue Tab Content
 */
@Composable
fun WalletRevenueTab(
    revenueStats: RevenueStats?,
    selectedPeriod: RevenuePeriod,
    isLoading: Boolean,
    onPeriodSelected: (RevenuePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Period Filter
        PeriodFilterRow(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (revenueStats != null) {
            // Revenue Summary Cards
            RevenueSummarySection(revenueStats)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Revenue Chart
            if (revenueStats.dailyBreakdown.isNotEmpty()) {
                RevenueChartSection(
                    dailyRevenue = revenueStats.dailyBreakdown,
                    selectedPeriod = selectedPeriod
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Daily Breakdown List
            if (revenueStats.dailyBreakdown.isNotEmpty()) {
                DailyBreakdownSection(
                    dailyRevenue = revenueStats.dailyBreakdown.reversed().take(10)
                )
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.wallet_no_revenue_data),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Period filter row
 */
@Composable
fun PeriodFilterRow(
    selectedPeriod: RevenuePeriod,
    onPeriodSelected: (RevenuePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RevenuePeriod.values().forEach { period ->
            val periodLabel = when (period) {
                RevenuePeriod.TODAY -> stringResource(R.string.revenue_period_today)
                RevenuePeriod.WEEK -> stringResource(R.string.revenue_period_week)
                RevenuePeriod.MONTH -> stringResource(R.string.revenue_period_month)
                RevenuePeriod.YEAR -> stringResource(R.string.revenue_period_year)
                RevenuePeriod.ALL -> stringResource(R.string.revenue_period_all)
            }
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
                label = { Text(periodLabel) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * Revenue Summary Cards
 */
@Composable
fun RevenueSummarySection(
    revenueStats: RevenueStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RevenueSummaryCard(
                title = stringResource(R.string.revenue_period_today),
                value = formatCurrency(revenueStats.today),
                icon = Icons.Default.Today,
                iconTint = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            RevenueSummaryCard(
                title = stringResource(R.string.revenue_period_week),
                value = formatCurrency(revenueStats.week),
                icon = Icons.Default.DateRange,
                iconTint = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RevenueSummaryCard(
                title = stringResource(R.string.revenue_period_month),
                value = formatCurrency(revenueStats.month),
                icon = Icons.Default.CalendarMonth,
                iconTint = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            RevenueSummaryCard(
                title = stringResource(R.string.revenue_period_all),
                value = formatCurrency(revenueStats.all),
                icon = Icons.Default.AttachMoney,
                iconTint = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RevenueSummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

/**
 * Revenue Chart Section
 */
@Composable
fun RevenueChartSection(
    dailyRevenue: List<DailyRevenue>,
    selectedPeriod: RevenuePeriod,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.wallet_revenue_chart),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SimpleLineChart(
                data = dailyRevenue.map { it.amount.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

/**
 * Simple Line Chart
 */
@Composable
fun SimpleLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return
    
    val maxValue = data.maxOrNull() ?: 1f
    val minValue = 0f
    val range = maxValue - minValue
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1).coerceAtLeast(1)
        
        // Draw grid lines
        val gridColor = Color.Gray.copy(alpha = 0.2f)
        for (i in 0..4) {
            val y = height * i / 4f
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        }
        
        // Draw line chart
        if (data.size > 1) {
            val path = Path()
            data.forEachIndexed { index, value ->
                val x = index * stepX
                val normalizedValue = if (range > 0) (value - minValue) / range else 0.5f
                val y = height - (normalizedValue * height * 0.9f) - (height * 0.05f)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )
            
            // Draw points
            data.forEachIndexed { index, value ->
                val x = index * stepX
                val normalizedValue = if (range > 0) (value - minValue) / range else 0.5f
                val y = height - (normalizedValue * height * 0.9f) - (height * 0.05f)
                
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

/**
 * Daily Breakdown Section
 */
@Composable
fun DailyBreakdownSection(
    dailyRevenue: List<DailyRevenue>,
    modifier: Modifier = Modifier
) {
    val ordersUnit = stringResource(R.string.owner_orders_unit)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.wallet_revenue_daily),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            dailyRevenue.forEachIndexed { index, day ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatDate(day.date),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "${day.orderCount} $ordersUnit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = formatCurrency(day.amount),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (day.amount > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Format currency
 */
private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f", amount) + "đ"
}

/**
 * Format date from YYYY-MM-DD to DD/MM/YYYY
 */
private fun formatDate(date: String): String {
    return try {
        val parts = date.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else if (parts.size == 2) {
            // Month format: YYYY-MM
            "${parts[1]}/${parts[0]}"
        } else {
            date
        }
    } catch (e: Exception) {
        date
    }
}
