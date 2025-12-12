package com.example.foodapp.pages.owner.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, backgroundColor = 0xFF00FF00)
@Composable
fun OrdersScreen() {
    var selectedFilter by remember { mutableStateOf("Tất cả") }

    val filters = listOf(
        "Tất cả",
        "Chờ xác nhận",
        "Đang chuẩn bị",
        "Đang giao",
        "Hoàn thành",
        "Đã hủy"
    )

    val orders = listOf(
        Order("#ORD10245", "Nguyễn Văn A", "KTX Khu A, Phòng 201",
            "• Cơm gà xối mỡ x2\n• Trà sữa trân châu x1", "10:25 AM", 125000, OrderStatus.DELIVERING),
        Order("#ORD10244", "Trần Thị B", "KTX Khu B, Phòng 305",
            "• Phở bò x1\n• Chả giò x3", "10:18 AM", 95000, OrderStatus.PENDING),
        Order("#ORD10243", "Lê Văn C", "KTX Khu A, Phòng 108",
            "• Bún chả Hà Nội x2\n• Nước chanh x2", "10:05 AM", 150000, OrderStatus.PROCESSING)
    )

    val totalOrders = orders.size
    val pendingOrders = orders.count { it.status == OrderStatus.PENDING }
    val deliveringOrders = orders.count { it.status == OrderStatus.DELIVERING }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            OrdersHeader()
            OrdersFilterRow(filters, selectedFilter) { selectedFilter = it }
            OrdersStatsRow(totalOrders, pendingOrders, deliveringOrders)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                orders.forEach { order ->
                    OrderCard(order)
                }
            }
        }

        OrdersFloatingButton()
    }
}
