package com.example.foodapp.pages.owner.vouchers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.owner.voucher.Voucher
import com.example.foodapp.data.model.owner.voucher.VoucherType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import com.example.foodapp.pages.owner.notifications.NotificationBell

// Colors
private val OrangeAccent = Color(0xFFFF6B35)
private val GreenActive = Color(0xFF4CAF50)
private val RedInactive = Color(0xFFF44336)
private val YellowWarning = Color(0xFFFFA500)
private val BlueInfo = Color(0xFF2196F3)
private val PurplePrimary = Color(0xFF9C27B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VouchersScreen(
    onMenuClick: () -> Unit,
    vouchersViewModel: VouchersViewModel = viewModel()
) {
    val uiState by vouchersViewModel.uiState.collectAsState()
    val filteredVouchers = remember(uiState.vouchers, uiState.searchQuery, uiState.selectedFilter) {
        vouchersViewModel.getFilteredVouchers()
    }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            vouchersViewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            vouchersViewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            VouchersSearchHeader(
                query = uiState.searchQuery,
                onQueryChange = vouchersViewModel::onSearchQueryChanged,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vouchersViewModel.showCreateDialog() },
                containerColor = OrangeAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm voucher")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { vouchersViewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter chips
                VouchersFilterRow(
                    filters = VoucherUiState.FILTER_OPTIONS,
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = vouchersViewModel::onFilterSelected
                )

                // Content
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OrangeAccent)
                        }
                    }
                    filteredVouchers.isEmpty() -> {
                        EmptyVouchersView(
                            onRefresh = { vouchersViewModel.refresh() },
                            onCreateClick = { vouchersViewModel.showCreateDialog() }
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Stats row
                            item {
                                VouchersStatsRow(
                                    total = vouchersViewModel.getTotalVouchers(),
                                    active = vouchersViewModel.getActiveVouchers(),
                                    expired = vouchersViewModel.getExpiredVouchers()
                                )
                            }

                            // Voucher cards
                            items(
                                items = filteredVouchers,
                                key = { it.id }
                            ) { voucher ->
                                VoucherCard(
                                    voucher = voucher,
                                    onToggleStatus = { vouchersViewModel.toggleVoucherStatus(voucher) },
                                    onEdit = { vouchersViewModel.showEditDialog(voucher) },
                                    onDelete = { vouchersViewModel.showDeleteDialog(voucher) }
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
                            CircularProgressIndicator(color = OrangeAccent)
                        }
                    }
                }
            }
        }
    }

    // Create Dialog
    if (uiState.showCreateDialog) {
        CreateVoucherDialog(
            onDismiss = { vouchersViewModel.dismissCreateDialog() },
            onCreate = { request -> vouchersViewModel.createVoucher(request) },
            isLoading = uiState.isActionLoading
        )
    }

    // Edit Dialog
    if (uiState.showEditDialog && uiState.selectedVoucher != null) {
        EditVoucherDialog(
            voucher = uiState.selectedVoucher!!,
            onDismiss = { vouchersViewModel.dismissEditDialog() },
            onSave = { request -> 
                vouchersViewModel.updateVoucher(uiState.selectedVoucher!!.id, request) 
            },
            isLoading = uiState.isActionLoading
        )
    }

    // Delete Dialog
    if (uiState.showDeleteDialog && uiState.selectedVoucher != null) {
        DeleteVoucherDialog(
            voucher = uiState.selectedVoucher!!,
            onDismiss = { vouchersViewModel.dismissDeleteDialog() },
            onConfirm = { vouchersViewModel.deleteVoucher() }
        )
    }
}

