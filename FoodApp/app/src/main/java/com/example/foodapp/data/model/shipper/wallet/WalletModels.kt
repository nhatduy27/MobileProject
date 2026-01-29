package com.example.foodapp.data.model.shipper.wallet

import com.google.gson.annotations.SerializedName

/**
 * Firebase Timestamp wrapper để parse timestamp từ backend
 */
data class FirebaseTimestamp(
    @SerializedName("_seconds") val seconds: Long = 0,
    @SerializedName("_nanoseconds") val nanoseconds: Long = 0
) {
    fun toMillis(): Long = seconds * 1000 + nanoseconds / 1_000_000
}

/**
 * Wallet Model - ví của shipper
 */
data class Wallet(
    val id: String = "",
    val type: WalletType = WalletType.SHIPPER,
    val balance: Long = 0,
    val totalEarned: Long = 0,
    val totalWithdrawn: Long = 0,
    val createdAt: FirebaseTimestamp? = null,
    val updatedAt: FirebaseTimestamp? = null
) {
    fun getFormattedBalance(): String {
        return String.format("%,d", balance) + "đ"
    }
    
    fun getFormattedTotalEarned(): String {
        return String.format("%,d", totalEarned) + "đ"
    }
    
    fun getFormattedTotalWithdrawn(): String {
        return String.format("%,d", totalWithdrawn) + "đ"
    }
}

enum class WalletType {
    @SerializedName("OWNER") OWNER,
    @SerializedName("SHIPPER") SHIPPER
}

/**
 * Ledger Entry - giao dịch trong ví
 */
data class LedgerEntry(
    val id: String = "",
    val type: LedgerType = LedgerType.ORDER_PAYOUT,
    val amount: Long = 0,
    val balanceBefore: Long = 0,
    val balanceAfter: Long = 0,
    val orderId: String? = null,
    val orderNumber: String? = null,
    val description: String? = null,
    val createdAt: FirebaseTimestamp? = null
) {
    fun getFormattedAmount(): String {
        val prefix = if (amount >= 0) "+" else ""
        return "$prefix${String.format("%,d", amount)}đ"
    }
    
    fun isIncome(): Boolean = amount > 0
    
    fun getDisplayDescription(): String {
        return when (type) {
            LedgerType.ORDER_PAYOUT -> orderNumber?.let { "Đơn hàng #$it" } ?: "Thu nhập từ đơn hàng"
            LedgerType.WITHDRAWAL -> "Rút tiền"
            LedgerType.ADJUSTMENT -> description ?: "Điều chỉnh"
        }
    }
    
    fun getCreatedAtMillis(): Long = createdAt?.toMillis() ?: 0
}

enum class LedgerType {
    @SerializedName("ORDER_PAYOUT") ORDER_PAYOUT,
    @SerializedName("WITHDRAWAL") WITHDRAWAL,
    @SerializedName("ADJUSTMENT") ADJUSTMENT
}

/**
 * Revenue Stats - thống kê doanh thu
 */
data class RevenueStats(
    val today: Long = 0,
    val week: Long = 0,
    val month: Long = 0,
    val year: Long = 0,
    val all: Long = 0,
    val dailyBreakdown: List<DailyRevenue> = emptyList(),
    val calculatedAt: String? = null
) {
    fun getFormattedToday(): String = String.format("%,d", today) + "đ"
    fun getFormattedWeek(): String = String.format("%,d", week) + "đ"
    fun getFormattedMonth(): String = String.format("%,d", month) + "đ"
    fun getFormattedYear(): String = String.format("%,d", year) + "đ"
    fun getFormattedAll(): String = String.format("%,d", all) + "đ"
}

/**
 * Daily Revenue - doanh thu theo ngày
 */
data class DailyRevenue(
    val date: String = "",
    val amount: Long = 0,
    val orderCount: Int = 0
) {
    fun getFormattedAmount(): String = String.format("%,d", amount) + "đ"
}

/**
 * Revenue Period - các khoảng thời gian lọc doanh thu
 */
enum class RevenuePeriod(val value: String, val displayName: String) {
    TODAY("today", "Hôm nay"),
    WEEK("week", "Tuần này"),
    MONTH("month", "Tháng này"),
    YEAR("year", "Năm nay"),
    ALL("all", "Tất cả")
}

/**
 * Payout Request - yêu cầu rút tiền
 */
data class PayoutRequest(
    val id: String = "",
    val amount: Long = 0,
    val status: PayoutStatus = PayoutStatus.PENDING,
    val bankCode: String = "",
    val accountNumber: String = "",
    val accountName: String = "",
    val createdAt: FirebaseTimestamp? = null
)

enum class PayoutStatus {
    @SerializedName("PENDING") PENDING,
    @SerializedName("APPROVED") APPROVED,
    @SerializedName("REJECTED") REJECTED,
    @SerializedName("TRANSFERRED") TRANSFERRED
}
