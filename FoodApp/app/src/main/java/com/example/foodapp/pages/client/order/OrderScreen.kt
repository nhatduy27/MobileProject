package com.example.foodapp.pages.client.order

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.foodapp.pages.client.components.order.OrderCard
import com.example.foodapp.ui.theme.*

// Màu chủ đạo cam
private val PrimaryOrange = Color(0xFFFF6B35)
private val LightOrange = Color(0xFFFFF4E6)
private val DarkOrange = Color(0xFFE55A2B)
private val AccentOrange = Color(0xFFFF8C42)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit = {}
) {
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.factory())
    val coroutineScope = rememberCoroutineScope()

    val orderState by viewModel.orderState.observeAsState(OrderState.Idle)
    val orders by viewModel.orders.observeAsState(emptyList())
    val isLoadingMore by viewModel.isLoadingMore.observeAsState(false)
    val hasMore by viewModel.hasMore.observeAsState(true)
    val selectedStatus by viewModel.selectedStatus.observeAsState(null)
    val deleteState by viewModel.deleteOrderState.observeAsState()

    // Sắp xếp đơn hàng: Ưu tiên đơn đang giao (SHIPPING) lên đầu
    val sortedOrders = remember(orders) {
        orders.sortedWith(
            compareByDescending<com.example.foodapp.data.remote.client.response.order.OrderPreviewApiModel> {
                when (it.status) {
                    "SHIPPING" -> 3      // Đang giao - Ưu tiên cao nhất
                    "PENDING" -> 2       // Đang chờ
                    "DELIVERED" -> 1     // Đã giao
                    "CANCELLED" -> 0     // Đã hủy - Ưu tiên thấp nhất
                    else -> -1
                }
            }.thenByDescending { it.createdAt }
        )
    }

    // State để quản lý flow xóa đơn hàng
    var showDeleteReasonDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var deletingOrderId by remember { mutableStateOf<String?>(null) }
    var deleteReason by remember { mutableStateOf("") }

    // Danh sách lý do xóa
    val deleteReasons = listOf(
        "Đặt nhầm đơn hàng",
        "Thay đổi ý định mua hàng",
        "Thông tin đơn hàng không chính xác",
        "Tìm thấy sản phẩm tốt hơn",
        "Lý do khác"
    )

    // Hiển thị snackbar thông báo
    val snackbarHostState = remember { SnackbarHostState() }

    // Xử lý kết quả xóa đơn hàng
    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is DeleteOrderState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Đã xóa đơn hàng thành công",
                    actionLabel = "OK"
                )
                showDeleteReasonDialog = false
                showConfirmDeleteDialog = false
                deletingOrderId = null
                deleteReason = ""
                viewModel.resetDeleteState()
            }
            is DeleteOrderState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    actionLabel = "Thử lại"
                )
                showConfirmDeleteDialog = false
                viewModel.resetDeleteState()
            }
            else -> {}
        }
    }

    // Load orders on first launch
    LaunchedEffect(Unit) {
        viewModel.getOrders()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = PrimaryOrange,
                        contentColor = Color.White
                    )
                }
            )
        },
        topBar = {
            OrderTopBar(
                selectedStatus = selectedStatus,
                onFilterClick = viewModel::filterByStatus,
                onRefresh = viewModel::refresh,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            when (orderState) {
                is OrderState.Idle -> {
                    // Initial state
                }
                is OrderState.Loading -> {
                    LoadingState()
                }
                is OrderState.Success -> {
                    if (sortedOrders.isEmpty()) {
                        EmptyState()
                    } else {
                        OrderList(
                            orders = sortedOrders,
                            isLoadingMore = isLoadingMore,
                            hasMore = hasMore,
                            onLoadMore = viewModel::loadMoreOrders,
                            onDeleteClick = { orderId ->
                                deletingOrderId = orderId
                                showDeleteReasonDialog = true
                            },
                            onOrderClick = onOrderClick
                        )
                    }
                }
                is OrderState.Error -> {
                    ErrorState(
                        message = (orderState as OrderState.Error).message,
                        onRetry = { viewModel.refresh() }
                    )
                }
                is OrderState.Empty -> {
                    EmptyState()
                }
            }

            // ============== DIALOG 1: CHỌN LÝ DO XÓA ==============
            if (showDeleteReasonDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteReasonDialog = false
                        deletingOrderId = null
                        deleteReason = ""
                    },
                    title = {
                        Text(
                            "Lý do xóa đơn hàng",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryOrange
                        )
                    },
                    text = {
                        Column {
                            // Hiển thị thông tin đơn hàng
                            deletingOrderId?.let { orderId ->
                                val order = sortedOrders.find { it.id == orderId }
                                order?.let {
                                    Column(
                                        modifier = Modifier
                                            .background(LightOrange, RoundedCornerShape(12.dp))
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            "Mã đơn: #${it.orderNumber}",
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarkOrange
                                        )
                                        Text(
                                            "Cửa hàng: ${it.shopName}",
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Tổng tiền: ${formatPrice(it.total)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            Text(
                                "Vui lòng chọn lý do bạn muốn xóa đơn hàng này:",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Danh sách lý do
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                deleteReasons.forEach { reason ->
                                    val isSelected = deleteReason == reason
                                    Card(
                                        onClick = {
                                            deleteReason = reason
                                            if (reason == "Lý do khác") {
                                                deleteReason = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected)
                                                LightOrange
                                            else
                                                Color.White
                                        ),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = if (isSelected)
                                                androidx.compose.ui.graphics.SolidColor(PrimaryOrange)
                                            else
                                                androidx.compose.ui.graphics.SolidColor(Color.LightGray)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    deleteReason = reason
                                                    if (reason == "Lý do khác") {
                                                        deleteReason = ""
                                                    }
                                                },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = PrimaryOrange,
                                                    unselectedColor = Color.Gray
                                                )
                                            )
                                            Text(
                                                text = reason,
                                                modifier = Modifier.weight(1f),
                                                fontSize = 14.sp,
                                                color = if (isSelected) DarkOrange else Color.Black
                                            )
                                        }
                                    }
                                }
                            }

                            // TextField cho lý do khác
                            if (deleteReason.isEmpty() || deleteReasons.none { it == deleteReason }) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = deleteReason,
                                    onValueChange = { deleteReason = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nhập lý do khác") },
                                    placeholder = { Text("Vui lòng nhập lý do cụ thể...") },
                                    maxLines = 3,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryOrange,
                                        focusedLabelColor = PrimaryOrange,
                                        cursorColor = PrimaryOrange
                                    )
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (deleteReason.isNotBlank()) {
                                    showDeleteReasonDialog = false
                                    showConfirmDeleteDialog = true
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Vui lòng chọn hoặc nhập lý do xóa",
                                            actionLabel = "OK"
                                        )
                                    }
                                }
                            },
                            enabled = deleteReason.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryOrange,
                                disabledContainerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tiếp tục", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteReasonDialog = false
                                deletingOrderId = null
                                deleteReason = ""
                            }
                        ) {
                            Text("Hủy", color = Color.Gray)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // ============== DIALOG 2: XÁC NHẬN XÓA ==============
            if (showConfirmDeleteDialog && deletingOrderId != null) {
                AlertDialog(
                    onDismissRequest = {
                        showConfirmDeleteDialog = false
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Cảnh báo",
                                tint = PrimaryOrange,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                "Xác nhận xóa đơn hàng",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryOrange
                            )
                        }
                    },
                    text = {
                        Column {
                            // Hiển thị thông tin đơn hàng
                            val order = sortedOrders.find { it.id == deletingOrderId }
                            order?.let {
                                Column(
                                    modifier = Modifier
                                        .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        "Bạn sắp xóa đơn hàng sau:",
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkOrange
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("📦 Mã đơn: #${it.orderNumber}", fontSize = 14.sp)
                                    Text("🏪 Cửa hàng: ${it.shopName}", fontSize = 14.sp)
                                    Text("💰 Tổng tiền: ${formatPrice(it.total)}", fontSize = 14.sp)
                                    Text("📅 Ngày đặt: ${formatDate(it.createdAt)}", fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Hiển thị lý do xóa
                            Column(
                                modifier = Modifier
                                    .background(LightOrange, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    "Lý do xóa:",
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkOrange
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    deleteReason,
                                    color = Color(0xFF424242),
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Cảnh báo quan trọng
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Cảnh báo",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Hành động này KHÔNG THỂ hoàn tác!",
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                deletingOrderId?.let { orderId ->
                                    viewModel.deleteOrder(orderId)
                                }
                                showConfirmDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("XÓA ĐƠN HÀNG", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showConfirmDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Hủy bỏ", fontWeight = FontWeight.Medium)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun OrderList(
    orders: List<com.example.foodapp.data.remote.client.response.order.OrderPreviewApiModel>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onDeleteClick: (String) -> Unit,
    onOrderClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orders) { order ->
            // Wrap OrderCard với clickable và hiệu ứng
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onOrderClick(order.id) }),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                OrderCard(
                    order = order,
                    onDeleteClick = onDeleteClick,
                    isDeleting = false
                )
            }
        }

        if (hasMore) {
            item {
                if (isLoadingMore) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryOrange)
                    }
                } else {
                    LaunchedEffect(listState) {
                        val layoutInfo = listState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index

                        if (lastVisibleItem != null && lastVisibleItem >= totalItems - 5) {
                            onLoadMore()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTopBar(
    selectedStatus: String?,
    onFilterClick: (String?) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Đơn hàng của tôi",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = PrimaryOrange,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }
        },
        actions = {
            // Filter dropdown
            var expanded by remember { mutableStateOf(false) }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Lọc đơn hàng"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tất cả") },
                        onClick = {
                            onFilterClick(null)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AllInbox,
                                contentDescription = null,
                                tint = PrimaryOrange
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Đang giao",
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryOrange
                            )
                        },
                        onClick = {
                            onFilterClick("SHIPPING")
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DeliveryDining,
                                contentDescription = null,
                                tint = PrimaryOrange
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Đang chờ") },
                        onClick = {
                            onFilterClick("PENDING")
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Đã giao") },
                        onClick = {
                            onFilterClick("DELIVERED")
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Đã hủy") },
                        onClick = {
                            onFilterClick("CANCELLED")
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            // Refresh button
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Làm mới"
                )
            }
        }
    )
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = PrimaryOrange)
            Text(
                "Đang tải đơn hàng...",
                color = PrimaryOrange,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Không có đơn hàng",
                modifier = Modifier.size(80.dp),
                tint = PrimaryOrange.copy(alpha = 0.5f)
            )
            Text(
                text = "Chưa có đơn hàng nào",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryOrange
            )
            Text(
                text = "Hãy đặt món ngay để trải nghiệm\ndịch vụ giao hàng nhanh chóng!",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Lỗi",
                modifier = Modifier.size(80.dp),
                tint = PrimaryOrange
            )
            Text(
                text = "Đã xảy ra lỗi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryOrange
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryOrange
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thử lại", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Helper functions
fun formatPrice(price: Double): String {
    return try {
        String.format("%,.0f", price) + "đ"
    } catch (e: Exception) {
        "0đ"
    }
}

fun formatDate(dateString: String): String {
    return try {
        if (dateString.contains("T")) {
            val parts = dateString.split("T")
            if (parts.isNotEmpty()) {
                parts[0]
            } else {
                dateString
            }
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}