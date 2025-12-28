package com.example.foodapp.pages.shipper.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryScreen() {
    var selectedStatus by remember { mutableStateOf<HistoryStatus?>(null) }

    val historyList = listOf(
        DeliveryHistory(
            "#ORD10247",
            "Phạm Thị D",
            "12/12/2024",
            "14:30",
            "KTX Food Store, Khu A",
            "KTX Khu C, Phòng 412",
            "2.1km",
            25000,
            HistoryStatus.COMPLETED,
            4.5
        ),
        DeliveryHistory(
            "#ORD10246",
            "Lê Văn C",
            "12/12/2024",
            "13:15",
            "KTX Food Store, Khu A",
            "KTX Khu A, Phòng 108",
            "0.5km",
            15000,
            HistoryStatus.COMPLETED,
            5.0
        ),
        DeliveryHistory(
            "#ORD10243",
            "Nguyễn Văn E",
            "11/12/2024",
            "19:20",
            "KTX Food Store, Khu A",
            "KTX Khu B, Phòng 201",
            "1.5km",
            20000,
            HistoryStatus.CANCELLED
        ),
        DeliveryHistory(
            "#ORD10240",
            "Trần Thị F",
            "11/12/2024",
            "12:45",
            "KTX Food Store, Khu A",
            "KTX Khu C, Phòng 310",
            "1.8km",
            22000,
            HistoryStatus.COMPLETED,
            4.8
        ),
        DeliveryHistory(
            "#ORD10235",
            "Hoàng Văn G",
            "10/12/2024",
            "18:00",
            "KTX Food Store, Khu A",
            "KTX Khu A, Phòng 505",
            "0.8km",
            15000,
            HistoryStatus.COMPLETED,
            4.2
        )
    )

    val filteredHistory = if (selectedStatus != null) {
        historyList.filter { it.status == selectedStatus }
    } else {
        historyList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        StatusFilterChips(
            selectedStatus = selectedStatus,
            onStatusSelected = { selectedStatus = it }
        )

        Text(
            text = "${filteredHistory.size} đơn hàng",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            filteredHistory.forEach { history ->
                DeliveryHistoryCard(history)
            }
        }
    }
}
