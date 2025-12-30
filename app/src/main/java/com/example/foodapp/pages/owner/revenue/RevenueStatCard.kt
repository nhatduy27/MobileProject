package com.example.foodapp.pages.owner.revenue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.owner.RevenueStat

@Composable
fun RevenueStatCard(stat: RevenueStat) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stat.title,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )

            Text(
                text = stat.value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = stat.color,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = stat.subtitle,
                fontSize = 11.sp,
                color = if (stat.subtitle.startsWith("↑")) Color(0xFF4CAF50) else Color(0xFF999999),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
