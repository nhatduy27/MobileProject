package com.example.foodapp.pages.owner.shippers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShippersScreen() {
    var selectedStatus by remember { mutableStateOf("Tất cả") }

    val shippers = listOf(
        Shipper("SH001", "Nguyễn Văn A", "0912345678", 4.8, 245, 8, ShipperStatus.DELIVERING),
        Shipper("SH002", "Trần Thị B", "0987654321", 4.9, 312, 12, ShipperStatus.AVAILABLE),
        Shipper("SH003", "Lê Văn C", "0901234567", 4.7, 198, 6, ShipperStatus.DELIVERING),
        Shipper("SH004", "Phạm Thị D", "0923456789", 4.6, 156, 0, ShipperStatus.OFFLINE),
        Shipper("SH005", "Hoàng Văn E", "0934567890", 4.9, 289, 10, ShipperStatus.AVAILABLE),
        Shipper("SH006", "Võ Thị F", "0945678901", 4.5, 134, 5, ShipperStatus.DELIVERING)
    )

    val totalShippers = shippers.size
    val activeShippers = shippers.count { it.status != ShipperStatus.OFFLINE }
    val todayDeliveries = shippers.sumOf { it.todayDeliveries }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        ShippersHeader()

        // Status Filter
        ShipperStatusFilter(
            selectedStatus = selectedStatus,
            onStatusSelected = { selectedStatus = it }
        )

        // Statistics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShipperStatCard(
                title = "Tổng shipper",
                value = totalShippers.toString(),
                color = Color(0xFFFF6B35),
                modifier = Modifier.weight(1f)
            )
            ShipperStatCard(
                title = "Đang hoạt động",
                value = activeShippers.toString(),
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            ShipperStatCard(
                title = "Đơn hôm nay",
                value = todayDeliveries.toString(),
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        // Shippers List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            shippers.forEach { shipper ->
                ShipperCard(shipper = shipper)
            }
        }
    }
}
