package com.example.foodapp.data.model.shipper.order

import com.google.gson.annotations.SerializedName

data class ShipperOrder(
    val id: String,
    val orderNumber: String? = null,
    
    // Shop info
    val shopId: String? = null,
    val shopName: String? = null,
    
    // Status
    val status: String,
    val paymentStatus: String? = null,
    val paymentMethod: String? = null,
    
    // Amounts  
    val subtotal: Double = 0.0,
    val shipFee: Double = 0.0,
    val discount: Double = 0.0,
    @SerializedName("total")
    val totalAmount: Double = 0.0,
    val voucherCode: String? = null,
    
    // Items
    val items: List<ShipperOrderItem> = emptyList(),
    val itemCount: Int? = null,
    @SerializedName("itemsPreview")
    val itemsPreview: List<ShipperOrderItem>? = null,
    
    // Customer info
    val customerId: String? = null,
    @SerializedName("customer")
    val customer: CustomerSnapshotDto? = null,
    @SerializedName("customerSnapshot")
    val customerSnapshot: CustomerSnapshotDto? = null,
    
    // Shipper info  
    val shipperId: String? = null,
    @SerializedName("shipper")
    val shipper: ShipperSnapshotDto? = null,
    @SerializedName("shipperSnapshot")
    val shipperSnapshot: ShipperSnapshotDto? = null,
    
    // Delivery
    @SerializedName("deliveryAddress")
    val deliveryAddress: DeliveryAddressDto? = null,
    val deliveryNote: String? = null,
    
    // Timestamps
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val confirmedAt: String? = null,
    val preparingAt: String? = null,
    val readyAt: String? = null,
    val shippingAt: String? = null,
    val deliveredAt: String? = null,
    val cancelledAt: String? = null,
    
    // Cancellation
    val cancelReason: String? = null,
    val cancelledBy: String? = null
) {
    // Helper để lấy địa chỉ giao hàng dạng string
    val shippingAddress: String?
        get() = deliveryAddress?.fullAddress ?: buildLegacyAddress()
    
    private fun buildLegacyAddress(): String? {
        val parts = listOfNotNull(
            deliveryAddress?.room?.let { "Phòng $it" },
            deliveryAddress?.building?.let { "Tòa $it" },
            deliveryAddress?.label
        )
        return if (parts.isNotEmpty()) parts.joinToString(", ") else null
    }
    
    // Helper để lấy tên khách hàng
    val customerName: String?
        get() = customer?.displayName ?: customerSnapshot?.displayName
    
    // Helper để lấy số điện thoại
    val customerPhone: String?
        get() = customer?.phone ?: customerSnapshot?.phone
        
    // Helper để lấy số món
    val displayItemCount: Int
        get() = itemCount ?: items.size
        
    // Helper lấy trạng thái thanh toán text
    val paymentStatusText: String
        get() = when(paymentStatus) {
            "PAID" -> "Đã thanh toán"
            "UNPAID" -> "Chưa thanh toán"
            "PROCESSING" -> "Đang xử lý"
            "REFUNDED" -> "Đã hoàn tiền"
            else -> paymentStatus ?: ""
        }
        
    // Helper lấy phương thức thanh toán text
    val paymentMethodText: String
        get() = when(paymentMethod) {
            "COD" -> "Tiền mặt"
            "ZALOPAY" -> "ZaloPay"
            "MOMO" -> "MoMo"
            "SEPAY" -> "SePay"
            else -> paymentMethod ?: ""
        }
        
    // Check if this is an available order (not yet assigned)
    val isAvailableForPickup: Boolean
        get() = status == "READY" && shipperId == null
}

data class DeliveryAddressDto(
    val id: String? = null,
    val label: String? = null,
    val fullAddress: String? = null,
    val building: String? = null,
    val buildingCode: String? = null,  // Building code for GPS (e.g., "A1", "B2")
    val room: String? = null,
    val note: String? = null,
    // Legacy format support
    val street: String? = null,
    val ward: String? = null,
    val district: String? = null,
    val city: String? = null
)

data class CustomerSnapshotDto(
    val id: String? = null,
    val displayName: String? = null,
    val phone: String? = null
)

data class ShipperSnapshotDto(
    val id: String? = null,
    val displayName: String? = null,
    val phone: String? = null
)

data class ShipperOrderItem(
    val productId: String? = null,
    val quantity: Int = 0,
    val price: Double = 0.0,
    @SerializedName("productName")
    val name: String = "",
    val imageUrl: String? = null,
    val subtotal: Double = 0.0
)

data class PaginatedShipperOrdersDto(
    val orders: List<ShipperOrder> = emptyList(),
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val totalPages: Int = 0
) {
    // Helper để tương thích với code cũ
    val data: List<ShipperOrder>
        get() = orders
}

// Wrapper for API responses that come wrapped in { success, data, timestamp }
data class WrappedPaginatedOrdersResponse(
    val success: Boolean = false,
    val data: PaginatedShipperOrdersDto? = null,
    val timestamp: String? = null
)

data class WrappedShipperOrderResponse(
    val success: Boolean = false,
    val data: ShipperOrder? = null,
    val timestamp: String? = null
)
