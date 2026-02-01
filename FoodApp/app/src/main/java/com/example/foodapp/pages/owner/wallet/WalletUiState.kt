package com.example.foodapp.pages.owner.wallet

import com.example.foodapp.data.model.owner.wallet.*
import com.example.foodapp.data.repository.owner.base.LedgerResult

/**
 * UI State for Wallet Screen
 */
data class WalletUiState(
    // Wallet info
    val wallet: Wallet? = null,
    
    // Ledger (transaction history)
    val ledgerEntries: List<LedgerEntry> = emptyList(),
    val ledgerPage: Int = 1,
    val ledgerTotalPages: Int = 1,
    val ledgerTotal: Int = 0,
    val hasMoreLedger: Boolean = false,
    
    // Revenue stats
    val revenueStats: RevenueStats? = null,
    val selectedPeriod: RevenuePeriod = RevenuePeriod.MONTH,
    
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRevenueLoading: Boolean = false,
    
    // Payout dialog
    val showPayoutDialog: Boolean = false,
    val isPayoutLoading: Boolean = false,
    
    // Active tab
    val activeTab: WalletTab = WalletTab.OVERVIEW,
    
    // Messages
    val error: String? = null,
    val successMessage: String? = null
) {
    companion object {
        // Formatted values for UI
        fun Wallet.formattedBalance(): String = formatCurrency(balance)
        fun Wallet.formattedTotalEarned(): String = formatCurrency(totalEarned)
        fun Wallet.formattedTotalWithdrawn(): String = formatCurrency(totalWithdrawn)
        
        private fun formatCurrency(amount: Double): String {
            return String.format("%,.0f", amount) + "đ"
        }
    }
}

/**
 * Wallet screen tabs
 */
enum class WalletTab {
    OVERVIEW,
    TRANSACTIONS,
    REVENUE;
    
    fun displayName(): String = when (this) {
        OVERVIEW -> "Tổng quan"
        TRANSACTIONS -> "Lịch sử"
        REVENUE -> "Doanh thu"
    }
}
