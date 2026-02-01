package com.example.foodapp.data.remote.client.response.payment

import com.google.gson.annotations.SerializedName

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

/**
 * Request DTO cho tạo thanh toán
 * Format request:
 * {
 *   "method": "COD" hoặc "SEPAY"
 * }
 */
data class CreatePaymentRequest(
    @SerializedName("method")
    val method: String
)

/**
 * Response DTO cho API tạo thanh toán
 * Format response:
 * {
 *   "success": true,
 *   "data": {
 *     "message": "Payment created successfully",
 *     "payment": {
 *       "id": "avVu2SHjiSQ68cX2PyHK",
 *       "orderId": "QMmjLsPvCbweh49Td7QL",
 *       "amount": 35000,
 *       "method": "COD",
 *       "status": "PAID",
 *       "providerData": { ... }, // Chỉ có khi method là SEPAY
 *       "createdAt": "2026-01-28T03:38:25.637Z"
 *     }
 *   },
 *   "timestamp": "2026-01-28T03:38:25.994Z"
 * }
 */
data class CreatePaymentResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: PaymentDataWrapper? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

/**
 * Wrapper cho payment data
 */
data class PaymentDataWrapper(
    @SerializedName("message")
    val message: String = "",

    @SerializedName("payment")
    val payment: PaymentData
)

/**
 * Dữ liệu payment trong response
 * Lưu ý: amount là số nguyên (Int) không phải số thập phân (Double)
 */
data class PaymentData(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("amount")
    val amount: Int = 0,

    @SerializedName("method")
    val method: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("providerData")
    val providerData: ProviderData? = null,

    @SerializedName("createdAt")
    val createdAt: String = ""
)

/**
 * Dữ liệu nhà cung cấp thanh toán
 * Chỉ có khi method là SEPAY
 * QR Code URL CÓ trong response: "qrCodeUrl": "https://qr.sepay.vn/img?..."
 */
data class ProviderData(
    @SerializedName("sepayContent")
    val sepayContent: String? = null,

    @SerializedName("qrCodeUrl")  // CÓ TRONG RESPONSE
    val qrCodeUrl: String? = null,

    @SerializedName("accountNumber")
    val accountNumber: String? = null,

    @SerializedName("accountName")
    val accountName: String? = null,

    @SerializedName("bankCode")
    val bankCode: String? = null,

    @SerializedName("amount")
    val amount: Int? = null,  // SỬA: Double → Int
)

/**
 * Request DTO cho verify payment
 * Format request: POST /api/orders/:orderId/payment/verify
 * Request body: KHÔNG CÓ BODY (chỉ cần orderId trong path)
 *
 * Chỉ cần gửi Authorization header và orderId trong path
 */
// Không cần request DTO vì không có request body

/**
 * Response DTO cho verify payment (inner data)
 * Format response trong data field:
 * {
 *   "matched": true/false,
 *   "message": "Payment verified successfully" / "Payment not yet confirmed",
 *   "payment": {
 *     "id": "payment_id",
 *     "status": "PAID" / "PROCESSING",
 *     "method": "SEPAY",
 *     "amount": 35000,
 *     "paidAt": "2026-01-28T03:39:17.374Z" // (optional, chỉ có khi matched = true)
 *   }
 * }
 */
data class VerifyPaymentData(
    @SerializedName("matched")
    val matched: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("payment")
    val payment: VerifyPaymentDetail
)

data class VerifyPaymentDetail(
    @SerializedName("id")
    val id: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("method")
    val method: String,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("paidAt")
    val paidAt: String? = null
)

/**
 * Response wrapper cho verify payment (dùng wrapper format)
 * Format response:
 * {
 *   "success": true,
 *   "data": {
 *     "matched": true,
 *     "message": "Payment verified successfully",
 *     "payment": {
 *       "id": "payment_id",
 *       "status": "PAID",
 *       "method": "SEPAY",
 *       "amount": 35000,
 *       "paidAt": "2026-01-28T03:39:17.374Z"
 *     }
 *   },
 *   "timestamp": "2026-01-28T03:38:25.994Z"
 * }
 */
data class VerifyPaymentResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: VerifyPaymentData? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)


/**
 * Response DTO cho API lấy thông tin thanh toán (Get Payment)
 * Format response CHÍNH XÁC từ ảnh:
 * {
 *   "success": true,
 *   "data": {
 *     "message": "Payment retrieved successfully",
 *     "paymentId": "GJ19mI0lAKus2ler56x",
 *     "orderId": "ycM7Wdf1WlwHSGVAhxD",
 *     "amount": 30000,
 *     "method": "SEPAY",
 *     "status": "PAID",
 *     "providerData": {
 *       "sepayContent": "KTXORD1769736133140FEB322",
 *       "qrCodeUrl": "https://qr.sepay.vn/img?acc=00012112005000&bank=MB&amount=30000&des=KTXORD1769736133140FEB322&template=compact",
 *       "accountNumber": "00012112005000",
 *       "accountName": "TONG DUONG THAI HOA",
 *       "bankCode": "MB",
 *       "amount": 30000,
 *       "verifiedAt": "2026-01-30T01:22:26.539Z"
 *     },
 *     "paidAt": "2026-01-30T01:22:26.539Z",
 *     "createdAt": "2026-01-30T01:22:14.172Z"
 *   }
 * }
 */
data class GetPaymentResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: GetPaymentData? = null
)

/**
 * Data class cho phần "data" trong response
 */
data class GetPaymentData(
    @SerializedName("message")
    val message: String = "",

    @SerializedName("payment")  // <- QUAN TRỌNG: Có field "payment" này
    val payment: PaymentDetail? = null
)

/**
 * Data class cho thông tin chi tiết payment
 * Đây là object nested trong "payment"
 */
data class PaymentDetail(
    @SerializedName("id")
    val paymentId: String = "",  // Chú ý: JSON là "id" nhưng bạn có thể map thành "paymentId"

    @SerializedName("orderId")
    val orderId: String = "",

    @SerializedName("amount")
    val amount: Int = 0,

    @SerializedName("method")
    val method: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("providerData")
    val providerData: GetPaymentProviderData? = null,

    @SerializedName("paidAt")
    val paidAt: String? = null,  // Có thể không có trong response nếu chưa thanh toán

    @SerializedName("createdAt")
    val createdAt: String = ""
)

/**
 * Dữ liệu providerData trong GetPaymentResponse
 * Format theo JSON hiện tại (không có verifiedAt trong response này)
 */
data class GetPaymentProviderData(
    @SerializedName("sepayContent")
    val sepayContent: String? = null,

    @SerializedName("qrCodeUrl")
    val qrCodeUrl: String? = null,

    @SerializedName("accountNumber")
    val accountNumber: String? = null,

    @SerializedName("accountName")
    val accountName: String? = null,

    @SerializedName("bankCode")
    val bankCode: String? = null,

    @SerializedName("amount")
    val amount: Int? = null
    // verifiedAt không có trong response này
)
/**
 * Enum để dễ sử dụng
 */
enum class PaymentMethod(val value: String) {
    COD("COD"),
    SEPAY("SEPAY");

    companion object {
        fun fromValue(value: String): PaymentMethod? {
            return values().find { it.value == value }
        }
    }
}

enum class PaymentStatus(val value: String) {
    PAID("PAID"),
    PROCESSING("PROCESSING"),
    VERIFYING("VERIFYING");  // Thêm status mới cho đang xác minh

    companion object {
        fun fromValue(value: String): PaymentStatus? {
            return values().find { it.value == value }
        }
    }
}
