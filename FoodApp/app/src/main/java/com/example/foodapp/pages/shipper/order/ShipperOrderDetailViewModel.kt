package com.example.foodapp.pages.shipper.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val order: ShipperOrder? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ShipperOrderDetailViewModel : ViewModel() {
    private val repository = RepositoryProvider.getShipperOrderRepository()
    
    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()
    
    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getOrderDetail(orderId)
                .onSuccess { order ->
                    _uiState.update { it.copy(order = order, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
    
    fun acceptOrder(orderId: String) {
        performAction(orderId) { repository.acceptOrder(it) }
    }
    
    fun markShipping(orderId: String) {
        performAction(orderId) { repository.markShipping(it) }
    }
    
    fun markDelivered(orderId: String) {
        performAction(orderId) { repository.markDelivered(it) }
    }
    
    private fun performAction(orderId: String, action: suspend (String) -> Result<ShipperOrder>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            action(orderId)
                .onSuccess { order ->
                    _uiState.update { it.copy(order = order, isLoading = false, successMessage = "Thao tác thành công") }
                }
                .onFailure { e ->
                     _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}
