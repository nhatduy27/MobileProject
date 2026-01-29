package com.example.foodapp.pages.owner.customer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Buyer/Customer.
 * Sử dụng Real Buyer Repository để kết nối API.
 */
class CustomerViewModel : ViewModel() {

    private val buyerRepository = RepositoryProvider.getBuyerRepository()

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null

    init {
        loadBuyers()
    }

    /**
     * Load danh sách buyers từ API
     */
    fun loadBuyers(resetPage: Boolean = true) {
        viewModelScope.launch {
            if (resetPage) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, currentPage = 1) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            }
            
            val state = _uiState.value
            val page = if (resetPage) 1 else state.currentPage
            
            buyerRepository.listBuyers(
                page = page,
                limit = 20,
                tier = state.selectedFilter.apiValue,
                search = state.searchQuery.takeIf { it.isNotBlank() },
                sort = "createdAt"
            ).onSuccess { result ->
                _uiState.update { currentState ->
                    currentState.copy(
                        buyers = if (resetPage) result.items else currentState.buyers + result.items,
                        currentPage = result.pagination.page,
                        totalPages = result.pagination.totalPages,
                        totalBuyers = result.pagination.total,
                        isLoading = false,
                        isLoadingMore = false
                    )
                }
            }.onFailure { error ->
                Log.e("CustomerVM", "Error loading buyers", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = error.message ?: "Không thể tải danh sách khách hàng"
                    )
                }
            }
        }
    }

    /**
     * Load thêm buyers (pagination)
     */
    fun loadMoreBuyers() {
        val state = _uiState.value
        if (!state.isLoadingMore && state.currentPage < state.totalPages) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
            loadBuyers(resetPage = false)
        }
    }

    /**
     * Xử lý khi filter thay đổi
     */
    fun onFilterChanged(filter: BuyerFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadBuyers()
    }
    
    // Overload để hỗ trợ display name (từ UI cũ)
    fun onFilterChanged(displayName: String) {
        val filter = BuyerFilter.fromDisplayName(displayName)
        onFilterChanged(filter)
    }

    /**
     * Xử lý search với debounce
     */
    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        
        // Cancel previous search job
        searchJob?.cancel()
        
        // Debounce search
        searchJob = viewModelScope.launch {
            delay(500) // Wait 500ms before searching
            loadBuyers()
        }
    }

    /**
     * Load buyer detail
     */
    fun loadBuyerDetail(customerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, errorMessage = null) }
            
            buyerRepository.getBuyerDetail(customerId)
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(
                            selectedBuyer = detail,
                            isLoadingDetail = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingDetail = false,
                            errorMessage = error.message ?: "Không thể tải thông tin khách hàng"
                        )
                    }
                }
        }
    }

    /**
     * Clear selected buyer
     */
    fun clearSelectedBuyer() {
        _uiState.update { it.copy(selectedBuyer = null) }
    }

    /**
     * Refresh danh sách
     */
    fun refresh() {
        loadBuyers()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
