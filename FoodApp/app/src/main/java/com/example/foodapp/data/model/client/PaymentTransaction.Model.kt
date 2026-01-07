package com.example.foodapp.data.model.client

import java.util.Date

/**
 * Model đại diện cho giao dịch thanh toán.
 * @param id ID giao dịch
 * @param orderId ID đơn hàng liên quan
 * @param clientId ID người mua
 * @param amount Số tiền thanh toán
 * @param paymentMethod Phương thức thanh toán
 * @param transactionId ID giao dịch từ cổng thanh toán
 * @param status Trạng thái giao dịch
 * @param createdAt Thời gian tạo
 * @param completedAt Thời gian hoàn thành
 * @param paymentInfo Thông tin thanh toán chi tiết (số tài khoản, v.v.)
 */
data class PaymentTransaction(
    val id: String = "",
    val orderId: String = "",
    val clientId: String = "",
    val amount: Double = 0.0,
    //val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val transactionId: String = "",
    //val status: PaymentStatus = PaymentStatus.PENDING,
    val createdAt: Date = Date(),
    val completedAt: Date? = null,
    val paymentInfo: PaymentInfo? = null
)

/**
 * Thông tin chi tiết thanh toán.
 */
data class PaymentInfo(
    val bankName: String? = null,
    val accountNumber: String? = null,
    val accountName: String? = null,
    val momoPhone: String? = null,
    val vnpayCode: String? = null,
    val zalopayId: String? = null
)