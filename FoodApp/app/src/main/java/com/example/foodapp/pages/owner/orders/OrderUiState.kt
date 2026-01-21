package com.example.foodapp.pages.owner.orders

import com.example.foodapp.data.model.owner.order.OrderDetail
import com.example.foodapp.data.model.owner.order.ShopOrder
import com.example.foodapp.data.model.owner.order.ShopOrderStatus

/**
 * UI State for Orders Screen
 */
data class OrderUiState(
    val orders: List<ShopOrder> = emptyList(),
    val selectedFilter: String = "Tất cả",
    val searchQuery: String = "",
    
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isActionLoading: Boolean = false,
    
    // Error state
    val error: String? = null,
    
    // Pagination
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMorePages: Boolean = false,
    
    // Selected order for detail view
    val selectedOrder: OrderDetail? = null,
    val showDetailSheet: Boolean = false,
    
    // Cancel dialog
    val showCancelDialog: Boolean = false,
    val cancelOrderId: String? = null
) {
    /**
     * Map filter display name to API status value
     */
    companion object {
        val FILTER_MAP = mapOf(
            "Tất cả" to null,
            "Chờ xác nhận" to "PENDING",
            "Đã xác nhận" to "CONFIRMED",
            "Đang chuẩn bị" to "PREPARING",
            "Sẵn sàng" to "READY",
            "Đang giao" to "SHIPPING",
            "Đã giao" to "DELIVERED",
            "Đã hủy" to "CANCELLED"
        )
        
        val FILTER_OPTIONS = listOf(
            "Tất cả",
            "Chờ xác nhận",
            "Đã xác nhận",
            "Đang chuẩn bị",
            "Sẵn sàng",
            "Đang giao",
            "Đã giao",
            "Đã hủy"
        )
    }
    
    fun getApiStatusFilter(): String? = FILTER_MAP[selectedFilter]
}
