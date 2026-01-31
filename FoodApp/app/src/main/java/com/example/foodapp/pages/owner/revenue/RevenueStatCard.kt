package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.R
import com.example.foodapp.data.model.owner.revenue.KpiStat
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

/**
 * Maps Vietnamese stat titles from API to localized strings
 */
@Composable
fun getLocalizedStatTitle(apiTitle: String): String {
    return when {
        apiTitle.contains("Tổng doanh thu", ignoreCase = true) || 
            apiTitle.contains("Doanh thu", ignoreCase = true) -> stringResource(R.string.revenue_stat_total_revenue)
        apiTitle.contains("Số đơn", ignoreCase = true) || 
            apiTitle.contains("đơn hàng", ignoreCase = true) -> stringResource(R.string.revenue_stat_order_count)
        apiTitle.contains("Trung bình", ignoreCase = true) -> stringResource(R.string.revenue_stat_avg_order)
        apiTitle.contains("Đang xử lý", ignoreCase = true) || 
            apiTitle.contains("Chờ", ignoreCase = true) -> stringResource(R.string.revenue_stat_pending)
        apiTitle.contains("Hoàn thành", ignoreCase = true) -> stringResource(R.string.revenue_stat_completed)
        apiTitle.contains("Đã hủy", ignoreCase = true) || 
            apiTitle.contains("Hủy", ignoreCase = true) -> stringResource(R.string.revenue_stat_cancelled)
        apiTitle.contains("Tăng trưởng", ignoreCase = true) -> stringResource(R.string.revenue_stat_growth)
        else -> apiTitle // Fallback to original if no match
    }
}

/**
 * Maps Vietnamese stat subtitles from API to localized strings
 */
@Composable
fun getLocalizedStatSubtitle(apiSubtitle: String): String {
    return when {
        // No data cases
        apiSubtitle.contains("Không có doanh thu", ignoreCase = true) -> 
            stringResource(R.string.revenue_stat_no_revenue)
        apiSubtitle.contains("Không có đơn", ignoreCase = true) -> 
            stringResource(R.string.revenue_stat_no_orders)
        apiSubtitle.contains("Chưa có dữ liệu", ignoreCase = true) -> 
            stringResource(R.string.revenue_stat_no_data)
        
        // First X days/weeks cases
        apiSubtitle.contains("ngày đầu tháng", ignoreCase = true) -> {
            val days = extractNumber(apiSubtitle)
            stringResource(R.string.revenue_stat_first_days, days)
        }
        apiSubtitle.contains("tuần đầu năm", ignoreCase = true) -> {
            val weeks = extractNumber(apiSubtitle)
            stringResource(R.string.revenue_stat_first_weeks, weeks)
        }
        apiSubtitle.contains("Tháng đầu tiên", ignoreCase = true) -> 
            stringResource(R.string.revenue_stat_first_month)
        apiSubtitle.contains("Lần đầu tiên", ignoreCase = true) -> 
            stringResource(R.string.revenue_stat_first_time)
        
        // Period descriptions
        apiSubtitle.contains("Chỉ tính hôm nay", ignoreCase = true) || 
            apiSubtitle.equals("Hôm nay", ignoreCase = true) -> 
            stringResource(R.string.revenue_stat_today_only)
        apiSubtitle.equals("Tuần này", ignoreCase = true) -> 
            stringResource(R.string.revenue_stat_this_week)
        apiSubtitle.equals("Tháng này", ignoreCase = true) -> 
            stringResource(R.string.revenue_stat_this_month)
        apiSubtitle.equals("Năm nay", ignoreCase = true) -> 
            stringResource(R.string.revenue_stat_this_year)
        
        // Comparison cases
        apiSubtitle.contains("hôm qua", ignoreCase = true) -> {
            val percentage = extractPercentage(apiSubtitle)
            val comparison = stringResource(R.string.revenue_stat_vs_yesterday)
            formatComparisonText(apiSubtitle, percentage, comparison)
        }
        apiSubtitle.contains("tuần trước", ignoreCase = true) -> {
            val percentage = extractPercentage(apiSubtitle)
            val comparison = stringResource(R.string.revenue_stat_vs_last_week)
            formatComparisonText(apiSubtitle, percentage, comparison)
        }
        apiSubtitle.contains("tháng trước", ignoreCase = true) -> {
            val percentage = extractPercentage(apiSubtitle)
            val comparison = stringResource(R.string.revenue_stat_vs_last_month)
            formatComparisonText(apiSubtitle, percentage, comparison)
        }
        apiSubtitle.contains("năm trước", ignoreCase = true) -> {
            val percentage = extractPercentage(apiSubtitle)
            val comparison = stringResource(R.string.revenue_stat_vs_last_year)
            formatComparisonText(apiSubtitle, percentage, comparison)
        }
        else -> apiSubtitle // Fallback to original if no match
    }
}

private fun extractNumber(text: String): Int {
    val regex = Regex("(\\d+)")
    return regex.find(text)?.value?.toIntOrNull() ?: 0
}

private fun extractPercentage(text: String): String {
    val regex = Regex("(\\d+(?:\\.\\d+)?)")
    return regex.find(text)?.value ?: "0"
}

private fun formatComparisonText(original: String, percentage: String, comparison: String): String {
    val prefix = if (original.startsWith("↑")) "↑" else if (original.startsWith("↓")) "↓" else ""
    return "$prefix $percentage% $comparison"
}

@Composable
fun RevenueStatCard(stat: KpiStat) {
    val localizedTitle = getLocalizedStatTitle(stat.title)
    val localizedSubtitle = getLocalizedStatSubtitle(stat.subtitle)
    
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(130.dp),
        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = OwnerDimens.CardElevation.dp),
        shape = RoundedCornerShape(OwnerDimens.CardRadiusLarge.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(OwnerDimens.CardPadding.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = localizedTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = OwnerColors.TextSecondary,
                maxLines = 1
            )

            Column {
                Text(
                    text = stat.value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = stat.color,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = localizedSubtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (localizedSubtitle.startsWith("↑")) OwnerColors.Success else OwnerColors.TextTertiary,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

