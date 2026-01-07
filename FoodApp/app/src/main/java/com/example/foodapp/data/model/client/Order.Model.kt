package com.example.foodapp.data.model.shared

import java.util.Date

/**
 * Model đại diện cho đơn hàng trong hệ thống.
 * @param id ID duy nhất của đơn hàng
 * @param clientId ID người mua
 * @param clientName Tên người mua
 * @param ownerId ID người bán
 * @param ownerName Tên người bán
 * @param shipperId ID người giao hàng (nếu có)
 * @param items Danh sách món ăn trong đơn
 * @param totalAmount Tổng tiền đơn hàng
 * @param deliveryAddress Địa chỉ giao hàng
 * @param deliveryFee Phí giao hàng
 * @param promotionDiscount Giảm giá từ khuyến mãi
 * @param finalAmount Số tiền thực tế phải thanh toán
 * @param paymentMethod Phương thức thanh toán
 * @param paymentStatus Trạng thái thanh toán
 * @param orderStatus Trạng thái đơn hàng
 * @param note Ghi chú đơn hàng
 * @param createdAt Thời gian tạo đơn
 * @param estimatedDeliveryTime Thời gian dự kiến giao hàng
 * @param deliveredAt Thời gian giao hàng thực tế
 * @param clientRating Đánh giá từ người mua (1-5)
 * @param clientComment Bình luận từ người mua
 */
data class Order(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val shipperId: String? = null,
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val deliveryAddress: String = "",
    val deliveryFee: Double = 0.0,
    val promotionDiscount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val note: String = "",
    val createdAt: Date = Date(),
    val estimatedDeliveryTime: Date? = null,
    val deliveredAt: Date? = null,
    val clientRating: Int? = null,
    val clientComment: String = ""
)

/**
 * Model đại diện cho một món ăn trong đơn hàng.
 */
data class OrderItem(
    val foodId: String = "",
    val foodName: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val note: String = ""
)

/**
 * Enum trạng thái đơn hàng.
 */
enum class OrderStatus {
    PENDING,        // Chờ xác nhận
    CONFIRMED,      // Đã xác nhận
    PREPARING,      // Đang chuẩn bị
    READY,          // Sẵn sàng giao
    PICKED_UP,      // Shipper đã nhận hàng
    DELIVERING,     // Đang giao hàng
    DELIVERED,      // Đã giao
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}

/**
 * Enum phương thức thanh toán.
 */
enum class PaymentMethod {
    CASH,           // Tiền mặt
    MOMO,           // Ví MoMo
    VNPAY,          // VNPay
    ZALOPAY,        // ZaloPay
    BANK_TRANSFER   // Chuyển khoản ngân hàng
}

/**
 * Enum trạng thái thanh toán.
 */
enum class PaymentStatus {
    PENDING,        // Chưa thanh toán
    PROCESSING,     // Đang xử lý
    COMPLETED,      // Đã thanh toán
    FAILED,         // Thanh toán thất bại
    REFUNDED        // Đã hoàn tiền
}