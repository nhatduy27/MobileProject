package com.example.foodapp.pages.shipper.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShipperHomeViewModel : ViewModel() {
    private val repository = RepositoryProvider.getShipperOrderRepository()
    
    private val _uiState = MutableStateFlow(ShipperHomeUiState())
    val uiState: StateFlow<ShipperHomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        loadAvailableOrders()
        loadMyOrders()
    }
    
    fun loadAvailableOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAvailable = true, error = null) }
            try {
                val result = repository.getAvailableOrders(1, 50)
                result.onSuccess { response ->
                    _uiState.update { 
                        it.copy(
                            availableOrders = response.data, 
                            isLoadingAvailable = false,
                            isNotAssignedToShop = false
                        ) 
                    }
                }.onFailure { e ->
                    handleError(e)
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    fun loadMyOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMyOrders = true, error = null) }
            try {
                val result = repository.getMyOrders(null, 1, 50)
                result.onSuccess { response ->
                    // Filter active orders only
                    val activeOrders = response.data.filter { 
                        it.status != "DELIVERED" && it.status != "CANCELLED" 
                    }
                    _uiState.update { 
                        it.copy(
                            myOrders = activeOrders, 
                            isLoadingMyOrders = false,
                            isNotAssignedToShop = false
                        ) 
                    }
                }.onFailure { e ->
                    handleError(e)
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    
    private fun handleError(e: Throwable) {
        val errorMessage = e.message ?: "Unknown error"
        
        // Detect "not assigned to shop" error
        val isNotAssigned = errorMessage.contains("not assigned", ignoreCase = true) ||
                errorMessage.contains("SHIPPER_NOT_ASSIGNED", ignoreCase = true) ||
                errorMessage.contains("not been assigned", ignoreCase = true)
        
        _uiState.update { 
            it.copy(
                error = if (isNotAssigned) null else errorMessage,
                isLoadingAvailable = false,
                isLoadingMyOrders = false,
                isNotAssignedToShop = isNotAssigned
            ) 
        }
        
        Log.e("ShipperHomeVM", "Error loading orders: $errorMessage, isNotAssigned: $isNotAssigned")
    }
    
    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        if (index == 0) loadAvailableOrders() else loadMyOrders()
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            repository.acceptOrder(orderId).onSuccess {
                loadAvailableOrders()
                loadMyOrders()
            }.onFailure {
                _uiState.update { state -> state.copy(error = it.message) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
