package com.example.foodapp.data.model.shipper.order

import com.google.gson.annotations.SerializedName

data class ShipperOrder(
    val id: String,
    @SerializedName("total")
    val totalAmount: Double = 0.0,
    val status: String,
    val paymentStatus: String? = null,
    val paymentMethod: String? = null,
    val items: List<ShipperOrderItem> = emptyList(),
    val customerId: String? = null,
    val shopId: String? = null,
    @SerializedName("deliveryAddress")
    val deliveryAddress: DeliveryAddressDto? = null,
    @SerializedName("customerSnapshot")
    val customerSnapshot: CustomerSnapshotDto? = null,
    val shopName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val shippingAt: String? = null,
    val deliveredAt: String? = null,
    val deliveryNote: String? = null,
    val orderNumber: String? = null
) {
    // Helper để lấy địa chỉ giao hàng dạng string
    val shippingAddress: String?
        get() = deliveryAddress?.fullAddress
    
    // Helper để lấy tên khách hàng
    val customerName: String?
        get() = customerSnapshot?.displayName
    
    // Helper để lấy số điện thoại
    val customerPhone: String?
        get() = customerSnapshot?.phone
}

data class DeliveryAddressDto(
    val id: String? = null,
    val label: String? = null,
    val fullAddress: String? = null,
    val building: String? = null,
    val room: String? = null,
    val note: String? = null
)

data class CustomerSnapshotDto(
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

// Pagination response wrapper - matches backend PaginatedOrdersDto
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
