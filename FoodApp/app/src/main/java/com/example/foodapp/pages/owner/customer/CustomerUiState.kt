package com.example.foodapp.pages.owner.customer

import com.example.foodapp.data.model.owner.buyer.BuyerDetail
import com.example.foodapp.data.model.owner.buyer.BuyerListItem
import com.example.foodapp.data.model.owner.buyer.BuyerTier

/**
 * Filter options cho Buyer list
 */
enum class BuyerFilter(val apiValue: String, val displayName: String) {
    ALL("ALL", "Tất cả"),
    VIP("VIP", "VIP"),
    NORMAL("NORMAL", "Thường xuyên"),
    NEW("NEW", "Mới");
    
    companion object {
        fun fromDisplayName(name: String): BuyerFilter {
            return values().find { it.displayName == name } ?: ALL
        }
    }
}

/**
 * UI state cho màn hình khách hàng (Buyer)
 */
data class CustomerUiState(
    // Danh sách buyers từ API
    val buyers: List<BuyerListItem> = emptyList(),
    
    // Buyer detail khi xem chi tiết
    val selectedBuyer: BuyerDetail? = null,
    
    // Bộ lọc đang được chọn
    val selectedFilter: BuyerFilter = BuyerFilter.ALL,
    
    // Search query
    val searchQuery: String = "",
    
    // Pagination
    val currentPage: Int = 1,
    val totalPages: Int = 0,
    val totalBuyers: Int = 0,
    
    // Loading & Error states
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val errorMessage: String? = null
)
