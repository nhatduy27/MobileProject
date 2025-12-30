package com.example.foodapp.pages.shipper.history

import androidx.lifecycle.ViewModel
import com.example.foodapp.data.model.shipper.HistoryStatus
import com.example.foodapp.data.repository.shipper.history.MockShipperHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel : ViewModel() {

    private val repository = MockShipperHistoryRepository()

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = HistoryUiState(
            historyList = repository.getHistoryList(),
            selectedStatus = null
        )
    }

    fun onStatusSelected(status: HistoryStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
    }
}
