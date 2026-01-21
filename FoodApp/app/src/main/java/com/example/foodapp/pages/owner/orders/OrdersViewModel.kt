package com.example.foodapp.pages.owner.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.order.ShopOrder
import com.example.foodapp.data.model.owner.order.ShopOrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdersViewModel : ViewModel() {

    private val repository = RepositoryProvider.getOrdersRepository()

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    /**
     * Load orders from API
     */
    fun loadOrders(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    error = null
                ) 
            }

            val result = repository.getOrders(
                status = _uiState.value.getApiStatusFilter(),
                page = 1,
                limit = 50
            )

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update { 
                        it.copy(
                            orders = paginated.orders,
                            currentPage = paginated.page,
                            totalPages = paginated.totalPages,
                            hasMorePages = paginated.page < paginated.totalPages,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message ?: "Không thể tải đơn hàng"
                        )
                    }
                }
            )
        }
    }

    /**
     * Refresh orders (pull-to-refresh)
     */
    fun refresh() {
        loadOrders(refresh = true)
    }

    /**
     * Filter change handler
     */
    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadOrders()
    }

    /**
     * Search query change handler
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Get filtered orders (client-side search)
     */
    fun getFilteredOrders(): List<ShopOrder> {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        
        if (query.isBlank()) return state.orders

        return state.orders.filter { order ->
            order.orderNumber.contains(query, ignoreCase = true) ||
            order.customer?.displayName?.contains(query, ignoreCase = true) == true ||
            order.customer?.phone?.contains(query, ignoreCase = true) == true ||
            order.deliveryAddress?.getDisplayAddress()?.contains(query, ignoreCase = true) == true
        }
    }

    // ==================== Order Detail ====================

    /**
     * Load order detail
     */
    fun loadOrderDetail(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            
            val result = repository.getOrderDetail(orderId)
            
            result.fold(
                onSuccess = { detail ->
                    _uiState.update { 
                        it.copy(
                            selectedOrder = detail,
                            showDetailSheet = true,
                            isActionLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            error = error.message,
                            isActionLoading = false
                        )
                    }
                }
            )
        }
    }

    fun dismissDetailSheet() {
        _uiState.update { 
            it.copy(
                showDetailSheet = false,
                selectedOrder = null
            )
        }
    }

    // ==================== Order Actions ====================

    /**
     * Confirm order (PENDING -> CONFIRMED)
     */
    fun confirmOrder(orderId: String) {
        performOrderAction(orderId) {
            repository.confirmOrder(orderId)
        }
    }

    /**
     * Mark order as preparing (CONFIRMED -> PREPARING)
     */
    fun markPreparing(orderId: String) {
        performOrderAction(orderId) {
            repository.markPreparing(orderId)
        }
    }

    /**
     * Mark order as ready (PREPARING -> READY)
     */
    fun markReady(orderId: String) {
        performOrderAction(orderId) {
            repository.markReady(orderId)
        }
    }

    /**
     * Show cancel dialog
     */
    fun showCancelDialog(orderId: String) {
        _uiState.update { 
            it.copy(
                showCancelDialog = true,
                cancelOrderId = orderId
            )
        }
    }

    fun dismissCancelDialog() {
        _uiState.update { 
            it.copy(
                showCancelDialog = false,
                cancelOrderId = null
            )
        }
    }

    /**
     * Cancel order with reason
     */
    fun cancelOrder(reason: String?) {
        val orderId = _uiState.value.cancelOrderId ?: return
        dismissCancelDialog()
        
        performOrderAction(orderId) {
            repository.cancelOrder(orderId, reason)
        }
    }

    /**
     * Generic order action handler
     */
    private fun performOrderAction(
        orderId: String,
        action: suspend () -> Result<ShopOrder>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            
            val result = action()
            
            result.fold(
                onSuccess = { updatedOrder ->
                    // Update the order in list
                    _uiState.update { state ->
                        state.copy(
                            orders = state.orders.map { 
                                if (it.id == orderId) updatedOrder else it 
                            },
                            isActionLoading = false,
                            // Update detail if viewing same order
                            selectedOrder = if (state.selectedOrder?.id == orderId) {
                                null // Close detail to force refresh
                            } else {
                                state.selectedOrder
                            },
                            showDetailSheet = if (state.selectedOrder?.id == orderId) {
                                false
                            } else {
                                state.showDetailSheet
                            }
                        )
                    }
                    // Reload to get fresh data
                    loadOrders()
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            error = error.message ?: "Không thể thực hiện thao tác",
                            isActionLoading = false
                        )
                    }
                }
            )
        }
    }

    // ==================== Statistics ====================

    fun getTotalOrders(): Int = _uiState.value.orders.size

    fun getPendingOrders(): Int = _uiState.value.orders.count { 
        it.status == ShopOrderStatus.PENDING 
    }

    fun getPreparingOrders(): Int = _uiState.value.orders.count { 
        it.status == ShopOrderStatus.PREPARING || it.status == ShopOrderStatus.CONFIRMED
    }

    fun getShippingOrders(): Int = _uiState.value.orders.count { 
        it.status == ShopOrderStatus.SHIPPING || it.status == ShopOrderStatus.READY
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
