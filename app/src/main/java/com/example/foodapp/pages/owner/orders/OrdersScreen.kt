package com.example.foodapp.pages.owner.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
            "• Bún chả Hà Nội x2\n• Nước chanh x2", "10:05 AM", 150000, OrderStatus.PROCESSING),
        Order("#ORD10242", "Phạm Thị D", "KTX Khu C, Phòng 401",
            "• Cà ri gà x1\n• Bánh mì x2", "09:50 AM", 85000, OrderStatus.COMPLETED),
        Order("#ORD10241", "Hoàng Văn E", "KTX Khu D, Phòng 105",
            "• Pizza Pepperoni x1\n• Nước ngọt x2", "09:30 AM", 200000, OrderStatus.CANCELLED),
    )

    // Lọc đơn hàng dựa trên filter được chọn
    val filteredOrders = if (selectedFilter == "Tất cả") {
        orders
    } else {
        orders.filter { it.status.displayName == selectedFilter }
    }

    val totalOrders = orders.size
    val pendingOrders = orders.count { it.status == OrderStatus.PENDING }
    val processingOrders = orders.count { it.status == OrderStatus.PROCESSING }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders) { order ->
                    OrderCard(order)
                }
            }
        }

        OrdersFloatingButton()
    }
}
