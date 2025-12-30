package com.example.foodapp.pages.owner.orders

import com.example.foodapp.data.model.owner.Order

/**
 * UI state cho màn hình quản lý đơn hàng.
 */
data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val selectedFilter: String = "Tất cả",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
