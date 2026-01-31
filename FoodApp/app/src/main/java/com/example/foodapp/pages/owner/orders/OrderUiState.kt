package com.example.foodapp.pages.owner.orders

import com.example.foodapp.data.model.owner.order.OrderDetail
import com.example.foodapp.data.model.owner.order.ShopOrder
import com.example.foodapp.data.model.owner.order.ShopOrderStatus

/**
 * UI State for Orders Screen
 */
data class OrderUiState(
    val orders: List<ShopOrder> = emptyList(),
    val selectedFilter: String = FILTER_ALL,
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
    companion object {
        // Filter keys (for internal use)
        const val FILTER_ALL = "all"
        const val FILTER_PENDING = "pending"
        const val FILTER_CONFIRMED = "confirmed"
        const val FILTER_PREPARING = "preparing"
        const val FILTER_READY = "ready"
        const val FILTER_SHIPPING = "shipping"
        const val FILTER_DELIVERED = "delivered"
        const val FILTER_CANCELLED = "cancelled"
        
        /**
         * Map filter key to API status value
         */
        val FILTER_MAP = mapOf(
            FILTER_ALL to null,
            FILTER_PENDING to "PENDING",
            FILTER_CONFIRMED to "CONFIRMED",
            FILTER_PREPARING to "PREPARING",
            FILTER_READY to "READY",
            FILTER_SHIPPING to "SHIPPING",
            FILTER_DELIVERED to "DELIVERED",
            FILTER_CANCELLED to "CANCELLED"
        )
        
        val FILTER_OPTIONS = listOf(
            FILTER_ALL,
            FILTER_PENDING,
            FILTER_CONFIRMED,
            FILTER_PREPARING,
            FILTER_READY,
            FILTER_SHIPPING,
            FILTER_DELIVERED,
            FILTER_CANCELLED
        )
    }
    
    fun getApiStatusFilter(): String? = FILTER_MAP[selectedFilter]
}

