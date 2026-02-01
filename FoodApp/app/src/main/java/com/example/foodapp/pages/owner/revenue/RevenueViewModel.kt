package com.example.foodapp.pages.owner.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.revenue.RevenuePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RevenueViewModel : ViewModel() {

    // Sử dụng Real Repository để kết nối API
    private val repository = RepositoryProvider.getRealRevenueRepository()

    private val _uiState = MutableStateFlow(RevenueUiState())
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    init {
        // Load dữ liệu mặc định cho "Hôm nay"
        loadRevenueData(RevenuePeriod.TODAY)
    }

    fun onPeriodSelected(period: RevenuePeriod) {
        loadRevenueData(period)
    }
    
    // Overload để hỗ trợ display key (từ UI cũ)
    fun onPeriodSelected(displayKey: String) {
        val period = RevenuePeriod.fromDisplayKey(displayKey)
        loadRevenueData(period)
    }
    
    private fun loadRevenueData(period: RevenuePeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, selectedPeriod = period) }
            
            repository.getRevenueAnalytics(period)
                .onSuccess { analytics ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            stats = analytics.stats,
                            timeSlots = analytics.timeSlots,
                            topProducts = analytics.topProducts
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Không thể tải dữ liệu doanh thu"
                        )
                    }
                }
        }
    }
    
    fun refresh() {
        loadRevenueData(_uiState.value.selectedPeriod)
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
