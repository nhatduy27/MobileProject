package com.example.foodapp.pages.shipper.earnings

import androidx.lifecycle.ViewModel
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.shipper.EarningsPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EarningsViewModel : ViewModel() {

    // ✅ SỬ DỤNG DI - Lấy repository từ RepositoryProvider
    // Repository có thể là Mock hoặc Real, ViewModel không cần quan tâm
    private val repository = RepositoryProvider.getEarningsRepository()

    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState: StateFlow<EarningsUiState> = _uiState.asStateFlow()

    init {
        val all = repository.getAllEarningsHistory()
        _uiState.value = EarningsUiState(
            selectedPeriod = EarningsPeriod.MONTH,
            allHistory = all
        )
    }

    fun onPeriodSelected(period: EarningsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }
}
