package com.example.foodapp.pages.owner.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    companion object {
        private const val TAG = "DashboardVM"
    }

    // Lấy repository từ RepositoryProvider
    private val repository = RepositoryProvider.getDashboardRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            Log.d(TAG, "Loading dashboard data...")
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getShopAnalytics() // Default: query all/current period logic
            
            result.onSuccess { data ->
                Log.d(TAG, "Data loaded successfully!")
                Log.d(TAG, "All-time revenue: ${data.allTime.revenue}")
                Log.d(TAG, "All-time orderCount: ${data.allTime.orderCount}")
                Log.d(TAG, "Recent orders: ${data.recentOrders.size}")
                data.recentOrders.firstOrNull()?.let {
                    Log.d(TAG, "First order: ${it.orderNumber}, total: ${it.total}")
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        data = data
                    ) 
                }
            }.onFailure { error ->
                Log.e(TAG, "Error loading data: ${error.message}", error)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = error.message ?: "Lỗi tải dữ liệu Dashboard"
                    ) 
                }
            }
        }
    }
}

