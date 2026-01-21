package com.example.foodapp.data.model.owner.order

/**
 * Full order detail model (matches OwnerOrderDetailDto)
 * Used for order detail screen
 */
data class OrderDetail(
    val id: String,
    val orderNumber: String,
    val shopId: String,
    val shopName: String,
    val status: ShopOrderStatus,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val customer: OrderCustomer,
    val deliveryAddress: DeliveryAddress,
    val deliveryNote: String? = null,
    
    // Items
    val items: List<OrderItemPreview> = emptyList(),
    
    // Pricing
    val subtotal: Long,
    val shipFee: Long,
    val discount: Long,
    val total: Long,
    
    // Shipper
    val shipperId: String? = null,
    val shipper: OrderShipper? = null,
    
    // Timestamps
    val createdAt: String?,
    val updatedAt: String?,
    val confirmedAt: String? = null,
    val preparingAt: String? = null,
    val readyAt: String? = null,
    val shippingAt: String? = null,
    val deliveredAt: String? = null,
    val cancelledAt: String? = null,
    
    // Cancellation
    val cancelReason: String? = null,
    val cancelledBy: String? = null,
    
    // Other
    val reviewId: String? = null,
    val reviewedAt: String? = null,
    val paidOut: Boolean = false,
    val paidOutAt: String? = null
) {
    /**
     * Get next available action based on current status
     */
    fun getAvailableActions(): List<OrderAction> {
        return when (status) {
            ShopOrderStatus.PENDING -> listOf(OrderAction.CONFIRM, OrderAction.CANCEL)
            ShopOrderStatus.CONFIRMED -> listOf(OrderAction.PREPARING, OrderAction.CANCEL)
            ShopOrderStatus.PREPARING -> listOf(OrderAction.READY, OrderAction.CANCEL)
            ShopOrderStatus.READY -> emptyList() // Wait for shipper or customer
            ShopOrderStatus.SHIPPING -> emptyList()
            ShopOrderStatus.DELIVERED -> emptyList()
            ShopOrderStatus.CANCELLED -> emptyList()
        }
    }
    
    /**
     * Format timestamp for display
     */
    fun formatTimestamp(timestamp: String?): String? {
        return timestamp?.let {
            try {
                val instant = java.time.Instant.parse(it)
                val zoned = instant.atZone(java.time.ZoneId.systemDefault())
                java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").format(zoned)
            } catch (e: Exception) {
                it
            }
        }
    }
}

/**
 * Available actions for an order
 */
enum class OrderAction(val displayName: String, val actionText: String) {
    CONFIRM("Xác nhận", "Xác nhận đơn"),
    PREPARING("Đang chuẩn bị", "Bắt đầu chuẩn bị"),
    READY("Sẵn sàng", "Đã sẵn sàng"),
    CANCEL("Hủy đơn", "Hủy đơn hàng")
}

/**
 * Paginated orders response
 */
data class PaginatedOrders(
    val orders: List<ShopOrder>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

/**
 * Cancel order request
 */
data class CancelOrderRequest(
    val reason: String? = null
)
