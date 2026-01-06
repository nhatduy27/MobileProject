package com.example.foodapp.pages.owner.revenue

import androidx.lifecycle.ViewModel
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RevenueViewModel : ViewModel() {

    // ✅ SỬ DỤNG DI - Lấy repository từ RepositoryProvider
    private val repository = RepositoryProvider.getRevenueRepository()

    private val _uiState = MutableStateFlow(RevenueUiState())
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    init {
        // Khởi tạo dữ liệu mặc định cho "Hôm nay"
        val defaultPeriod = "Hôm nay"
        val data = repository.getRevenueData(defaultPeriod)!!
        _uiState.value = RevenueUiState(
            selectedPeriod = defaultPeriod,
            periods = repository.getAvailablePeriods(),
            stats = data.stats,
            timeSlots = data.timeSlots,
            topProducts = data.topProducts
        )
    }

    fun onPeriodSelected(period: String) {
        val data = repository.getRevenueData(period)!!
        _uiState.update {
            it.copy(
                selectedPeriod = period,
                stats = data.stats,
                timeSlots = data.timeSlots,
                topProducts = data.topProducts
            )
        }
    }
}
