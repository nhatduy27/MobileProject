package com.example.foodapp.data.remote.shipper.wallet

import com.example.foodapp.data.model.shipper.wallet.*
import com.google.gson.annotations.SerializedName

/**
 * API Response wrappers for Wallet endpoints
 */

// GET /api/wallets/me
data class WalletResponse(
    val wallet: Wallet
)

data class WrappedWalletResponse(
    val success: Boolean = false,
    val data: WalletResponse? = null
)

// GET /api/wallets/ledger
data class LedgerResponse(
    val entries: List<LedgerEntry> = emptyList(),
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 0
)

data class WrappedLedgerResponse(
    val success: Boolean = false,
    val data: LedgerResponse? = null
)

// POST /api/wallets/payout
data class PayoutResponse(
    val message: String = "",
    val payoutRequest: PayoutRequest? = null
)

data class WrappedPayoutResponse(
    val success: Boolean = false,
    val data: PayoutResponse? = null
)

// GET /api/wallets/revenue
data class WrappedRevenueResponse(
    val success: Boolean = false,
    val data: RevenueStats? = null
)

// Request DTOs

data class PayoutRequestBody(
    val amount: Long,
    val bankCode: String,
    val accountNumber: String,
    val accountName: String,
    val note: String? = null
)
