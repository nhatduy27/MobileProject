package com.example.foodapp.data.remote.owner.response

import com.example.foodapp.data.model.owner.order.*
import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper
 * Backend returns: {"success": true, "data": {...}, "timestamp": "..."}
 */
data class ApiWrapper<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Wrapped response for paginated orders
 */
typealias WrappedPaginatedOrdersResponse = ApiWrapper<PaginatedOrdersResponse>

/**
 * Wrapped response for order detail
 */
typealias WrappedOrderDetailResponse = ApiWrapper<OrderDetailResponse>

/**
 * Wrapped response for order actions
 */
typealias WrappedOrderActionResponse = ApiWrapper<OrderActionResponse>

/**
 * Response for GET /orders/shop - paginated orders list
 */
data class PaginatedOrdersResponse(
    val orders: List<OrderListItemResponse>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
) {
    fun toPaginatedOrders(): PaginatedOrders {
        return PaginatedOrders(
            orders = orders.map { it.toShopOrder() },
            page = page,
            limit = limit,
            total = total,
            totalPages = totalPages
        )
    }
}

/**
 * Single order item in list response (matches OrderListItemDto)
 */
data class OrderListItemResponse(
    val id: String,
    val orderNumber: String,
    val shopId: String,
    val shopName: String,
    val status: String,
    val paymentStatus: String,
    val paymentMethod: String? = null,
    val total: Long,
    val itemCount: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val itemsPreview: List<ItemPreviewResponse>? = null,
    val itemsPreviewCount: Int? = null,
    val customer: CustomerSnapshotResponse? = null,
    val deliveryAddress: DeliveryAddressResponse? = null,
    val shipperId: String? = null,
    val shipper: ShipperSnapshotResponse? = null
) {
    fun toShopOrder(): ShopOrder {
        return ShopOrder(
            id = id,
            orderNumber = orderNumber,
            shopId = shopId,
            shopName = shopName,
            status = ShopOrderStatus.fromApiValue(status),
            paymentStatus = PaymentStatus.fromApiValue(paymentStatus),
            paymentMethod = paymentMethod?.let { PaymentMethod.fromApiValue(it) },
            total = total,
            itemCount = itemCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
            itemsPreview = itemsPreview?.map { it.toOrderItemPreview() } ?: emptyList(),
            itemsPreviewCount = itemsPreviewCount ?: 0,
            customer = customer?.toOrderCustomer(),
            deliveryAddress = deliveryAddress?.toDeliveryAddress(),
            shipperId = shipperId,
            shipper = shipper?.toOrderShipper()
        )
    }
}

/**
 * Item preview in order (matches OrderItemPreviewDto)
 */
data class ItemPreviewResponse(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Long,
    val subtotal: Long
) {
    fun toOrderItemPreview(): OrderItemPreview {
        return OrderItemPreview(
            productId = productId,
            productName = productName,
            quantity = quantity,
            price = price,
            subtotal = subtotal
        )
    }
}

/**
 * Customer snapshot (matches OrderCustomerSnapshotDto)
 */
data class CustomerSnapshotResponse(
    val id: String,
    val displayName: String? = null,
    val phone: String? = null
) {
    fun toOrderCustomer(): OrderCustomer {
        return OrderCustomer(
            id = id,
            displayName = displayName,
            phone = phone
        )
    }
}

/**
 * Shipper snapshot
 */
data class ShipperSnapshotResponse(
    val id: String,
    val displayName: String? = null,
    val phone: String? = null
) {
    fun toOrderShipper(): OrderShipper {
        return OrderShipper(
            id = id,
            displayName = displayName,
            phone = phone
        )
    }
}

/**
 * Delivery address response
 */
data class DeliveryAddressResponse(
    val label: String? = null,
    val fullAddress: String? = null,
    val building: String? = null,
    val room: String? = null,
    val note: String? = null
) {
    fun toDeliveryAddress(): DeliveryAddress {
        return DeliveryAddress(
            label = label,
            fullAddress = fullAddress,
            building = building,
            room = room,
            note = note
        )
    }
}

/**
 * Response for GET /orders/shop/{id} - order detail
 */
data class OrderDetailResponse(
    val id: String,
    val orderNumber: String,
    val shopId: String,
    val shopName: String,
    val status: String,
    val paymentStatus: String,
    val paymentMethod: String,
    val customer: CustomerSnapshotResponse,
    val deliveryAddress: DeliveryAddressResponse,
    val deliveryNote: String? = null,
    val items: List<ItemPreviewResponse> = emptyList(),
    val subtotal: Long,
    val shipFee: Long,
    val discount: Long,
    val total: Long,
    val shipperId: String? = null,
    val shipper: ShipperSnapshotResponse? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val confirmedAt: String? = null,
    val preparingAt: String? = null,
    val readyAt: String? = null,
    val shippingAt: String? = null,
    val deliveredAt: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: String? = null,
    val cancelledBy: String? = null,
    val reviewId: String? = null,
    val reviewedAt: String? = null,
    val paidOut: Boolean = false,
    val paidOutAt: String? = null
) {
    fun toOrderDetail(): OrderDetail {
        return OrderDetail(
            id = id,
            orderNumber = orderNumber,
            shopId = shopId,
            shopName = shopName,
            status = ShopOrderStatus.fromApiValue(status),
            paymentStatus = PaymentStatus.fromApiValue(paymentStatus),
            paymentMethod = PaymentMethod.fromApiValue(paymentMethod),
            customer = customer.toOrderCustomer(),
            deliveryAddress = deliveryAddress.toDeliveryAddress(),
            deliveryNote = deliveryNote,
            items = items.map { it.toOrderItemPreview() },
            subtotal = subtotal,
            shipFee = shipFee,
            discount = discount,
            total = total,
            shipperId = shipperId,
            shipper = shipper?.toOrderShipper(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            confirmedAt = confirmedAt,
            preparingAt = preparingAt,
            readyAt = readyAt,
            shippingAt = shippingAt,
            deliveredAt = deliveredAt,
            cancelledAt = cancelledAt,
            cancelReason = cancelReason,
            cancelledBy = cancelledBy,
            reviewId = reviewId,
            reviewedAt = reviewedAt,
            paidOut = paidOut,
            paidOutAt = paidOutAt
        )
    }
}

/**
 * Response for order actions (confirm, preparing, ready, cancel)
 * Returns the updated order entity
 */
data class OrderActionResponse(
    val id: String,
    val orderNumber: String? = null,
    val status: String,
    val paymentStatus: String? = null,
    val updatedAt: String? = null,
    // Other fields may be included but we mainly care about status change
    val message: String? = null
)
