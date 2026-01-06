package com.example.foodapp.pages.owner.shippers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.Shipper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShippersViewModel : ViewModel() {

    // ✅ SỬ DỤNG DI - Lấy repository từ RepositoryProvider
    private val repository = RepositoryProvider.getOwnerShipperRepository()

    private val _uiState = MutableStateFlow(ShipperUiState())
    val uiState: StateFlow<ShipperUiState> = _uiState.asStateFlow()

    init {
        loadShippers()
    }

    private fun loadShippers() {
        viewModelScope.launch {
            repository.getShippers().collect { list ->
                _uiState.update { current ->
                    current.copy(shippers = list)
                }
            }
        }
    }

    fun onStatusSelected(status: String) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredShippers(): List<Shipper> {
        val state = _uiState.value
        val filteredByStatus = if (state.selectedStatus == "Tất cả") {
            state.shippers
        } else {
            state.shippers.filter { it.status.displayName == state.selectedStatus }
        }

        val query = state.searchQuery.trim()
        return if (query.isBlank()) {
            filteredByStatus
        } else {
            filteredByStatus.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.phone.contains(query, ignoreCase = true)
            }
        }
    }

    fun getTotalShippers(): Int = _uiState.value.shippers.size

    fun getActiveShippers(): Int = _uiState.value.shippers.count { it.status != com.example.foodapp.data.model.owner.ShipperStatus.OFFLINE }

    fun getTodayDeliveries(): Int = _uiState.value.shippers.sumOf { it.todayDeliveries }

    fun addOrUpdateShipper(shipper: Shipper) {
        val exists = _uiState.value.shippers.any { it.id == shipper.id }
        if (exists) repository.updateShipper(shipper) else repository.addShipper(shipper)
    }

    fun deleteShipper(id: String) {
        repository.deleteShipper(id)
    }
}
