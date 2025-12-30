package com.example.foodapp.pages.shipper.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = viewModel()
) {
    val uiState by historyViewModel.uiState.collectAsState()

    val filteredHistory = if (uiState.selectedStatus != null) {
        uiState.historyList.filter { it.status == uiState.selectedStatus }
    } else {
        uiState.historyList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        StatusFilterChips(
            selectedStatus = uiState.selectedStatus,
            onStatusSelected = { historyViewModel.onStatusSelected(it) }
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

