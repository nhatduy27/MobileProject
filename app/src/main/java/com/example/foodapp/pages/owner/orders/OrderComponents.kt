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
fun OrdersHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF6B35))
            .padding(20.dp)
    ) {
        Text(
            text = "Quản lý đơn hàng",
            fontSize = 24.sp,
            color = Color.White
        )
        Text(
            text = "Theo dõi và xử lý đơn hàng",
            fontSize = 14.sp,
            color = Color(0xFFFFE5D9),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun OrdersFilterRow(filters: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(12.dp),
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
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OrderStatCard("Tổng đơn", total.toString(), Color(0xFFFF6B35), Modifier.weight(1f))
        OrderStatCard("Chờ xác nhận", pending.toString(), Color(0xFFFFA500), Modifier.weight(1f))
        OrderStatCard("Đang giao", delivering.toString(), Color(0xFF4CAF50), Modifier.weight(1f))
    }
}

@Composable
fun OrdersFloatingButton(modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = { /* TODO */ },
        modifier = modifier,
        containerColor = Color(0xFFFF6B35)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Thêm đơn hàng", tint = Color.White)
    }
}
