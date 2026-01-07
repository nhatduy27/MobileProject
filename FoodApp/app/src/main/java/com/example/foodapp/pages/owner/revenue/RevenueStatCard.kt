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
            .width(170.dp)
            .height(130.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stat.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF757575),
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
                    text = stat.subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (stat.subtitle.startsWith("↑")) Color(0xFF4CAF50) else Color(0xFF999999),
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
