package com.example.foodapp.pages.shipper.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val orders: List<ShipperOrder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedStatus: String? = null // Filter: DELIVERED, CANCELLED
)

class HistoryViewModel : ViewModel() {
    private val repository = RepositoryProvider.getShipperOrderRepository()
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistory()
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Fetch all orders, then filter client side or API side.
            // API allows status filtering. But we want both delivered and cancelled?
            // "History" usually implies completed orders.
            // Api: filter by status. If null, returns all (including active).
            // We probably want to fetch list and filter locally or fetch "DELIVERED" by default.
            // Let's fetch all and filter for history tabs.
            
            repository.getMyOrders(null, 1, 100).onSuccess { response -> // Fetch more
                val allOrders = response.data
                // Filter only completed
                val completedOrders = allOrders.filter { 
                    it.status == "DELIVERED" || it.status == "CANCELLED" 
                }
                
                _uiState.update { it.copy(orders = completedOrders, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    
    fun onStatusSelected(status: String?) {
        _uiState.update { it.copy(selectedStatus = status) }
    }
    
    fun getFilteredOrders(): List<ShipperOrder> {
        val state = _uiState.value
        return if (state.selectedStatus != null) {
            state.orders.filter { it.status == state.selectedStatus }
        } else {
            state.orders
        }
    }
}
