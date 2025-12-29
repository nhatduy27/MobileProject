package com.example.foodapp.pages.owner.customer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerFilterTabs(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val tabs = listOf("Tất cả", "VIP", "Thường xuyên", "Mới")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // Nền tổng thể trắng sạch
            .horizontalScroll(scrollState)
            .padding(vertical = 12.dp, horizontal = 16.dp), // Padding thoáng hơn
        horizontalArrangement = Arrangement.spacedBy(10.dp) // Khoảng cách giữa các nút
    ) {
        tabs.forEach { title ->
            val isSelected = selectedFilter == title

            // Màu sắc (Có animation nhẹ)
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFFFF6B35) else Color.White,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "bgColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color(0xFF616161),
                label = "textColor"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFFFF6B35) else Color(0xFFE0E0E0),
                label = "borderColor"
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape) // Bo tròn hoàn toàn (hình viên thuốc)
                    .background(backgroundColor)
                    .border(
                        width = 1.dp,
                        color = borderColor, // Border chuyển màu mượt mà
                        shape = CircleShape
                    )
                    .clickable { onFilterSelected(title) }
                    .padding(horizontal = 24.dp, vertical = 10.dp) // Padding bên trong nút lớn hơn chút cho dễ bấm
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium, // Chữ đậm hơn
                    color = contentColor
                )
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun PreviewFilterTabs() {
    Column {
        CustomerFilterTabs(selectedFilter = "Tất cả", onFilterSelected = {})
        Spacer(modifier = Modifier.height(10.dp))
        CustomerFilterTabs(selectedFilter = "VIP", onFilterSelected = {})
    }
}