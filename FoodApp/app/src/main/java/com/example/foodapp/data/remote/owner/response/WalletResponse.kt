package com.example.foodapp.data.remote.owner.response

import com.example.foodapp.data.model.owner.wallet.*
import com.google.gson.annotations.SerializedName

/**
 * Inner response for GET /api/wallets/me
 * This is the actual data inside the wrapper
 */
data class WalletInnerResponse(
    val wallet: WalletDto? = null
)

/**
 * Response wrapper for GET /api/wallets/me
 * API returns: { success, data: { wallet: {...} }, timestamp }
 */
data class WrappedWalletResponse(
    val success: Boolean? = true,
    val data: WalletInnerResponse? = null,
    val message: String? = null
)

/**
 * Inner response for GET /api/wallets/ledger
 */
data class LedgerInnerResponse(
    val entries: List<LedgerEntryDto>? = null,
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 0
)

/**
 * Response wrapper for GET /api/wallets/ledger
 * API returns: { success, data: { entries, page, limit, total, totalPages }, timestamp }
 */
data class WrappedLedgerResponse(
    val success: Boolean? = true,
    val data: LedgerInnerResponse? = null,
    val message: String? = null
)

/**
 * Inner response for POST /api/wallets/payout
 */
data class PayoutInnerResponse(
    val message: String? = null,
    val payoutRequest: PayoutRequestDto? = null
)

/**
 * Response wrapper for POST /api/wallets/payout
 * API returns: { success, data: { message, payoutRequest }, timestamp }
 */
data class WrappedPayoutResponse(
    val success: Boolean? = true,
    val data: PayoutInnerResponse? = null,
    val message: String? = null
)

/**
 * Inner response for GET /api/wallets/revenue
 */
data class RevenueInnerResponse(
    val today: Double = 0.0,
    val week: Double = 0.0,
    val month: Double = 0.0,
    val year: Double = 0.0,
    val all: Double = 0.0,
    val dailyBreakdown: List<DailyRevenueDto>? = null,
    val calculatedAt: String? = null
)

/**
 * Response wrapper for GET /api/wallets/revenue
 * API returns: { success, data: { today, week, month, year, all, dailyBreakdown, calculatedAt }, timestamp }
 */
data class WrappedWalletRevenueResponse(
    val success: Boolean? = true,
    val data: RevenueInnerResponse? = null,
    val message: String? = null
)

/**
 * DTO for Wallet from API
 */
data class WalletDto(
    val id: String,
    val type: String,
    val balance: Double = 0.0,
    val totalEarned: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val createdAt: String?,
    val updatedAt: String?
) {
    fun toWallet(): Wallet = Wallet(
        id = id,
        type = when (type) {
            "OWNER" -> WalletType.OWNER
            "SHIPPER" -> WalletType.SHIPPER
            else -> WalletType.OWNER
        },
        balance = balance,
        totalEarned = totalEarned,
        totalWithdrawn = totalWithdrawn,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: ""
    )
}

/**
 * DTO for Ledger Entry from API
 */
data class LedgerEntryDto(
    val id: String,
    val type: String,
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val orderId: String?,
    val orderNumber: String?,
    val description: String?,
    val createdAt: String
) {
    fun toLedgerEntry(): LedgerEntry = LedgerEntry(
        id = id,
        type = when (type) {
            "ORDER_PAYOUT" -> LedgerType.ORDER_PAYOUT
            "WITHDRAWAL" -> LedgerType.WITHDRAWAL
            "ADJUSTMENT" -> LedgerType.ADJUSTMENT
            "PAYOUT" -> LedgerType.PAYOUT
            else -> LedgerType.ORDER_PAYOUT
        },
        amount = amount,
        balanceBefore = balanceBefore,
        balanceAfter = balanceAfter,
        orderId = orderId,
        orderNumber = orderNumber,
        description = description,
        createdAt = createdAt
    )
}

/**
 * DTO for Daily Revenue from API
 */
data class DailyRevenueDto(
    val date: String,
    val amount: Double,
    val orderCount: Int
) {
    fun toDailyRevenue(): DailyRevenue = DailyRevenue(
        date = date,
        amount = amount,
        orderCount = orderCount
    )
}

/**
 * DTO for Payout Request from API
 */
data class PayoutRequestDto(
    val id: String,
    val amount: Double,
    val status: String,
    val bankCode: String,
    val accountNumber: String,
    val accountName: String,
    val createdAt: String
) {
    fun toPayoutRequest(): PayoutRequest = PayoutRequest(
        id = id,
        amount = amount,
        status = status,
        bankCode = bankCode,
        accountNumber = accountNumber,
        accountName = accountName,
        createdAt = createdAt
    )
}
