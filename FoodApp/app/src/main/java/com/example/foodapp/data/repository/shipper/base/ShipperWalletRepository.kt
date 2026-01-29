package com.example.foodapp.data.repository.shipper.base

import com.example.foodapp.data.model.shipper.wallet.*

/**
 * Interface cho Wallet Repository của Shipper.
 * Định nghĩa các phương thức liên quan đến ví và thu nhập.
 */
interface ShipperWalletRepository {
    
    /**
     * Lấy thông tin ví của shipper hiện tại
     */
    suspend fun getMyWallet(): Result<Wallet>
    
    /**
     * Lấy lịch sử giao dịch (ledger)
     */
    suspend fun getLedger(page: Int = 1, limit: Int = 20): Result<LedgerResult>
    
    /**
     * Yêu cầu rút tiền
     */
    suspend fun requestPayout(
        amount: Long,
        bankCode: String,
        accountNumber: String,
        accountName: String,
        note: String? = null
    ): Result<PayoutRequest>
    
    /**
     * Lấy thống kê doanh thu theo khoảng thời gian
     */
    suspend fun getRevenue(period: RevenuePeriod = RevenuePeriod.MONTH): Result<RevenueStats>
}

/**
 * Result wrapper cho ledger pagination
 */
data class LedgerResult(
    val entries: List<LedgerEntry>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)
