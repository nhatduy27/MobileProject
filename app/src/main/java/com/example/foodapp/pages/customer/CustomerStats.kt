package com.example.foodapp.pages.customer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StatCardData(val label: String, val value: String, val color: Color)

@Composable
fun CustomerStats() {
    val scrollState = rememberScrollState()
    val stats = listOf(
        StatCardData("Tổng số", "248", Color(0xFFFF6B35)),
        StatCardData("VIP", "28", Color(0xFFFFD700)),
        StatCardData("Thường xuyên", "156", Color(0xFF4CAF50)),
        StatCardData("Mới", "64", Color(0xFF2196F3))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { stat ->
            StatCard(stat)
        }
    }
}

@Composable
fun StatCard(stat: StatCardData) {
    Card(
        modifier = Modifier.size(width = 140.dp, height = 100.dp),
        colors = CardDefaults.cardColors(stat.color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stat.label, fontSize = 13.sp, color = Color(0xFF757575))
            Text(
                text = stat.value,
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = stat.color,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}