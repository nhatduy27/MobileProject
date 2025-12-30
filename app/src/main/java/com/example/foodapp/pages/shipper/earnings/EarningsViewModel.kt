package com.example.foodapp.pages.shipper.earnings

import androidx.lifecycle.ViewModel
import com.example.foodapp.data.model.shipper.EarningsPeriod
import com.example.foodapp.data.repository.shipper.earnings.MockShipperEarningsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EarningsViewModel : ViewModel() {

    private val repository = MockShipperEarningsRepository()

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
