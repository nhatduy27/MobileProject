package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PeriodFilter(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    val periods = listOf("Hôm nay", "Tuần này", "Tháng này", "Năm nay")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        periods.forEach { period ->
            PeriodChip(
                text = period,
                isSelected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) }
            )
        }
    }
}

@Composable
fun PeriodChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        // Chỉ sử dụng onClick của Surface, đây là cách làm đúng
        onClick = onClick,
        color = if (isSelected) Color(0xFFFF6B35) else Color.White,
        shape = RoundedCornerShape(6.dp),
        // Modifier giờ đây chỉ còn nhiệm vụ vẽ border
        modifier = Modifier
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color(0xFFDDDDDD), // Dùng Transparent khi được chọn
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isSelected) Color.White else Color(0xFF757575),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}
