package com.example.foodapp.data.model.owner.buyer

import com.google.gson.annotations.SerializedName

/**
 * Buyer tier enum (maps to backend)
 */
enum class BuyerTier {
    @SerializedName("NEW") NEW,
    @SerializedName("NORMAL") NORMAL,
    @SerializedName("VIP") VIP
}

/**
 * Buyer list item DTO - từ GET /owner/buyers
 */
data class BuyerListItem(
    @SerializedName("customerId") val customerId: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("tier") val tier: BuyerTier = BuyerTier.NEW,
    @SerializedName("totalOrders") val totalOrders: Int = 0,
    @SerializedName("totalSpent") val totalSpent: Double = 0.0,
    @SerializedName("avgOrderValue") val avgOrderValue: Double = 0.0,
    @SerializedName("joinedDate") val joinedDate: String? = null,
    @SerializedName("lastOrderDate") val lastOrderDate: String? = null
) {
    // Helper để lấy tier text hiển thị
    val tierText: String
        get() = when (tier) {
            BuyerTier.VIP -> "VIP"
            BuyerTier.NORMAL -> "Thường xuyên"
            BuyerTier.NEW -> "Mới"
        }
    
    // Helper để format tổng chi tiêu
    val totalSpentFormatted: String
        get() = formatCurrency(totalSpent)
    
    // Helper để format giá trị đơn trung bình
    val avgOrderValueFormatted: String
        get() = formatCurrency(avgOrderValue)
}

/**
 * Pagination info
 */
data class PaginationInfo(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0
)

/**
 * Paginated buyer list response - từ GET /owner/buyers
 */
data class PaginatedBuyerList(
    @SerializedName("items") val items: List<BuyerListItem> = emptyList(),
    @SerializedName("pagination") val pagination: PaginationInfo = PaginationInfo()
)

/**
 * Recent order DTO - trong buyer detail
 */
data class RecentOrder(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("orderNumber") val orderNumber: String,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String
) {
    val totalFormatted: String
        get() = formatCurrency(total)
    
    val statusText: String
        get() = when (status) {
            "DELIVERED" -> "Đã giao"
            "CANCELLED" -> "Đã hủy"
            "PENDING" -> "Chờ xác nhận"
            "CONFIRMED" -> "Đã xác nhận"
            "PREPARING" -> "Đang chuẩn bị"
            "READY" -> "Sẵn sàng"
            "SHIPPING" -> "Đang giao"
            else -> status
        }
}

/**
 * Buyer detail DTO - từ GET /owner/buyers/{customerId}
 */
data class BuyerDetail(
    @SerializedName("customerId") val customerId: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("tier") val tier: BuyerTier = BuyerTier.NEW,
    @SerializedName("totalOrders") val totalOrders: Int = 0,
    @SerializedName("totalSpent") val totalSpent: Double = 0.0,
    @SerializedName("avgOrderValue") val avgOrderValue: Double = 0.0,
    @SerializedName("joinedDate") val joinedDate: String? = null,
    @SerializedName("firstOrderDate") val firstOrderDate: String? = null,
    @SerializedName("lastOrderDate") val lastOrderDate: String? = null,
    @SerializedName("recentOrders") val recentOrders: List<RecentOrder> = emptyList()
) {
    val tierText: String
        get() = when (tier) {
            BuyerTier.VIP -> "VIP"
            BuyerTier.NORMAL -> "Thường xuyên"
            BuyerTier.NEW -> "Mới"
        }
    
    val totalSpentFormatted: String
        get() = formatCurrency(totalSpent)
    
    val avgOrderValueFormatted: String
        get() = formatCurrency(avgOrderValue)
}

// Wrapper responses for API
data class WrappedBuyerListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: PaginatedBuyerList? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class WrappedBuyerDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: BuyerDetail? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

// Utility function to format currency
private fun formatCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000_000 -> String.format("%.1fB", amount / 1_000_000_000)
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.0fK", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}
