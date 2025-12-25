package com.example.foodapp.pages.owner.shippers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShippersHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF6B35))
            .padding(20.dp)
    ) {
        Text(
            text = "Quản lý Shipper",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Theo dõi và quản lý đội ngũ giao hàng",
            fontSize = 14.sp,
            color = Color(0xFFFFE5D9),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ShipperFilterChip(
    status: String,
    isSelected: Boolean,
    onClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFFF6B35) else Color(0xFFCCCCCC),
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = if (isSelected) Color(0xFFFF6B35) else Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick(status) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = status,
            color = if (isSelected) Color.White else Color(0xFF666666),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
