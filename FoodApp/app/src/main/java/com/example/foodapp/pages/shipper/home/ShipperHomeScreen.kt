package com.example.foodapp.pages.shipper.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import com.example.foodapp.pages.shipper.theme.ShipperColors
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipperHomeScreen(
    onOrderClick: (String) -> Unit = {},
    onApplyShipper: () -> Unit = {},
    viewModel: ShipperHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.onlineStatusMessage) {
        uiState.onlineStatusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearOnlineStatusMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        if (uiState.isNotAssignedToShop) {
            NotAssignedToShopContent(
                onRefresh = { viewModel.loadData() },
                onApplyClick = onApplyShipper
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Tabs with new style
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = ShipperColors.Surface,
                    contentColor = ShipperColors.Primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color = ShipperColors.Primary,
                            height = 3.dp
                        )
                    },
                    divider = { HorizontalDivider(color = ShipperColors.Divider) }
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.onTabSelected(0) },
                        selectedContentColor = ShipperColors.Primary,
                        unselectedContentColor = ShipperColors.TextSecondary,
                        text = { 
                            Text(
                                "Đơn mới (${uiState.availableOrders.size})", 
                                fontWeight = if(uiState.selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                            ) 
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.onTabSelected(1) },
                        selectedContentColor = ShipperColors.Primary,
                        unselectedContentColor = ShipperColors.TextSecondary,
                        text = { 
                            Text(
                                "Đơn của tôi (${uiState.myOrders.size})",
                                fontWeight = if(uiState.selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                            ) 
                        }
                    )
                }
                
                // Content
                if (uiState.selectedTab == 0) {
                    OrdersList(
                        orders = uiState.availableOrders,
                        isLoading = uiState.isLoadingAvailable,
                        onRefresh = { viewModel.loadAvailableOrders() },
                        onAccept = { order -> viewModel.acceptOrder(order.id) },
                        onViewDetail = { order -> onOrderClick(order.id) }
                    )
                } else {
                    OrdersList(
                        orders = uiState.myOrders,
                        isLoading = uiState.isLoadingMyOrders,
                        onRefresh = { viewModel.loadMyOrders() },
                        onAccept = { },
                        onViewDetail = { order -> onOrderClick(order.id) },
                        isMyOrder = true
                    )
                }
            }
        }
    }
}

@Composable
fun NotAssignedToShopContent(
    onRefresh: () -> Unit,
    onApplyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = ShipperColors.Surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon background
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = ShipperColors.PrimaryLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Store,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = ShipperColors.Primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Chưa đăng ký cửa hàng",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ShipperColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Bạn cần đăng ký làm shipper cho một cửa hàng để có thể nhận và giao đơn hàng.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = ShipperColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                Button(
                    onClick = onApplyClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShipperColors.Primary
                    )
                ) {
                    Text("Đăng ký cửa hàng", fontWeight = FontWeight.SemiBold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ShipperColors.TextSecondary
                    )
                ) {
                    Text("Kiểm tra lại")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Nếu bạn đã gửi đơn, vui lòng đợi chủ cửa hàng phê duyệt.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = ShipperColors.TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersList(
    orders: List<ShipperOrder>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onAccept: (ShipperOrder) -> Unit,
    onViewDetail: (ShipperOrder) -> Unit,
    isMyOrder: Boolean = false
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (orders.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassEmpty,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = ShipperColors.TextTertiary
                    )
                    Text(
                        text = if (isMyOrder) "Bạn chưa nhận đơn nào" else "Không có đơn hàng mới",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ShipperColors.TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders) { order ->
                    ShipperOrderCard(
                        order = order,
                        onAccept = { onAccept(order) },
                        onClick = { onViewDetail(order) },
                        showAcceptButton = !isMyOrder
                    )
                }
            }
        }
    }
}
