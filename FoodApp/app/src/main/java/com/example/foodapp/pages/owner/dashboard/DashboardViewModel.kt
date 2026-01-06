package com.example.foodapp.pages.owner.dashboard

import androidx.lifecycle.ViewModel
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel : ViewModel() {

    // ✅ SỬ DỤNG DI - Lấy repository từ RepositoryProvider
    private val repository = RepositoryProvider.getDashboardRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Load dữ liệu mock cho màn dashboard
        _uiState.value = DashboardUiState(
            stats = repository.getStats(),
            weeklyRevenue = repository.getWeeklyRevenue(),
            recentOrders = repository.getRecentOrders(),
            topProducts = repository.getTopProducts()
        )
    }
}
