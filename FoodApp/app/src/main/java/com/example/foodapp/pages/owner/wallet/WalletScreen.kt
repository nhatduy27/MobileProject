package com.example.foodapp.pages.owner.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import kotlinx.coroutines.launch

/**
 * Main Wallet Screen for Owner
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onMenuClick: () -> Unit,
    viewModel: WalletViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { WalletTab.values().size }
    )
    
    // Sync tab with pager
    LaunchedEffect(uiState.activeTab) {
        val index = WalletTab.values().indexOf(uiState.activeTab)
        if (pagerState.currentPage != index) {
            pagerState.animateScrollToPage(index)
        }
    }
    
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(WalletTab.values()[pagerState.currentPage])
    }
    
    // Success snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }
    
    // Error snackbar
    val retryLabel = stringResource(R.string.owner_retry)
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = retryLabel
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.wallet_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.nav_dashboard))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isLoading && !uiState.isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.owner_retry))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                WalletTab.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = when (tab) {
                                    WalletTab.OVERVIEW -> stringResource(R.string.wallet_tab_overview)
                                    WalletTab.TRANSACTIONS -> stringResource(R.string.wallet_tab_transactions)
                                    WalletTab.REVENUE -> stringResource(R.string.wallet_tab_revenue)
                                },
                                fontWeight = if (pagerState.currentPage == index) 
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            // Content
            if (uiState.isLoading && uiState.wallet == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (WalletTab.values()[page]) {
                            WalletTab.OVERVIEW -> WalletOverviewTab(
                                wallet = uiState.wallet,
                                formattedBalance = viewModel.getFormattedBalance(),
                                formattedTotalEarned = viewModel.getFormattedTotalEarned(),
                                formattedTotalWithdrawn = viewModel.getFormattedTotalWithdrawn(),
                                onWithdrawClick = { viewModel.showPayoutDialog() }
                            )
                            
                            WalletTab.TRANSACTIONS -> WalletTransactionsTab(
                                entries = uiState.ledgerEntries,
                                isLoading = uiState.isLoading,
                                isLoadingMore = uiState.isLoadingMore,
                                hasMore = uiState.hasMoreLedger,
                                total = uiState.ledgerTotal,
                                onLoadMore = { viewModel.loadMoreLedger() }
                            )
                            
                            WalletTab.REVENUE -> WalletRevenueTab(
                                revenueStats = uiState.revenueStats,
                                selectedPeriod = uiState.selectedPeriod,
                                isLoading = uiState.isRevenueLoading,
                                onPeriodSelected = { viewModel.onPeriodSelected(it) }
                            )
                        }
                    }
                }
            }
        }
        
        // Payout Dialog
        if (uiState.showPayoutDialog) {
            PayoutDialog(
                currentBalance = uiState.wallet?.balance ?: 0.0,
                isLoading = uiState.isPayoutLoading,
                onDismiss = { viewModel.dismissPayoutDialog() },
                onSubmit = { request -> viewModel.requestPayout(request) }
            )
        }
    }
}

