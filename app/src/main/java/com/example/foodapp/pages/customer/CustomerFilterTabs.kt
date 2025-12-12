package com.example.foodapp.pages.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerFilterTabs() {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .background(Color.White)
            .padding(12.dp)
    ) {
        val tabs = listOf("Tất cả" to Color(0xFFFF6B35), "VIP" to Color(0xFFF0F0F0),
            "Thường xuyên" to Color(0xFFF0F0F0), "Mới" to Color(0xFFF0F0F0))

        tabs.forEach { (title, color) ->
            Text(
                text = title,
                fontSize = 14.sp,
                color = if (color == Color(0xFFFF6B35)) Color.White else Color(0xFF757575),
                modifier = Modifier
                    .background(color)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(end = 8.dp)
            )
        }
    }
}