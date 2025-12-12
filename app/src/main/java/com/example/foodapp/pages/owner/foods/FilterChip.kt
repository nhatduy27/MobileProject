package com.example.foodapp.pages.owner.foods

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding

@Composable
fun FilterChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFFFF6B35) else Color(0xFFF0F0F0),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = category,
            fontSize = 14.sp,
            color = if (isSelected) Color.White else Color(0xFF757575),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}
