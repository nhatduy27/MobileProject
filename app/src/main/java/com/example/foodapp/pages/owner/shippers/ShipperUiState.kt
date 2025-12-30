package com.example.foodapp.pages.owner.shippers

import com.example.foodapp.data.model.owner.Shipper

/**
 * UI state cho màn hình quản lý shipper phía chủ quán.
 */
data class ShipperUiState(
    val shippers: List<Shipper> = emptyList(),
    val selectedStatus: String = "Tất cả",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
