package com.example.foodapp.pages.shipper.home

import androidx.lifecycle.ViewModel
import com.example.foodapp.data.repository.shipper.home.MockShipperHomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShipperHomeViewModel : ViewModel() {

    private val repository = MockShipperHomeRepository()

    private val _uiState = MutableStateFlow(ShipperHomeUiState())
    val uiState: StateFlow<ShipperHomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = ShipperHomeUiState(
            stats = repository.getStats(),
            tasks = repository.getTasks()
        )
    }
}
