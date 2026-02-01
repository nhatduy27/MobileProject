package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.wallet.*

/**
 * Interface for Owner Wallet Repository.
 * Defines methods for wallet management through backend API.
 */
interface OwnerWalletRepository {
    
    /**
     * Get current user's wallet
     */
    suspend fun getMyWallet(): Result<Wallet>
    
    /**
     * Get ledger (transaction history)
     * @param page Page number (1-indexed)
     * @param limit Items per page
     */
    suspend fun getLedger(page: Int = 1, limit: Int = 20): Result<LedgerResult>
    
    /**
     * Request payout (withdraw funds)
     * @param request Payout request details
     */
    suspend fun requestPayout(request: RequestPayoutRequest): Result<PayoutRequest>
    
    /**
     * Get revenue statistics
     * @param period Revenue period
     */
    suspend fun getRevenue(period: RevenuePeriod = RevenuePeriod.MONTH): Result<RevenueStats>
}

/**
 * Result wrapper for ledger with pagination info
 */
data class LedgerResult(
    val entries: List<LedgerEntry>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)
