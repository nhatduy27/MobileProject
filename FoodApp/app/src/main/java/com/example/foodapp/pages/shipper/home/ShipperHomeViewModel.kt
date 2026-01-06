package com.example.foodapp.pages.shipper.home

import androidx.lifecycle.ViewModel
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShipperHomeViewModel : ViewModel() {

    // ✅ SỬ DỤNG DI - Lấy repository từ RepositoryProvider
    // Repository có thể là Mock hoặc Real, ViewModel không cần quan tâm
    private val repository = RepositoryProvider.getHomeRepository()

    private val _uiState = MutableStateFlow(ShipperHomeUiState())
    val uiState: StateFlow<ShipperHomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = ShipperHomeUiState(
            stats = repository.getStats(),
            tasks = repository.getTasks()
        )
    }
}
