package com.example.foodapp.pages.shipper.history

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.shipper.home.ShipperOrderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredOrders = viewModel.getFilteredOrders()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedStatus == null,
                onClick = { viewModel.onStatusSelected(null) },
                label = { Text("Tất cả") }
            )
            FilterChip(
                selected = uiState.selectedStatus == "DELIVERED",
                onClick = { viewModel.onStatusSelected("DELIVERED") },
                label = { Text("Đã giao") }
            )
            FilterChip(
                selected = uiState.selectedStatus == "CANCELLED",
                onClick = { viewModel.onStatusSelected("CANCELLED") },
                label = { Text("Đã hủy") }
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có lịch sử đơn hàng", color = Color.Gray)
            }
        } else {
             LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredOrders) { order ->
                    ShipperOrderCard(
                        order = order,
                        onAccept = { }, // Cannot accept in history
                        onClick = { /* Navigate to detail if needed */ }
                    )
                }
            }
        }
    }
}
