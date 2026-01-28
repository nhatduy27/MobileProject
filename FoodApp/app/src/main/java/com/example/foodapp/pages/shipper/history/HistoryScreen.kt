package com.example.foodapp.pages.shipper.history

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.shipper.home.ShipperOrderCard
import com.example.foodapp.pages.shipper.theme.ShipperColors

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
            .background(ShipperColors.Background)
    ) {
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ShipperColors.Surface)
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedStatus == null,
                onClick = { viewModel.onStatusSelected(null) },
                label = { Text("Tất cả") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ShipperColors.PrimaryLight,
                    selectedLabelColor = ShipperColors.Primary
                )
            )
            FilterChip(
                selected = uiState.selectedStatus == "DELIVERED",
                onClick = { viewModel.onStatusSelected("DELIVERED") },
                label = { Text("Đã giao") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ShipperColors.SuccessLight,
                    selectedLabelColor = ShipperColors.Success
                )
            )
            FilterChip(
                selected = uiState.selectedStatus == "CANCELLED",
                onClick = { viewModel.onStatusSelected("CANCELLED") },
                label = { Text("Đã hủy") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ShipperColors.ErrorLight,
                    selectedLabelColor = ShipperColors.Error
                )
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ShipperColors.Primary)
            }
        } else if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = ShipperColors.TextTertiary
                    )
                    Text("Không có lịch sử đơn hàng", color = ShipperColors.TextSecondary)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredOrders) { order ->
                    ShipperOrderCard(
                        order = order,
                        onAccept = { },
                        onClick = { },
                        showAcceptButton = false
                    )
                }
            }
        }
    }
}
