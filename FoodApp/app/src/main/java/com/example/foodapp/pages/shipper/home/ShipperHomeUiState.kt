package com.example.foodapp.pages.shipper.home

import com.example.foodapp.data.model.shipper.order.ShipperOrder

data class ShipperHomeUiState(
    val availableOrders: List<ShipperOrder> = emptyList(),
    val myOrders: List<ShipperOrder> = emptyList(),
    val isLoadingAvailable: Boolean = false,
    val isLoadingMyOrders: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0, // 0: Available, 1: My Orders
    val isNotAssignedToShop: Boolean = false // Shipper chưa được phê duyệt/gán vào shop
)
