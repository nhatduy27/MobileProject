package com.example.foodapp.pages.shipper.earnings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.shipper.wallet.RevenuePeriod
import com.example.foodapp.data.repository.shipper.base.ShipperWalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn Thu nhập của Shipper
 * Kết nối với Wallet API
 */
class EarningsViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "EarningsViewModel"
    }
    
    private val walletRepository: ShipperWalletRepository = RepositoryProvider.getWalletRepository()
    
    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState: StateFlow<EarningsUiState> = _uiState.asStateFlow()
    
    init {
        loadAllData()
    }
    
    /**
     * Load tất cả dữ liệu ban đầu
     */
    fun loadAllData() {
        loadWallet()
        loadRevenue()
        loadLedger(refresh = true)
    }
    
    /**
     * Refresh all data
     */
    fun refresh() {
        loadAllData()
    }
    
    /**
     * Load thông tin ví
     */
    private fun loadWallet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWallet = true, errorMessage = null) }
            
            walletRepository.getMyWallet()
                .onSuccess { wallet ->
                    Log.d(TAG, "Wallet loaded: balance=${wallet.balance}")
                    _uiState.update { it.copy(wallet = wallet, isLoadingWallet = false) }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load wallet", e)
                    _uiState.update { 
                        it.copy(
                            isLoadingWallet = false,
                            errorMessage = e.message ?: "Không thể tải thông tin ví"
                        )
                    }
                }
        }
    }
    
    /**
     * Load thống kê doanh thu theo period
     */
    fun loadRevenue(period: RevenuePeriod = _uiState.value.selectedPeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRevenue = true, selectedPeriod = period) }
            
            walletRepository.getRevenue(period)
                .onSuccess { stats ->
                    Log.d(TAG, "Revenue loaded: today=${stats.today}, month=${stats.month}")
                    _uiState.update { 
                        it.copy(
                            revenueStats = stats,
                            isLoadingRevenue = false
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load revenue", e)
                    _uiState.update { 
                        it.copy(
                            isLoadingRevenue = false,
                            errorMessage = e.message ?: "Không thể tải thống kê doanh thu"
                        )
                    }
                }
        }
    }
    
    /**
     * Load lịch sử giao dịch
     */
    fun loadLedger(refresh: Boolean = false) {
        viewModelScope.launch {
            val currentPage = if (refresh) 1 else _uiState.value.ledgerPage
            
            _uiState.update { it.copy(isLoadingLedger = true) }
            
            walletRepository.getLedger(page = currentPage, limit = 20)
                .onSuccess { result ->
                    Log.d(TAG, "Ledger loaded: ${result.entries.size} entries")
                    
                    val newEntries = if (refresh) {
                        result.entries
                    } else {
                        _uiState.value.ledgerEntries + result.entries
                    }
                    
                    _uiState.update { 
                        it.copy(
                            ledgerEntries = newEntries,
                            ledgerPage = result.page,
                            ledgerTotalPages = result.totalPages,
                            hasMoreLedger = result.page < result.totalPages,
                            isLoadingLedger = false
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load ledger", e)
                    _uiState.update { 
                        it.copy(
                            isLoadingLedger = false,
                            errorMessage = e.message ?: "Không thể tải lịch sử giao dịch"
                        )
                    }
                }
        }
    }
    
    /**
     * Load thêm lịch sử giao dịch (pagination)
     */
    fun loadMoreLedger() {
        if (_uiState.value.isLoadingLedger || !_uiState.value.hasMoreLedger) return
        
        _uiState.update { it.copy(ledgerPage = it.ledgerPage + 1) }
        loadLedger(refresh = false)
    }
    
    /**
     * Thay đổi period filter
     */
    fun onPeriodSelected(period: RevenuePeriod) {
        if (period != _uiState.value.selectedPeriod) {
            loadRevenue(period)
        }
    }
    
    /**
     * Mở dialog rút tiền
     */
    fun openPayoutDialog() {
        _uiState.update { it.copy(showPayoutDialog = true) }
    }
    
    /**
     * Đóng dialog rút tiền
     */
    fun closePayoutDialog() {
        _uiState.update { 
            it.copy(
                showPayoutDialog = false,
                payoutAmount = "",
                payoutBankCode = "",
                payoutAccountNumber = "",
                payoutAccountName = "",
                payoutNote = ""
            )
        }
    }
    
    /**
     * Cập nhật thông tin rút tiền
     */
    fun updatePayoutAmount(value: String) {
        _uiState.update { it.copy(payoutAmount = value) }
    }
    
    fun updatePayoutBankCode(value: String) {
        _uiState.update { it.copy(payoutBankCode = value) }
    }
    
    fun updatePayoutAccountNumber(value: String) {
        _uiState.update { it.copy(payoutAccountNumber = value) }
    }
    
    fun updatePayoutAccountName(value: String) {
        _uiState.update { it.copy(payoutAccountName = value) }
    }
    
    fun updatePayoutNote(value: String) {
        _uiState.update { it.copy(payoutNote = value) }
    }
    
    /**
     * Gửi yêu cầu rút tiền
     */
    fun requestPayout() {
        val state = _uiState.value
        
        // Validate
        val amount = state.payoutAmount.replace(",", "").replace(".", "").toLongOrNull()
        if (amount == null || amount < 50000) {
            _uiState.update { it.copy(errorMessage = "Số tiền rút tối thiểu là 50,000đ") }
            return
        }
        
        if (state.payoutBankCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn ngân hàng") }
            return
        }
        
        if (state.payoutAccountNumber.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập số tài khoản") }
            return
        }
        
        if (state.payoutAccountName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên chủ tài khoản") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingPayout = true) }
            
            walletRepository.requestPayout(
                amount = amount,
                bankCode = state.payoutBankCode,
                accountNumber = state.payoutAccountNumber,
                accountName = state.payoutAccountName,
                note = state.payoutNote.ifBlank { null }
            )
                .onSuccess { payoutRequest ->
                    Log.d(TAG, "Payout requested: ${payoutRequest.id}")
                    _uiState.update { 
                        it.copy(
                            isRequestingPayout = false,
                            showPayoutDialog = false,
                            successMessage = "Yêu cầu rút tiền đã được gửi thành công!",
                            payoutAmount = "",
                            payoutBankCode = "",
                            payoutAccountNumber = "",
                            payoutAccountName = "",
                            payoutNote = ""
                        )
                    }
                    // Reload wallet to update balance
                    loadWallet()
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to request payout", e)
                    _uiState.update { 
                        it.copy(
                            isRequestingPayout = false,
                            errorMessage = e.message ?: "Không thể gửi yêu cầu rút tiền"
                        )
                    }
                }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
