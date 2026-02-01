package com.example.foodapp.pages.client.voucherhistory

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.data.remote.client.response.voucher.VoucherUsageHistoryApiModel
import com.example.foodapp.ui.theme.FoodAppTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ============== COLOR SCHEME ==============
private val OrangePrimary = Color(0xFFFF6B35)
private val OrangeLight = Color(0xFFFF8C61)
private val OrangeDark = Color(0xFFE55A2B)
private val OrangeBackground = Color(0xFFFFF5F0)
private val OrangeSurface = Color(0xFFFFE8DC)
private val OrangeAccent = Color(0xFFFFAB8C)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun VoucherHistoryScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: VoucherHistoryViewModel = viewModel(factory = VoucherHistoryViewModel.factory(context))

    // Observe LiveData states
    val historyState by viewModel.voucherHistoryState.observeAsState(VoucherHistoryState.Idle)
    val isLoadingMore by viewModel.isLoadingMore.observeAsState(false)

    // Setup for scroll detection
    val listState = rememberLazyListState()

    // Auto fetch on first load
    LaunchedEffect(Unit) {
        if (historyState is VoucherHistoryState.Idle) {
            viewModel.fetchVoucherHistory()
        }
    }

    FoodAppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.voucher_history_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.go_back),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.refresh() },
                            enabled = historyState !is VoucherHistoryState.Loading
                        ) {
                            if (historyState is VoucherHistoryState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.refresh),
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = OrangePrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(OrangeBackground)
            ) {
                when (historyState) {
                    VoucherHistoryState.Idle -> {
                        // Show nothing on initial state
                    }

                    VoucherHistoryState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = OrangePrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = stringResource(R.string.loading_history),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OrangeDark,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    is VoucherHistoryState.Error -> {
                        val errorMessage = (historyState as VoucherHistoryState.Error).message
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(OrangeSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = stringResource(R.string.error),
                                        modifier = Modifier.size(56.dp),
                                        tint = OrangePrimary
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.error_occurred),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeDark
                                    )
                                    Text(
                                        text = errorMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Button(
                                    onClick = { viewModel.refresh() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangePrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.retry),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    VoucherHistoryState.Empty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(28.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(OrangeSurface, OrangeBackground)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = stringResource(R.string.no_history),
                                        modifier = Modifier.size(72.dp),
                                        tint = OrangeAccent
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_history_title),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = OrangeDark
                                    )
                                    Text(
                                        text = stringResource(R.string.no_history_message),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Button(
                                    onClick = { viewModel.refresh() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangePrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(48.dp)
                                        .padding(top = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.refresh),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    is VoucherHistoryState.Success -> {
                        val successState = historyState as VoucherHistoryState.Success
                        SuccessContent(
                            historyItems = successState.history,
                            currentPage = successState.currentPage,
                            totalPages = successState.totalPages,
                            totalItems = successState.totalItems,
                            hasMore = successState.hasMore,
                            listState = listState,
                            isLoadingMore = isLoadingMore,
                            onLoadMore = { viewModel.loadMoreVoucherHistory() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    historyItems: List<VoucherUsageHistoryApiModel>,
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    hasMore: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit
) {
    // Check if we're near the bottom for load more
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            if (totalItemsCount > 0 && lastVisibleItem >= totalItemsCount - 3 && hasMore && !isLoadingMore) {
                onLoadMore()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Stats Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(OrangePrimary, OrangeLight)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.total_usage),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalItems.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 36.sp
                        )
                    }

                    if (totalPages > 1) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.page_info, currentPage, totalPages),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // History List
        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_data),
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyItems) { historyItem ->
                    VoucherHistoryItem(
                        historyItem = historyItem,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Load more indicator
                if (hasMore || isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp,
                                    color = OrangePrimary
                                )
                            } else if (hasMore) {
                                Text(
                                    text = stringResource(R.string.pull_to_load_more),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OrangeAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // End of list indicator
                if (!hasMore && historyItems.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Divider(
                                    modifier = Modifier.width(40.dp),
                                    color = OrangeAccent.copy(alpha = 0.5f),
                                    thickness = 1.dp
                                )
                                Text(
                                    text = stringResource(R.string.all_items_displayed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OrangeAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Divider(
                                    modifier = Modifier.width(40.dp),
                                    color = OrangeAccent.copy(alpha = 0.5f),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoucherHistoryItem(
    historyItem: VoucherUsageHistoryApiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header với gradient cam
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(OrangeSurface, OrangeBackground)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voucher Code
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(OrangePrimary, OrangeLight)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = historyItem.code,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangeDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatShortDate(historyItem.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Discount Amount
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = OrangePrimary,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = formatCurrency(historyItem.discountAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Details với divider
            Divider(color = OrangeSurface, thickness = 1.dp)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Order Info
                DetailRow(
                    icon = Icons.Default.Receipt,
                    label = stringResource(R.string.order_label),
                    value = formatId(historyItem.orderId)
                )

                // Shop Info
                if (historyItem.shopId.isNotBlank()) {
                    DetailRow(
                        icon = Icons.Default.Store,
                        label = stringResource(R.string.shop_label),
                        value = formatId(historyItem.shopId)
                    )
                }

                // Time
                DetailRow(
                    icon = Icons.Default.AccessTime,
                    label = stringResource(R.string.time_label),
                    value = formatFullDate(historyItem.createdAt)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(OrangeSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = OrangePrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
        }
    }
}

// ============== UTILITY FUNCTIONS ==============

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 0
    return "-${formatter.format(amount)}đ"
}

private fun formatId(id: String): String {
    return if (id.length > 12) {
        "${id.take(10)}..."
    } else {
        id
    }
}

private fun formatShortDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

private fun formatFullDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}