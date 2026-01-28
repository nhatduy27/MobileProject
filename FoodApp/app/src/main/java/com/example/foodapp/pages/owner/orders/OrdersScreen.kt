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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.pages.owner.notifications.NotificationBell

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
        containerColor = Color(0xFFF5F5F5)
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
                            CircularProgressIndicator(color = Color(0xFFFF6B35))
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
                            CircularProgressIndicator(color = Color(0xFFFF6B35))
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
            text = "Chưa có đơn hàng nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Đơn hàng mới sẽ xuất hiện ở đây",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onRefresh,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tải lại")
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
            title = "Tổng đơn",
            value = total.toString(),
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        OrderStatCard(
            title = "Chờ xác nhận",
            value = pending.toString(),
            color = Color(0xFFFFA500),
            modifier = Modifier.weight(1f)
        )
        OrderStatCard(
            title = "Đang làm",
            value = preparing.toString(),
            color = Color(0xFF9C27B0),
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
        filters.forEach { filter ->
            OrderFilterChip(
                text = filter,
                isSelected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
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
            .background(Color.White)
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
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color(0xFF1A1A1A))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quản lý đơn hàng",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Notification Bell Icon
                    NotificationBell()

                    IconButton(
                        onClick = { isSearchActive = true },
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF1A1A1A))
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A1A1A))
                    }
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Tìm mã đơn, tên khách...", color = Color.Gray, fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFFFF6B35)
                        ),
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
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
        title = { Text("Hủy đơn hàng") },
        text = {
            Column {
                Text(
                    text = "Bạn có chắc chắn muốn hủy đơn hàng này?",
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Lý do hủy (không bắt buộc)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Text("Hủy đơn")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}
