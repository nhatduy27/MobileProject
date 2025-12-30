package com.example.foodapp.pages.owner.foods

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
fun FoodFilterTabs(
    categories: List<String> = listOf("Tất cả", "Cơm", "Phở/Bún", "Đồ uống", "Ăn vặt", "Tráng miệng"),
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // Nền trắng sạch
            .horizontalScroll(scrollState) // Cho phép cuộn ngang
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp) // Khoảng cách giữa các chip
    ) {
        categories.forEach { category ->
            FilterChip(
                category = category,
                isSelected = selectedFilter == category,
                onClick = { onFilterSelected(category) }
            )
        }
    }
}

@Composable
fun FilterChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animation màu sắc mượt mà (300ms)
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
            .clip(CircleShape) // Bo tròn hoàn toàn (Pill shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp) // Padding rộng rãi dễ bấm
    ) {
        Text(
            text = category,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor
        )
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun PreviewFoodFilterTabs() {
    Column {
        FoodFilterTabs(selectedFilter = "Tất cả", onFilterSelected = {})
        Spacer(modifier = Modifier.height(20.dp))
        FoodFilterTabs(selectedFilter = "Đồ uống", onFilterSelected = {})
    }
}