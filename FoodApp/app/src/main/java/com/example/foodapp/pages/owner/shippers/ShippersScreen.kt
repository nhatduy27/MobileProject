package com.example.foodapp.pages.owner.shippers

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.owner.shipper.ApplicationStatus
import com.example.foodapp.data.model.owner.removal.OwnerRemovalRequest
import com.example.foodapp.data.model.owner.removal.OwnerRemovalRequestStatus
import com.example.foodapp.data.model.owner.removal.OwnerRemovalRequestType
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.example.foodapp.R

/**
 * Màn hình quản lý Shipper
 * 3 tabs: Applications (Đơn xin làm shipper), Active Shippers (Shipper đang hoạt động), Removal Requests (Yêu cầu rời shop)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippersScreen(
    onMenuClick: () -> Unit,
    shopId: String? = null,
    viewModel: ShippersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = remember(uiState.applications, uiState.shippers, uiState.removalRequests) {
        viewModel.getStats()
    }
    
    // Set shopId khi có
    LaunchedEffect(shopId) {
        shopId?.let { viewModel.setShopId(it) }
    }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Show messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSuccess()
        }
    }
    
    // Reject Dialog
    if (uiState.showRejectDialog) {
        RejectRemovalRequestDialog(
            reason = uiState.rejectionReason,
            onReasonChange = viewModel::onRejectionReasonChanged,
            onDismiss = viewModel::dismissRejectDialog,
            onConfirm = viewModel::rejectRemovalRequest,
            isProcessing = uiState.isProcessing
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ShippersHeader(
                onMenuClick = onMenuClick,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChanged,
                onRefresh = { viewModel.refreshCurrentTab() }
            )
        },
        containerColor = OwnerColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs - 3 tabs now
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = OwnerColors.Surface,
                contentColor = OwnerColors.Primary,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(R.string.shippers_tab_applications),
                                fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                            if (stats.pendingApplications > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Badge(
                                    containerColor = OwnerColors.Primary,
                                    contentColor = OwnerColors.Surface
                                ) {
                                    Text("${stats.pendingApplications}")
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = {
                        Text(
                            stringResource(R.string.shippers_tab_shippers),
                            fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.onTabSelected(2) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(R.string.shippers_tab_removal),
                                fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                            if (stats.pendingRemovalRequests > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Badge(
                                    containerColor = OwnerColors.Warning,
                                    contentColor = OwnerColors.Surface
                                ) {
                                    Text("${stats.pendingRemovalRequests}")
                                }
                            }
                        }
                    }
                )
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && (uiState.applications.isEmpty() && uiState.shippers.isEmpty() && uiState.removalRequests.isEmpty())) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = OwnerColors.Primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.general_loading), color = OwnerColors.TextSecondary)
                        }
                    }
                } else {
                    when (uiState.selectedTab) {
                        0 -> ApplicationsTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            stats = stats
                        )
                        1 -> ShippersTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            stats = stats
                        )
                        2 -> RemovalRequestsTab(
                            viewModel = viewModel,
                            uiState = uiState,
                            stats = stats
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationsTab(
    viewModel: ShippersViewModel,
    uiState: ShipperUiState,
    stats: ShipperStats
) {
    val filteredApplications = remember(uiState.applications, uiState.searchQuery) {
        viewModel.getFilteredApplications()
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.loadApplications(refresh = true) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = stringResource(R.string.shippers_stat_total_applications),
                        value = stats.totalApplications.toString(),
                        color = OwnerColors.Primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = stringResource(R.string.shippers_stat_pending),
                        value = stats.pendingApplications.toString(),
                        color = OwnerColors.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipStatus(
                        label = stringResource(R.string.shippers_filter_all),
                        isSelected = uiState.selectedApplicationStatus == null,
                        onClick = { viewModel.onApplicationStatusFilterChanged(null) }
                    )
                    FilterChipStatus(
                        label = stringResource(R.string.shippers_filter_pending),
                        isSelected = uiState.selectedApplicationStatus == ApplicationStatus.PENDING,
                        onClick = { viewModel.onApplicationStatusFilterChanged(ApplicationStatus.PENDING) }
                    )
                    FilterChipStatus(
                        label = stringResource(R.string.shippers_filter_approved),
                        isSelected = uiState.selectedApplicationStatus == ApplicationStatus.APPROVED,
                        onClick = { viewModel.onApplicationStatusFilterChanged(ApplicationStatus.APPROVED) }
                    )
                    FilterChipStatus(
                        label = stringResource(R.string.shippers_filter_rejected),
                        isSelected = uiState.selectedApplicationStatus == ApplicationStatus.REJECTED,
                        onClick = { viewModel.onApplicationStatusFilterChanged(ApplicationStatus.REJECTED) }
                    )
                }
            }

            // Empty state
            if (filteredApplications.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = OwnerColors.BorderLight
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty())
                                    stringResource(R.string.shippers_empty_no_applications_search)
                                else
                                    stringResource(R.string.shippers_empty_no_applications),
                                fontSize = 16.sp,
                                color = OwnerColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // Applications list
            items(filteredApplications, key = { it.id }) { application ->
                ApplicationItem(
                    application = application,
                    onApprove = { viewModel.approveApplication(application.id) },
                    onReject = { reason -> viewModel.rejectApplication(application.id, reason) },
                    isProcessing = uiState.isProcessing
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShippersTab(
    viewModel: ShippersViewModel,
    uiState: ShipperUiState,
    stats: ShipperStats
) {
    val filteredShippers = remember(uiState.shippers, uiState.searchQuery) {
        viewModel.getFilteredShippers()
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.loadShippers(refresh = true) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = stringResource(R.string.shippers_stat_total_shippers),
                        value = stats.totalShippers.toString(),
                        color = OwnerColors.Success,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = stringResource(R.string.shippers_stat_available),
                        value = stats.availableShippers.toString(),
                        color = OwnerColors.Info,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = stringResource(R.string.shippers_stat_delivery),
                        value = stats.busyShippers.toString(),
                        color = OwnerColors.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Empty state
            if (filteredShippers.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.DeliveryDining,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = OwnerColors.BorderLight
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty())
                                    stringResource(R.string.shippers_empty_no_shippers_search)
                                else
                                    stringResource(R.string.shippers_empty_no_shippers),
                                fontSize = 16.sp,
                                color = OwnerColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // Shippers list
            items(filteredShippers, key = { it.id }) { shipper ->
                ShipperCard(
                    shipper = shipper,
                    onRemove = { viewModel.removeShipper(shipper.id) },
                    isProcessing = uiState.isProcessing
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                fontSize = 12.sp,
                color = OwnerColors.TextSecondary
            )
        }
    }
}

@Composable
private fun FilterChipStatus(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = OwnerColors.Primary,
            selectedLabelColor = OwnerColors.Surface,
            containerColor = OwnerColors.Surface,
            labelColor = OwnerColors.TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) OwnerColors.Primary else OwnerColors.Divider,
            selectedBorderColor = OwnerColors.Primary
        )
    )
}

// ==================== REMOVAL REQUESTS TAB ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemovalRequestsTab(
    viewModel: ShippersViewModel,
    uiState: ShipperUiState,
    stats: ShipperStats
) {
    val filteredRequests = remember(uiState.removalRequests, uiState.searchQuery) {
        viewModel.getFilteredRemovalRequests()
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.loadRemovalRequests(refresh = true) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        title = stringResource(R.string.shippers_stat_total_requests),
                        value = stats.totalRemovalRequests.toString(),
                        color = OwnerColors.Info,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = stringResource(R.string.shippers_stat_pending_requests),
                        value = stats.pendingRemovalRequests.toString(),
                        color = OwnerColors.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipStatus(
                        label = stringResource(R.string.shippers_filter_all),
                        isSelected = uiState.selectedRemovalStatus == null,
                        onClick = { viewModel.onRemovalStatusFilterChanged(null) }
                    )
                    FilterChipStatus(
                        label = stringResource(R.string.shippers_filter_pending_process),
                        isSelected = uiState.selectedRemovalStatus == OwnerRemovalRequestStatus.PENDING,
                        onClick = { viewModel.onRemovalStatusFilterChanged(OwnerRemovalRequestStatus.PENDING) }
                    )
                    FilterChipStatus(
                        label = stringResource(R.string.shippers_filter_approved),
                        isSelected = uiState.selectedRemovalStatus == OwnerRemovalRequestStatus.APPROVED,
                        onClick = { viewModel.onRemovalStatusFilterChanged(OwnerRemovalRequestStatus.APPROVED) }
                    )
                    FilterChipStatus(
                        label = stringResource(R.string.shippers_filter_rejected),
                        isSelected = uiState.selectedRemovalStatus == OwnerRemovalRequestStatus.REJECTED,
                        onClick = { viewModel.onRemovalStatusFilterChanged(OwnerRemovalRequestStatus.REJECTED) }
                    )
                }
            }

            // Empty state
            if (filteredRequests.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = OwnerColors.BorderLight
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty())
                                    stringResource(R.string.shippers_empty_no_requests_search)
                                else
                                    stringResource(R.string.shippers_empty_no_requests),
                                fontSize = 16.sp,
                                color = OwnerColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // Removal requests list
            items(filteredRequests, key = { it.id }) { request ->
                RemovalRequestCard(
                    request = request,
                    onApprove = { viewModel.approveRemovalRequest(request.id) },
                    onReject = { viewModel.showRejectRemovalDialog(request.id) },
                    isProcessing = uiState.isProcessing
                )
            }
        }
    }
}

@Composable
private fun RemovalRequestCard(
    request: OwnerRemovalRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isProcessing: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shipper info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar placeholder
                    Surface(
                        shape = CircleShape,
                        color = OwnerColors.Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = OwnerColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            request.shipperName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = OwnerColors.TextPrimary
                        )
                        request.shipperPhone?.let { phone ->
                            Text(
                                phone,
                                fontSize = 13.sp,
                                color = OwnerColors.TextSecondary
                            )
                        }
                    }
                }
                
                // Status badge
                RemovalStatusBadge(status = request.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Type row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (request.type) {
                        OwnerRemovalRequestType.QUIT -> Icons.Outlined.ExitToApp
                        OwnerRemovalRequestType.TRANSFER -> Icons.Outlined.SwapHoriz
                    },
                    contentDescription = null,
                    tint = OwnerColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    request.getTypeDisplayName(),
                    fontSize = 14.sp,
                    color = OwnerColors.TextSecondary
                )
            }
            
            // Reason
            if (!request.reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = OwnerColors.Background),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Lý do: ${request.reason}",
                        modifier = Modifier.padding(10.dp),
                        fontSize = 13.sp,
                        color = OwnerColors.TextSecondary
                    )
                }
            }
            
            // Rejection reason (if rejected)
            if (request.status == OwnerRemovalRequestStatus.REJECTED && !request.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = OwnerColors.Error.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Lý do từ chối: ${request.rejectionReason}",
                        modifier = Modifier.padding(10.dp),
                        fontSize = 13.sp,
                        color = OwnerColors.Error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date
            Text(
                "Ngày tạo: ${formatRemovalDate(request.createdAt)}",
                fontSize = 12.sp,
                color = OwnerColors.TextTertiary
            )
            
            // Action buttons (only for pending)
            if (request.status == OwnerRemovalRequestStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = OwnerColors.Divider)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OwnerColors.Error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (!isProcessing) OwnerColors.Error else OwnerColors.Error.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.shippers_btn_reject))
                    }
                    
                    Button(
                        onClick = onApprove,
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OwnerColors.Success
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.shippers_btn_accept))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemovalStatusBadge(status: OwnerRemovalRequestStatus) {
    val text = when (status) {
        OwnerRemovalRequestStatus.PENDING -> stringResource(R.string.shippers_status_pending)
        OwnerRemovalRequestStatus.APPROVED -> stringResource(R.string.shippers_status_approved)
        OwnerRemovalRequestStatus.REJECTED -> stringResource(R.string.shippers_status_rejected)
    }
    val (backgroundColor, textColor) = when (status) {
        OwnerRemovalRequestStatus.PENDING -> Pair(
            OwnerColors.Warning.copy(alpha = 0.15f),
            OwnerColors.Warning
        )
        OwnerRemovalRequestStatus.APPROVED -> Pair(
            OwnerColors.Success.copy(alpha = 0.15f),
            OwnerColors.Success
        )
        OwnerRemovalRequestStatus.REJECTED -> Pair(
            OwnerColors.Error.copy(alpha = 0.15f),
            OwnerColors.Error
        )
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun RejectRemovalRequestDialog(
    reason: String,
    onReasonChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isProcessing: Boolean
) {
    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = { 
            Text(
                "Từ chối yêu cầu rời shop",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Vui lòng nhập lý do từ chối yêu cầu rời shop của shipper:",
                    color = OwnerColors.TextSecondary
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text("Lý do từ chối") },
                    placeholder = { Text("Nhập lý do...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isProcessing
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isProcessing && reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Error)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Từ chối")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("Hủy")
            }
        }
    )
}

// Helper function
private fun formatRemovalDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.take(10)
    }
}
