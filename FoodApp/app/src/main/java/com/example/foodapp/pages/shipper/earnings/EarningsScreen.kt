package com.example.foodapp.pages.shipper.earnings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.shipper.theme.ShipperColors

/**
 * Màn hình Thu nhập của Shipper
 * Hiển thị ví, thống kê doanh thu và lịch sử giao dịch
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(
    earningsViewModel: EarningsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by earningsViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    // Handle messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            earningsViewModel.clearErrorMessage()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            earningsViewModel.clearSuccessMessage()
        }
    }
    
    // Load more when reaching end of list
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            lastVisibleItem >= totalItems - 3 && 
                    !uiState.isLoadingLedger && 
                    uiState.hasMoreLedger
        }
    }
    
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            earningsViewModel.loadMoreLedger()
        }
    }
    
    // Payout dialog
    if (uiState.showPayoutDialog) {
        PayoutDialog(
            currentBalance = uiState.wallet?.balance ?: 0,
            amount = uiState.payoutAmount,
            bankCode = uiState.payoutBankCode,
            accountNumber = uiState.payoutAccountNumber,
            accountName = uiState.payoutAccountName,
            note = uiState.payoutNote,
            isLoading = uiState.isRequestingPayout,
            onAmountChange = { earningsViewModel.updatePayoutAmount(it) },
            onBankCodeChange = { earningsViewModel.updatePayoutBankCode(it) },
            onAccountNumberChange = { earningsViewModel.updatePayoutAccountNumber(it) },
            onAccountNameChange = { earningsViewModel.updatePayoutAccountName(it) },
            onNoteChange = { earningsViewModel.updatePayoutNote(it) },
            onSubmit = { earningsViewModel.requestPayout() },
            onDismiss = { earningsViewModel.closePayoutDialog() }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background),
        state = listState,
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Wallet Summary Card
        item {
            WalletSummaryCard(
                wallet = uiState.wallet,
                revenueStats = uiState.revenueStats,
                onWithdrawClick = { earningsViewModel.openPayoutDialog() },
                isLoading = uiState.isLoadingWallet || uiState.isLoadingRevenue
            )
        }
        
        // Period Filter Chips
        item {
            PeriodFilterChips(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { earningsViewModel.onPeriodSelected(it) },
                isLoading = uiState.isLoadingRevenue
            )
        }
        
        // Revenue Chart Section (if there's daily breakdown data)
        if (uiState.revenueStats?.dailyBreakdown?.isNotEmpty() == true) {
            item {
                RevenueChartSection(
                    dailyBreakdown = uiState.revenueStats!!.dailyBreakdown,
                    isLoading = uiState.isLoadingRevenue
                )
            }
        }
        
        // Section header for transaction history
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lịch sử giao dịch",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ShipperColors.TextPrimary
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.isLoadingLedger && uiState.ledgerEntries.isNotEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = ShipperColors.Primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    // Refresh button
                    IconButton(
                        onClick = { earningsViewModel.refresh() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Làm mới",
                            tint = ShipperColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        // Ledger entries
        if (uiState.ledgerEntries.isEmpty() && !uiState.isLoadingLedger) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Chưa có giao dịch nào",
                            color = ShipperColors.TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { earningsViewModel.loadLedger(refresh = true) }
                        ) {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tải lại")
                        }
                    }
                }
            }
        } else if (uiState.ledgerEntries.isEmpty() && uiState.isLoadingLedger) {
            // Initial loading state
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = ShipperColors.Primary,
                        strokeWidth = 3.dp
                    )
                }
            }
        } else {
            items(
                items = uiState.ledgerEntries,
                key = { it.id }
            ) { entry ->
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LedgerEntryCard(entry = entry)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Loading indicator at bottom
            if (uiState.isLoadingLedger) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ShipperColors.Primary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}
