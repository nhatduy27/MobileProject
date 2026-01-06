package com.example.foodapp.pages.shipper.history

import androidx.lifecycle.ViewModel
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.shipper.HistoryStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel : ViewModel() {

    // ✅ SỬ DỤNG DI - Lấy repository từ RepositoryProvider
    // Repository có thể là Mock hoặc Real, ViewModel không cần quan tâm
    private val repository = RepositoryProvider.getHistoryRepository()

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
