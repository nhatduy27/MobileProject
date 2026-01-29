package com.example.foodapp.data.remote.shipper.wallet

import retrofit2.Response
import retrofit2.http.*

/**
 * Wallet API Service - các endpoint liên quan đến ví
 */
interface WalletApiService {

    /**
     * Get my wallet
     * GET /api/wallets/me
     */
    @GET("wallets/me")
    suspend fun getMyWallet(): Response<WrappedWalletResponse>

    /**
     * Get wallet ledger history
     * GET /api/wallets/ledger?page=1&limit=20
     */
    @GET("wallets/ledger")
    suspend fun getLedger(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<WrappedLedgerResponse>

    /**
     * Request payout (withdraw funds)
     * POST /api/wallets/payout
     */
    @POST("wallets/payout")
    suspend fun requestPayout(
        @Body request: PayoutRequestBody
    ): Response<WrappedPayoutResponse>

    /**
     * Get revenue statistics
     * GET /api/wallets/revenue?period=month
     */
    @GET("wallets/revenue")
    suspend fun getRevenue(
        @Query("period") period: String = "month"
    ): Response<WrappedRevenueResponse>
}
