package com.example.foodapp.data.model.owner.order

import com.google.gson.annotations.SerializedName

/**
 * Order status enum matching backend status values
 */
enum class ShopOrderStatus(val displayName: String, val apiValue: String) {
    PENDING("Chờ xác nhận", "PENDING"),
    CONFIRMED("Đã xác nhận", "CONFIRMED"),
    PREPARING("Đang chuẩn bị", "PREPARING"),
    READY("Sẵn sàng giao", "READY"),
    SHIPPING("Đang giao", "SHIPPING"),
    DELIVERED("Đã giao", "DELIVERED"),
    CANCELLED("Đã hủy", "CANCELLED");

    companion object {
        fun fromApiValue(value: String): ShopOrderStatus {
            return entries.find { it.apiValue == value } ?: PENDING
        }
    }
}

/**
 * Payment status enum
 */
enum class PaymentStatus(val displayName: String, val apiValue: String) {
    UNPAID("Chưa thanh toán", "UNPAID"),
    PROCESSING("Đang xử lý", "PROCESSING"),
    PAID("Đã thanh toán", "PAID"),
    REFUNDED("Đã hoàn tiền", "REFUNDED");

    companion object {
        fun fromApiValue(value: String): PaymentStatus {
            return entries.find { it.apiValue == value } ?: UNPAID
        }
    }
}

/**
 * Payment method enum
 */
enum class PaymentMethod(val displayName: String, val apiValue: String) {
    COD("Tiền mặt", "COD"),
    ZALOPAY("ZaloPay", "ZALOPAY"),
    MOMO("MoMo", "MOMO"),
    SEPAY("SePay", "SEPAY");

    companion object {
        fun fromApiValue(value: String): PaymentMethod {
            return entries.find { it.apiValue == value } ?: COD
        }
    }
}

/**
 * Customer snapshot in order
 */
data class OrderCustomer(
    val id: String,
    val displayName: String? = null,
    val phone: String? = null
)

/**
 * Shipper snapshot in order
 */
data class OrderShipper(
    val id: String,
    val displayName: String? = null,
    val phone: String? = null
)

/**
 * Delivery address in order
 */
data class DeliveryAddress(
    val label: String? = null,
    val fullAddress: String? = null,
    val building: String? = null,
    val room: String? = null,
    val note: String? = null
) {
    fun getDisplayAddress(): String {
        val parts = mutableListOf<String>()
        if (!building.isNullOrBlank()) parts.add("Tòa $building")
        if (!room.isNullOrBlank()) parts.add("Phòng $room")
        if (!fullAddress.isNullOrBlank()) parts.add(fullAddress)
        return if (parts.isEmpty()) label ?: "Chưa có địa chỉ" else parts.joinToString(", ")
    }
}

/**
 * Order item preview
 */
data class OrderItemPreview(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Long,
    val subtotal: Long
)

/**
 * Shop order model for list display (matches OrderListItemDto)
 */
data class ShopOrder(
    val id: String,
    val orderNumber: String,
    val shopId: String,
    val shopName: String,
    val status: ShopOrderStatus,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod?,
    val total: Long,
    val itemCount: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val itemsPreview: List<OrderItemPreview> = emptyList(),
    val itemsPreviewCount: Int = 0,
    val customer: OrderCustomer?,
    val deliveryAddress: DeliveryAddress?,
    val shipperId: String? = null,
    val shipper: OrderShipper? = null
) {
    /**
     * Get items display text for card
     */
    fun getItemsDisplayText(): String {
        return if (itemsPreview.isEmpty()) {
            "$itemCount món"
        } else {
            itemsPreview.joinToString("\n") { "• ${it.productName} x${it.quantity}" }
        }
    }

    /**
     * Get formatted creation time
     */
    fun getDisplayTime(): String {
        return createdAt?.let {
            try {
                // Parse ISO-8601 and format to HH:mm
                val instant = java.time.Instant.parse(it)
                val zoned = instant.atZone(java.time.ZoneId.systemDefault())
                java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(zoned)
            } catch (e: Exception) {
                it.substringAfter("T").substringBefore(".")
            }
        } ?: "--:--"
    }
}
