package com.example.foodapp.pages.shipper.history

import com.example.foodapp.data.model.shipper.DeliveryHistory
import com.example.foodapp.data.model.shipper.HistoryStatus

// UI state cho màn Lịch sử giao hàng

data class HistoryUiState(
    val historyList: List<DeliveryHistory> = emptyList(),
    val selectedStatus: HistoryStatus? = null
)