@Composable
fun VouchersSearchHeader(
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color(0xFF1A1A1A))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quản lý Voucher",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
                
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
                        placeholder = { Text("Tìm mã voucher, tên...", color = Color.Gray, fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = OrangeAccent
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
fun VouchersFilterRow(
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
            VoucherFilterChip(
                text = filter,
                isSelected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
fun VoucherFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = OrangeAccent,
            selectedLabelColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun VouchersStatsRow(total: Int, active: Int, expired: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VoucherStatCard(
            title = "Tổng voucher",
            value = total.toString(),
            color = BlueInfo,
            modifier = Modifier.weight(1f)
        )
        VoucherStatCard(
            title = "Đang hoạt động",
            value = active.toString(),
            color = GreenActive,
            modifier = Modifier.weight(1f)
        )
        VoucherStatCard(
            title = "Hết hạn",
            value = expired.toString(),
            color = RedInactive,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun VoucherStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VoucherCard(
    voucher: Voucher,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isExpired = try {
        val expiryTime = Instant.parse(voucher.validTo)
        Instant.now().isAfter(expiryTime)
    } catch (e: Exception) {
        false
    }

    val statusColor = when {
        isExpired -> RedInactive
        voucher.isActive -> GreenActive
        else -> Color(0xFF999999)
    }

    val statusText = when {
        isExpired -> "Hết hạn"
        voucher.isActive -> "Đang hoạt động"
        else -> "Đã tắt"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Code badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OrangeAccent.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = voucher.code,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Status badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name and description
            if (!voucher.name.isNullOrBlank()) {
                Text(
                    text = voucher.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!voucher.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = voucher.description,
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Discount info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VoucherInfoItem(
                    icon = Icons.Default.LocalOffer,
                    label = "Giảm",
                    value = formatDiscount(voucher),
                    color = PurplePrimary
                )

                if (voucher.minOrderAmount != null && voucher.minOrderAmount > 0) {
                    VoucherInfoItem(
                        icon = Icons.Default.ShoppingCart,
                        label = "Đơn tối thiểu",
                        value = formatCurrency(voucher.minOrderAmount.toLong()),
                        color = BlueInfo
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Usage and validity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VoucherInfoItem(
                    icon = Icons.Default.People,
                    label = "Đã dùng",
                    value = "${voucher.currentUsage}/${voucher.usageLimit}",
                    color = YellowWarning
                )

                VoucherInfoItem(
                    icon = Icons.Default.Schedule,
                    label = "Hiệu lực",
                    value = formatDateRange(voucher.validFrom, voucher.validTo),
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color(0xFFEEEEEE))

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle status button
                OutlinedButton(
                    onClick = onToggleStatus,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (voucher.isActive) RedInactive else GreenActive
                    ),
                    enabled = !isExpired
                ) {
                    Icon(
                        if (voucher.isActive) Icons.Default.Block else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (voucher.isActive) "Tắt" else "Bật", fontSize = 13.sp)
                }

                // Edit button
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BlueInfo)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sửa", fontSize = 13.sp)
                }

                // Delete button
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedInactive)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun VoucherInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF999999)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyVouchersView(
    onRefresh: () -> Unit,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎫",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có voucher nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tạo voucher để thu hút khách hàng",
            fontSize = 14.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tạo voucher mới")
        }
        Spacer(modifier = Modifier.height(12.dp))
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

// Helper functions
private fun formatDiscount(voucher: Voucher): String {
    return when (voucher.type) {
        VoucherType.PERCENTAGE -> {
            val maxDiscountText = if (voucher.maxDiscount != null && voucher.maxDiscount > 0) {
                " (tối đa ${formatCurrency(voucher.maxDiscount.toLong())})"
            } else ""
            "${voucher.value.toInt()}%$maxDiscountText"
        }
        VoucherType.FIXED_AMOUNT -> formatCurrency(voucher.value.toLong())
        VoucherType.FREE_SHIP -> "Miễn phí ship ${voucher.value.toInt()}%"
    }
}

private fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return "${formatter.format(amount)}đ"
}

private fun formatDateRange(from: String, to: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        
        val fromDate = inputFormat.parse(from.replace("Z", ""))
        val toDate = inputFormat.parse(to.replace("Z", ""))
        
        "${outputFormat.format(fromDate!!)} - ${outputFormat.format(toDate!!)}"
    } catch (e: Exception) {
        "N/A"
    }
}
