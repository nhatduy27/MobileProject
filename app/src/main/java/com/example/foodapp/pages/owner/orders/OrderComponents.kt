package com.example.foodapp.pages.owner.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrdersFilterRow(filters: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            OrderFilterChip(filter, selected == filter) { onSelect(filter) }
        }
    }
}

@Composable
fun OrdersStatsRow(total: Int, pending: Int, delivering: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OrderStatCard("Tổng đơn", total.toString(), Color(0xFFFF6B35))
        OrderStatCard("Chờ xác nhận", pending.toString(), Color(0xFFFFA500))
        OrderStatCard("Đang giao", delivering.toString(), Color(0xFF4CAF50))
    }
}
