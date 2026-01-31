package com.example.foodapp.pages.owner.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.R
import com.example.foodapp.pages.owner.notifications.NotificationBell
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onMenuClick: () -> Unit,
    ordersViewModel: OrdersViewModel = viewModel()
) {
    val uiState by ordersViewModel.uiState.collectAsState()
    val filteredOrders = remember(uiState.orders, uiState.searchQuery) {
        ordersViewModel.getFilteredOrders()
    }

    // Error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            ordersViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            OrdersSearchHeader(
                query = uiState.searchQuery,
                onQueryChange = ordersViewModel::onSearchQueryChanged,
                onMenuClick = onMenuClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = OwnerColors.Background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { ordersViewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter chips
                OrdersFilterRow(
                    filters = OrderUiState.FILTER_OPTIONS,
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = ordersViewModel::onFilterSelected
                )

                // Content
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OwnerColors.Primary)
                        }
                    }
                    filteredOrders.isEmpty() -> {
                        EmptyOrdersView(
                            onRefresh = { ordersViewModel.refresh() }
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Stats row
                            item {
                                OrdersStatsRow(
                                    total = ordersViewModel.getTotalOrders(),
                                    pending = ordersViewModel.getPendingOrders(),
                                    preparing = ordersViewModel.getPreparingOrders()
                                )
                            }

                            // Order cards
                            items(
                                items = filteredOrders,
                                key = { it.id }
                            ) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = {
                                        ordersViewModel.loadOrderDetail(order.id)
                                    },
                                    onActionClick = { orderId ->
                                        ordersViewModel.confirmOrder(orderId)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Loading overlay for actions
            if (uiState.isActionLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            CircularProgressIndicator(color = OwnerColors.Primary)
                        }
                    }
                }
            }
        }
    }

    // Order Detail Bottom Sheet
    if (uiState.showDetailSheet && uiState.selectedOrder != null) {
        OrderDetailBottomSheet(
            order = uiState.selectedOrder!!,
            onDismiss = { ordersViewModel.dismissDetailSheet() },
            onConfirm = { ordersViewModel.confirmOrder(it) },
            onPreparing = { ordersViewModel.markPreparing(it) },
            onReady = { ordersViewModel.markReady(it) },
            onCancel = { ordersViewModel.showCancelDialog(it) }
        )
    }

    // Cancel Dialog
    if (uiState.showCancelDialog) {
        CancelOrderDialog(
            onDismiss = { ordersViewModel.dismissCancelDialog() },
            onConfirm = { reason -> ordersViewModel.cancelOrder(reason) }
        )
    }
}

@Composable
fun EmptyOrdersView(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📦",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.orders_no_orders),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = OwnerColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.orders_new_orders_appear),
            fontSize = 14.sp,
            color = OwnerColors.TextTertiary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onRefresh,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.owner_retry))
        }
    }
}

@Composable
fun OrdersStatsRow(total: Int, pending: Int, preparing: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OrderStatCard(
            title = stringResource(R.string.orders_total),
            value = total.toString(),
            color = OwnerColors.Info,
            modifier = Modifier.weight(1f)
        )
        OrderStatCard(
            title = stringResource(R.string.orders_pending),
            value = pending.toString(),
            color = OwnerColors.Warning,
            modifier = Modifier.weight(1f)
        )
        OrderStatCard(
            title = stringResource(R.string.orders_preparing),
            value = preparing.toString(),
            color = OwnerColors.StatusPreparing,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OrdersFilterRow(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filterKey ->
            OrderFilterChip(
                text = getFilterDisplayName(filterKey),
                isSelected = filterKey == selectedFilter,
                onClick = { onFilterSelected(filterKey) }
            )
        }
    }
}

@Composable
fun getFilterDisplayName(filterKey: String): String {
    return when (filterKey) {
        OrderUiState.FILTER_ALL -> stringResource(R.string.orders_filter_all)
        OrderUiState.FILTER_PENDING -> stringResource(R.string.orders_filter_pending)
        OrderUiState.FILTER_CONFIRMED -> stringResource(R.string.orders_filter_confirmed)
        OrderUiState.FILTER_PREPARING -> stringResource(R.string.orders_filter_preparing)
        OrderUiState.FILTER_READY -> stringResource(R.string.orders_filter_ready)
        OrderUiState.FILTER_SHIPPING -> stringResource(R.string.orders_filter_shipping)
        OrderUiState.FILTER_DELIVERED -> stringResource(R.string.orders_filter_delivered)
        OrderUiState.FILTER_CANCELLED -> stringResource(R.string.orders_filter_cancelled)
        else -> filterKey
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
        else focusManager.clearFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(OwnerColors.Surface)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn(tween(300)) + slideInHorizontally(),
            exit = fadeOut(tween(300)) + slideOutHorizontally { -it / 2 }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.nav_dashboard), tint = OwnerColors.TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.orders_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OwnerColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Notification Bell Icon
                    NotificationBell()

                    IconButton(
                        onClick = { isSearchActive = true },
                        modifier = Modifier
                            .background(OwnerColors.SurfaceVariant, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.owner_search), tint = OwnerColors.TextPrimary)
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End, animationSpec = tween(300)),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End, animationSpec = tween(300))
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        isSearchActive = false 
                        onQueryChange("")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.owner_close), tint = OwnerColors.TextPrimary)
                    }
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text(stringResource(R.string.orders_search_hint), color = Color.Gray, fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = OwnerColors.Primary
                        ),
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.owner_close), tint = Color.Gray)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CancelOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.orders_cancel_order)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.orders_cancel_confirm),
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.orders_cancel_reason)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Error)
            ) {
                Text(stringResource(R.string.orders_cancel_order))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.owner_close))
            }
        }
    )
}
