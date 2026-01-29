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
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

/**
 * Màn hình quản lý Shipper
 * 2 tabs: Applications (Đơn xin làm shipper) và Active Shippers (Shipper đang hoạt động)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippersScreen(
    onMenuClick: () -> Unit,
    viewModel: ShippersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = remember(uiState.applications, uiState.shippers) {
        viewModel.getStats()
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
            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = OwnerColors.Surface,
                contentColor = OwnerColors.Primary
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Đơn xin làm shipper",
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
                            "Shipper hoạt động",
                            fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading && (uiState.applications.isEmpty() && uiState.shippers.isEmpty())) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = OwnerColors.Primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Đang tải...", color = OwnerColors.TextSecondary)
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
                        title = "Tổng đơn",
                        value = stats.totalApplications.toString(),
                        color = OwnerColors.Primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Chờ duyệt",
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
                        label = "Tất cả",
                        isSelected = uiState.selectedApplicationStatus == null,
                        onClick = { viewModel.onApplicationStatusFilterChanged(null) }
                    )
                    FilterChipStatus(
                        label = "Chờ duyệt",
                        isSelected = uiState.selectedApplicationStatus == ApplicationStatus.PENDING,
                        onClick = { viewModel.onApplicationStatusFilterChanged(ApplicationStatus.PENDING) }
                    )
                    FilterChipStatus(
                        label = "Đã duyệt",
                        isSelected = uiState.selectedApplicationStatus == ApplicationStatus.APPROVED,
                        onClick = { viewModel.onApplicationStatusFilterChanged(ApplicationStatus.APPROVED) }
                    )
                    FilterChipStatus(
                        label = "Đã từ chối",
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
                                    "Không tìm thấy đơn nào"
                                else
                                    "Chưa có đơn xin làm shipper",
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
                        title = "Tổng shipper",
                        value = stats.totalShippers.toString(),
                        color = OwnerColors.Success,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Sẵn sàng",
                        value = stats.availableShippers.toString(),
                        color = OwnerColors.Info,
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Đang giao",
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
                                    "Không tìm thấy shipper nào"
                                else
                                    "Chưa có shipper nào",
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
