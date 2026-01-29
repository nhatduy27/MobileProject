package com.example.foodapp.pages.shipper.earnings

import com.example.foodapp.data.model.shipper.wallet.*
import com.example.foodapp.data.repository.shipper.base.LedgerResult

/**
 * UI State cho màn Thu nhập của Shipper
 * Sử dụng dữ liệu từ Wallet API
 */
data class EarningsUiState(
    // Loading states
    val isLoadingWallet: Boolean = false,
    val isLoadingRevenue: Boolean = false,
    val isLoadingLedger: Boolean = false,
    val isRequestingPayout: Boolean = false,
    
    // Data
    val wallet: Wallet? = null,
    val revenueStats: RevenueStats? = null,
    val ledgerEntries: List<LedgerEntry> = emptyList(),
    val ledgerPage: Int = 1,
    val ledgerTotalPages: Int = 1,
    val hasMoreLedger: Boolean = false,
    
    // Selected period for filter
    val selectedPeriod: RevenuePeriod = RevenuePeriod.MONTH,
    
    // Messages
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Payout dialog
    val showPayoutDialog: Boolean = false,
    val payoutAmount: String = "",
    val payoutBankCode: String = "",
    val payoutAccountNumber: String = "",
    val payoutAccountName: String = "",
    val payoutNote: String = ""
)
