package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.wallet.RequestPayoutRequest
import com.example.foodapp.data.remote.owner.response.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for Wallet Management
 * 
 * Endpoints:
 * - GET /wallets/me - Get my wallet
 * - GET /wallets/ledger - Get ledger history
 * - POST /wallets/payout - Request payout
 * - GET /wallets/revenue - Get revenue stats
 */
interface WalletApiService {

    /**
     * GET /wallets/me
     * Get current user's wallet
     */
    @GET("wallets/me")
    suspend fun getMyWallet(): Response<WrappedWalletResponse>

    /**
     * GET /wallets/ledger
     * Get wallet ledger (transaction history)
     * 
     * @param page Page number (1-indexed)
     * @param limit Items per page (default 20)
     */
    @GET("wallets/ledger")
    suspend fun getLedger(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<WrappedLedgerResponse>

    /**
     * POST /wallets/payout
     * Request a payout (withdraw funds)
     */
    @POST("wallets/payout")
    suspend fun requestPayout(
        @Body request: RequestPayoutRequest
    ): Response<WrappedPayoutResponse>

    /**
     * GET /wallets/revenue
     * Get revenue statistics
     * 
     * @param period Period for revenue: today | week | month | year | all
     */
    @GET("wallets/revenue")
    suspend fun getRevenue(
        @Query("period") period: String = "month"
    ): Response<WrappedWalletRevenueResponse>
}
