package com.example.foodapp.pages.owner.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    // Lấy repository từ RepositoryProvider
    private val repository = RepositoryProvider.getDashboardRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.getShopAnalytics() // Default: query all/current period logic
            
            result.onSuccess { data ->
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        data = data
                    ) 
                }
            }.onFailure { error ->
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
