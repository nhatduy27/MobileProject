package com.example.foodapp.pages.owner.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.foodapp.R
import com.example.foodapp.data.model.owner.wallet.LedgerEntry
import com.example.foodapp.data.model.owner.wallet.LedgerType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Transactions (Ledger) Tab Content
 */
@Composable
fun WalletTransactionsTab(
    entries: List<LedgerEntry>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    total: Int,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Trigger load more when reaching end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= entries.size - 3 && 
                    hasMore && !isLoadingMore) {
                    onLoadMore()
                }
            }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.wallet_transactions_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = stringResource(R.string.wallet_transactions_count, total),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isLoading && entries.isEmpty()) {
            // Initial loading
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (entries.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.wallet_no_transactions),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    TransactionItem(entry = entry)
                }
                
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single Transaction Item
 */
@Composable
fun TransactionItem(
    entry: LedgerEntry,
    modifier: Modifier = Modifier
) {
    val isIncome = entry.isIncome()
    val iconTint = if (isIncome) Color(0xFF4CAF50) else Color(0xFFFF5722)
    val iconBackground = if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFBE9E7)
    val icon = when (entry.type) {
        LedgerType.ORDER_PAYOUT -> Icons.Default.ShoppingCart
        LedgerType.WITHDRAWAL, LedgerType.PAYOUT -> Icons.Default.AccountBalance
        LedgerType.ADJUSTMENT -> Icons.Default.Settings
    }
    
    // Get localized type name
    val typeName = when (entry.type) {
        LedgerType.ORDER_PAYOUT -> stringResource(R.string.wallet_transaction_order_payout)
        LedgerType.WITHDRAWAL, LedgerType.PAYOUT -> stringResource(R.string.wallet_transaction_withdrawal)
        LedgerType.ADJUSTMENT -> stringResource(R.string.wallet_transaction_adjustment)
    }
    
    val currencySuffix = stringResource(R.string.owner_currency_suffix)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = iconBackground,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = typeName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!entry.orderNumber.isNullOrEmpty()) {
                    Text(
                        text = stringResource(R.string.wallet_transaction_order, entry.orderNumber),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = formatDate(entry.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = entry.getFormattedAmount(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
                
                Text(
                    text = stringResource(R.string.wallet_balance_after, String.format("%,.0f", entry.balanceAfter) + currencySuffix),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Format ISO date to readable format
 */
private fun formatDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(isoDate.replace("Z", "").split(".")[0])
        date?.let { outputFormat.format(it) } ?: isoDate
    } catch (e: Exception) {
        isoDate.take(16).replace("T", " ")
    }
}
