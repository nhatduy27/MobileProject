package com.example.foodapp.pages.shipper.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shipper.order.ShipperOrder
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

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        // Nếu shipper chưa được gán vào shop, hiển thị thông báo
        if (uiState.isNotAssignedToShop) {
            NotAssignedToShopContent(
                onRefresh = { viewModel.loadData() },
                onApplyClick = onApplyShipper,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tabs
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.onTabSelected(0) },
                        text = { 
                            Text(
                                "Đơn mới (${uiState.availableOrders.size})", 
                                fontWeight = if(uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.onTabSelected(1) },
                        text = { 
                            Text(
                                "Đơn của tôi (${uiState.myOrders.size})",
                                fontWeight = if(uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                }
                
                // Content
                Box(modifier = Modifier.fillMaxSize()) {
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
                    
                    if (uiState.isLoadingAvailable || uiState.isLoadingMyOrders) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

/**
 * Composable hiển thị khi shipper chưa được gán vào shop
 */
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
                containerColor = Color(0xFFFFF3E0) // Light orange
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFFF9800)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Chưa đăng ký cửa hàng",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Bạn cần đăng ký làm shipper cho một cửa hàng để có thể nhận và giao đơn hàng.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF795548)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onApplyClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đăng ký cửa hàng")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                Text(
                    text = if (isMyOrder) "Bạn chưa nhận đơn nào" else "Không có đơn hàng mới",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(orders) { order ->
                    ShipperOrderCard(
                        order = order,
                        onAccept = { onAccept(order) },
                        onClick = { onViewDetail(order) }
                    )
                }
            }
        }
    }
}
