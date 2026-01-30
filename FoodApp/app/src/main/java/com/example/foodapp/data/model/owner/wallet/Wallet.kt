package com.example.foodapp.data.model.owner.wallet

import com.google.gson.annotations.SerializedName

/**
 * Wallet Entity - represents owner's wallet
 */
data class Wallet(
    val id: String,
    val type: WalletType,
    val balance: Double,
    val totalEarned: Double,
    val totalWithdrawn: Double,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Wallet types
 */
enum class WalletType {
    @SerializedName("OWNER")
    OWNER,
    
    @SerializedName("SHIPPER")
    SHIPPER
}

/**
 * Ledger entry - represents a wallet transaction
 */
data class LedgerEntry(
    val id: String,
    val type: LedgerType,
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val orderId: String?,
    val orderNumber: String?,
    val description: String?,
    val createdAt: String
) {
    /**
     * Check if this is an income (positive amount)
     */
    fun isIncome(): Boolean = amount > 0
    
    /**
     * Get formatted amount string
     */
    fun getFormattedAmount(): String {
        val prefix = if (amount >= 0) "+" else ""
        return "$prefix${String.format("%,.0f", amount)}đ"
    }
}

/**
 * Ledger transaction types
 */
enum class LedgerType {
    @SerializedName("ORDER_PAYOUT")
    ORDER_PAYOUT,
    
    @SerializedName("WITHDRAWAL")
    WITHDRAWAL,
    
    @SerializedName("ADJUSTMENT")
    ADJUSTMENT,
    
    @SerializedName("PAYOUT")
    PAYOUT;
    
    fun displayName(): String = when (this) {
        ORDER_PAYOUT -> "Thanh toán đơn hàng"
        WITHDRAWAL -> "Rút tiền"
        ADJUSTMENT -> "Điều chỉnh"
        PAYOUT -> "Rút tiền"
    }
}

/**
 * Daily revenue breakdown
 */
data class DailyRevenue(
    val date: String,
    val amount: Double,
    val orderCount: Int
)

/**
 * Revenue statistics
 */
data class RevenueStats(
    val today: Double,
    val week: Double,
    val month: Double,
    val year: Double,
    val all: Double,
    val dailyBreakdown: List<DailyRevenue>,
    val calculatedAt: String
)

/**
 * Revenue period options
 */
enum class RevenuePeriod(val apiValue: String) {
    TODAY("today"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year"),
    ALL("all");
    
    fun displayName(): String = when (this) {
        TODAY -> "Hôm nay"
        WEEK -> "Tuần này"
        MONTH -> "Tháng này"
        YEAR -> "Năm nay"
        ALL -> "Tất cả"
    }
}

/**
 * Request to payout (withdraw funds)
 */
data class RequestPayoutRequest(
    val amount: Double,
    val bankCode: String,
    val accountNumber: String,
    val accountName: String,
    val note: String? = null
)

/**
 * Payout request response
 */
data class PayoutRequest(
    val id: String,
    val amount: Double,
    val status: String,
    val bankCode: String,
    val accountNumber: String,
    val accountName: String,
    val createdAt: String
)
