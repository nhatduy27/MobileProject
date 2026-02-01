package com.example.foodapp.pages.owner.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.owner.wallet.*
import com.example.foodapp.data.repository.owner.base.OwnerWalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalletViewModel : ViewModel() {

    private val repository: OwnerWalletRepository = RepositoryProvider.getOwnerWalletRepository()

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * Load initial data: wallet + ledger + revenue
     */
    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Load wallet info
            val walletResult = repository.getMyWallet()
            walletResult.fold(
                onSuccess = { wallet ->
                    _uiState.update { it.copy(wallet = wallet) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
            
            // Load ledger
            val ledgerResult = repository.getLedger(page = 1, limit = 20)
            ledgerResult.fold(
                onSuccess = { result ->
                    _uiState.update { 
                        it.copy(
                            ledgerEntries = result.entries,
                            ledgerPage = result.page,
                            ledgerTotalPages = result.totalPages,
                            ledgerTotal = result.total,
                            hasMoreLedger = result.page < result.totalPages
                        )
                    }
                },
                onFailure = { /* Ignore ledger error for now */ }
            )
            
            // Load revenue
            loadRevenue(_uiState.value.selectedPeriod)
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Refresh all data
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            
            val walletResult = repository.getMyWallet()
            walletResult.onSuccess { wallet ->
                _uiState.update { it.copy(wallet = wallet) }
            }
            
            val ledgerResult = repository.getLedger(page = 1, limit = 20)
            ledgerResult.onSuccess { result ->
                _uiState.update { 
                    it.copy(
                        ledgerEntries = result.entries,
                        ledgerPage = result.page,
                        ledgerTotalPages = result.totalPages,
                        ledgerTotal = result.total,
                        hasMoreLedger = result.page < result.totalPages
                    )
                }
            }
            
            loadRevenue(_uiState.value.selectedPeriod)
            
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Load more ledger entries
     */
    fun loadMoreLedger() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMoreLedger) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            
            val nextPage = state.ledgerPage + 1
            val result = repository.getLedger(page = nextPage, limit = 20)
            
            result.fold(
                onSuccess = { ledgerResult ->
                    _uiState.update { 
                        it.copy(
                            ledgerEntries = it.ledgerEntries + ledgerResult.entries,
                            ledgerPage = ledgerResult.page,
                            ledgerTotalPages = ledgerResult.totalPages,
                            ledgerTotal = ledgerResult.total,
                            hasMoreLedger = ledgerResult.page < ledgerResult.totalPages,
                            isLoadingMore = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            )
        }
    }

    /**
     * Load revenue statistics
     */
    private fun loadRevenue(period: RevenuePeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRevenueLoading = true) }
            
            val result = repository.getRevenue(period)
            result.fold(
                onSuccess = { stats ->
                    _uiState.update { 
                        it.copy(
                            revenueStats = stats,
                            isRevenueLoading = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isRevenueLoading = false) }
                }
            )
        }
    }

    /**
     * Change selected revenue period
     */
    fun onPeriodSelected(period: RevenuePeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadRevenue(period)
    }

    /**
     * Change active tab
     */
    fun onTabSelected(tab: WalletTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    /**
     * Show payout dialog
     */
    fun showPayoutDialog() {
        _uiState.update { it.copy(showPayoutDialog = true) }
    }

    /**
     * Dismiss payout dialog
     */
    fun dismissPayoutDialog() {
        _uiState.update { it.copy(showPayoutDialog = false) }
    }

    /**
     * Request payout
     */
    fun requestPayout(request: RequestPayoutRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPayoutLoading = true) }
            
            val result = repository.requestPayout(request)
            result.fold(
                onSuccess = { payoutRequest ->
                    _uiState.update { 
                        it.copy(
                            isPayoutLoading = false,
                            showPayoutDialog = false,
                            successMessage = "Yêu cầu rút tiền đã được gửi thành công"
                        )
                    }
                    // Refresh wallet balance
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isPayoutLoading = false,
                            error = error.message ?: "Không thể gửi yêu cầu rút tiền"
                        )
                    }
                }
            )
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Get formatted balance
     */
    fun getFormattedBalance(): String {
        return _uiState.value.wallet?.let {
            String.format("%,.0f", it.balance) + "đ"
        } ?: "0đ"
    }
    
    /**
     * Get formatted total earned
     */
    fun getFormattedTotalEarned(): String {
        return _uiState.value.wallet?.let {
            String.format("%,.0f", it.totalEarned) + "đ"
        } ?: "0đ"
    }
    
    /**
     * Get formatted total withdrawn
     */
    fun getFormattedTotalWithdrawn(): String {
        return _uiState.value.wallet?.let {
            String.format("%,.0f", it.totalWithdrawn) + "đ"
        } ?: "0đ"
    }
}
