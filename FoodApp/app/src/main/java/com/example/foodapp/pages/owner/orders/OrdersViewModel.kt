package com.example.foodapp.pages.owner.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.Order
import com.example.foodapp.data.model.owner.OrderStatus
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

    private fun loadOrders() {
        viewModelScope.launch {
            repository.getOrders().collect { list ->
                _uiState.update { current -> current.copy(orders = list) }
            }
        }
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredOrders(): List<Order> {
        val state = _uiState.value

        val byStatus = if (state.selectedFilter == "Tất cả") {
            state.orders
        } else {
            state.orders.filter { it.status.displayName == state.selectedFilter }
        }

        val query = state.searchQuery.trim()
        if (query.isBlank()) return byStatus

        return byStatus.filter { order ->
            order.id.contains(query, ignoreCase = true) ||
                order.customerName.contains(query, ignoreCase = true) ||
                order.location.contains(query, ignoreCase = true)
        }
    }

    fun getTotalOrders(): Int = _uiState.value.orders.size

    fun getPendingOrders(): Int = _uiState.value.orders.count { it.status == OrderStatus.PENDING }

    fun getDeliveringOrders(): Int = _uiState.value.orders.count { it.status == OrderStatus.DELIVERING }

    fun addOrUpdateOrder(order: Order) {
        val exists = _uiState.value.orders.any { it.id == order.id }
        if (exists) repository.updateOrder(order) else repository.addOrder(order)
    }

    fun deleteOrder(id: String) {
        repository.deleteOrder(id)
    }
}
