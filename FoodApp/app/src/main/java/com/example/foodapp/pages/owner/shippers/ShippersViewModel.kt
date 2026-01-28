package com.example.foodapp.pages.owner.shippers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.shipper.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình quản lý Shipper
 * Kết nối với Real API Backend
 */
class ShippersViewModel : ViewModel() {

    companion object {
        private const val TAG = "ShippersViewModel"
    }

    private val repository = RepositoryProvider.getOwnerShipperRepository()

    private val _uiState = MutableStateFlow(ShipperUiState())
    val uiState: StateFlow<ShipperUiState> = _uiState.asStateFlow()

    init {
        loadApplications()
        loadShippers()
    }

    // ==================== TAB SELECTION ====================

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        if (tabIndex == 0) {
            loadApplications(refresh = true)
        } else {
            loadShippers(refresh = true)
        }
    }

    // ==================== APPLICATIONS ====================

    fun loadApplications(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    errorMessage = null
                )
            }

            val status = _uiState.value.selectedApplicationStatus
            val result = repository.getApplications(status)

            result.onSuccess { applications ->
                Log.d(TAG, "✅ Loaded ${applications.size} applications")
                _uiState.update {
                    it.copy(
                        applications = applications,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to load applications", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Không thể tải đơn xin làm shipper: ${error.message}"
                    )
                }
            }
        }
    }

    fun onApplicationStatusFilterChanged(status: ApplicationStatus?) {
        _uiState.update { it.copy(selectedApplicationStatus = status) }
        loadApplications(refresh = true)
    }

    fun approveApplication(applicationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            val result = repository.approveApplication(applicationId)

            result.onSuccess { message ->
                Log.d(TAG, "✅ Approved: $message")
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = message
                    )
                }
                // Reload both lists
                loadApplications(refresh = true)
                loadShippers(refresh = true)
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to approve", error)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Không thể duyệt đơn: ${error.message}"
                    )
                }
            }
        }
    }

    fun rejectApplication(applicationId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            val result = repository.rejectApplication(applicationId, reason)

            result.onSuccess { message ->
                Log.d(TAG, "✅ Rejected: $message")
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = message
                    )
                }
                loadApplications(refresh = true)
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to reject", error)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Không thể từ chối đơn: ${error.message}"
                    )
                }
            }
        }
    }

    // ==================== ACTIVE SHIPPERS ====================

    fun loadShippers(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    errorMessage = null
                )
            }

            val result = repository.getShippers()

            result.onSuccess { shippers ->
                Log.d(TAG, "✅ Loaded ${shippers.size} shippers")
                _uiState.update {
                    it.copy(
                        shippers = shippers,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to load shippers", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Không thể tải danh sách shipper: ${error.message}"
                    )
                }
            }
        }
    }

    fun removeShipper(shipperId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            val result = repository.removeShipper(shipperId)

            result.onSuccess { message ->
                Log.d(TAG, "✅ Removed: $message")
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        successMessage = message,
                        // Remove from local list immediately
                        shippers = it.shippers.filter { s -> s.id != shipperId }
                    )
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Failed to remove shipper", error)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Không thể xóa shipper: ${error.message}"
                    )
                }
            }
        }
    }

    // ==================== SEARCH & FILTER ====================

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredApplications(): List<ShipperApplication> {
        val state = _uiState.value
        val query = state.searchQuery.trim()

        return if (query.isBlank()) {
            state.applications
        } else {
            state.applications.filter {
                it.userName.contains(query, ignoreCase = true) ||
                it.userPhone.contains(query, ignoreCase = true) ||
                it.vehicleNumber.contains(query, ignoreCase = true)
            }
        }
    }

    fun getFilteredShippers(): List<Shipper> {
        val state = _uiState.value
        val query = state.searchQuery.trim()

        return if (query.isBlank()) {
            state.shippers
        } else {
            state.shippers.filter {
                it.name.contains(query, ignoreCase = true) ||
                (it.phone?.contains(query, ignoreCase = true) == true) ||
                (it.shipperInfo?.vehicleNumber?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    // ==================== STATISTICS ====================

    fun getStats(): ShipperStats {
        val state = _uiState.value
        return ShipperStats(
            totalApplications = state.applications.size,
            pendingApplications = state.applications.count { it.status == ApplicationStatus.PENDING },
            totalShippers = state.shippers.size,
            availableShippers = state.shippers.count { it.shipperInfo?.status == ShipperStatus.AVAILABLE },
            busyShippers = state.shippers.count { it.shipperInfo?.status == ShipperStatus.BUSY }
        )
    }

    // ==================== UI HELPERS ====================

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun refreshCurrentTab() {
        if (_uiState.value.selectedTab == 0) {
            loadApplications(refresh = true)
        } else {
            loadShippers(refresh = true)
        }
    }
}
