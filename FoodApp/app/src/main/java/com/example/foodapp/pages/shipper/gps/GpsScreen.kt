package com.example.foodapp.pages.shipper.gps

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import com.example.foodapp.pages.shipper.theme.ShipperColors

/**
 * GPS Main Screen - Create and manage delivery trips
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsScreen(
    onNavigateToTripDetail: (String) -> Unit = {},
    onNavigateToTripHistory: () -> Unit = {},
    viewModel: GpsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Handle success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }
    
    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, "Lỗi: $message", Toast.LENGTH_SHORT).show()
            viewModel.clearErrorMessage()
        }
    }
    
    // Navigate to trip detail when trip is created
    LaunchedEffect(uiState.navigateToTripDetail) {
        uiState.navigateToTripDetail?.let { tripId ->
            onNavigateToTripDetail(tripId)
            viewModel.clearNavigation()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        // Active Trip Banner (if exists)
        uiState.currentTrip?.let { trip ->
            ActiveTripBanner(
                trip = trip,
                onClick = { onNavigateToTripDetail(trip.id) }
            )
        }
        
        // Header with actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Lộ trình giao hàng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShipperColors.TextPrimary
                )
                Text(
                    "Quản lý đơn hàng và lộ trình",
                    fontSize = 13.sp,
                    color = ShipperColors.TextSecondary
                )
            }
            
            // History button
            IconButton(onClick = onNavigateToTripHistory) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = "Lịch sử chuyến",
                    tint = ShipperColors.Primary
                )
            }
        }
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ShipperColors.Surface,
            contentColor = ShipperColors.Primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { 
                    Text(
                        "Chờ giao (${uiState.availableOrders.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { 
                    Text(
                        "Đang giao (${uiState.shippingOrders.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
        
        // Selection summary and actions (only for ready orders tab)
        if (selectedTab == 0 && uiState.selectedOrderIds.isNotEmpty()) {
            SelectionSummaryCard(
                selectedCount = uiState.selectedOrdersCount,
                maxCount = 15,
                onClear = { viewModel.clearSelection() },
                onCreateTrip = { viewModel.createTrip() },
                isCreating = uiState.isCreatingTrip
            )
        }
        
        // Orders list
        if (uiState.isLoadingOrders) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ShipperColors.Primary)
            }
        } else {
            when (selectedTab) {
                0 -> {
                    // Ready orders - for creating trips
                    if (uiState.availableOrders.isEmpty()) {
                        EmptyOrdersContent(
                            message = "Chưa có đơn hàng sẵn sàng để giao",
                            onRefresh = { viewModel.loadAvailableOrders() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                SelectAllRow(
                                    totalOrders = uiState.availableOrders.size,
                                    selectedCount = uiState.selectedOrdersCount,
                                    onSelectAll = { viewModel.selectAllOrders() },
                                    onClearAll = { viewModel.clearSelection() }
                                )
                            }
                            
                            items(uiState.availableOrders) { order ->
                                SelectableOrderCard(
                                    order = order,
                                    isSelected = uiState.selectedOrderIds.contains(order.id),
                                    onToggle = { viewModel.toggleOrderSelection(order.id) }
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
                1 -> {
                    // Shipping orders - already accepted and being delivered
                    if (uiState.shippingOrders.isEmpty()) {
                        EmptyOrdersContent(
                            message = "Chưa có đơn hàng đang giao",
                            onRefresh = { viewModel.loadShippingOrders() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    "${uiState.shippingOrders.size} đơn đang giao",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ShipperColors.TextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(uiState.shippingOrders) { order ->
                                ShippingOrderCard(order = order)
                            }
                            
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShippingOrderCard(order: ShipperOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Surface(
                shape = CircleShape,
                color = ShipperColors.SuccessLight,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = ShipperColors.Success,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Order info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "#${order.orderNumber}",
                        fontWeight = FontWeight.SemiBold,
                        color = ShipperColors.TextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ShipperColors.SuccessLight
                    ) {
                        Text(
                            "Đang giao",
                            fontSize = 11.sp,
                            color = ShipperColors.Success,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    order.customer?.displayName ?: order.customerSnapshot?.displayName ?: "Khách hàng",
                    fontSize = 13.sp,
                    color = ShipperColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ShipperColors.Primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        order.deliveryAddress?.buildingCode 
                            ?: order.deliveryAddress?.building 
                            ?: order.deliveryAddress?.fullAddress 
                            ?: "",
                        fontSize = 12.sp,
                        color = ShipperColors.Primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTripBanner(
    trip: com.example.foodapp.data.model.shipper.gps.ShipperTrip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (trip.status) {
                com.example.foodapp.data.model.shipper.gps.TripStatus.STARTED -> ShipperColors.SuccessLight
                com.example.foodapp.data.model.shipper.gps.TripStatus.PENDING -> ShipperColors.WarningLight
                else -> ShipperColors.Surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = when (trip.status) {
                    com.example.foodapp.data.model.shipper.gps.TripStatus.STARTED -> ShipperColors.Success
                    com.example.foodapp.data.model.shipper.gps.TripStatus.PENDING -> ShipperColors.Warning
                    else -> ShipperColors.TextSecondary
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Route,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when (trip.status) {
                        com.example.foodapp.data.model.shipper.gps.TripStatus.STARTED -> "Đang giao hàng"
                        com.example.foodapp.data.model.shipper.gps.TripStatus.PENDING -> "Chuyến đang chờ"
                        else -> "Chuyến đi"
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = ShipperColors.TextPrimary
                )
                Text(
                    "${trip.totalBuildings} điểm • ${trip.totalOrders} đơn • ${trip.getFormattedDistance()}",
                    fontSize = 13.sp,
                    color = ShipperColors.TextSecondary
                )
            }
            
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = ShipperColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SelectionSummaryCard(
    selectedCount: Int,
    maxCount: Int,
    onClear: () -> Unit,
    onCreateTrip: () -> Unit,
    isCreating: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.PrimaryLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = ShipperColors.Primary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$selectedCount",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Đã chọn ($selectedCount/$maxCount)",
                    fontWeight = FontWeight.Medium,
                    color = ShipperColors.Primary
                )
            }
            
            Row {
                TextButton(onClick = onClear) {
                    Text("Xóa", color = ShipperColors.TextSecondary)
                }
                
                Button(
                    onClick = onCreateTrip,
                    enabled = !isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.Route, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tạo lộ trình")
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectAllRow(
    totalOrders: Int,
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$totalOrders đơn hàng sẵn sàng",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ShipperColors.TextSecondary
        )
        
        TextButton(
            onClick = if (selectedCount == 0) onSelectAll else onClearAll
        ) {
            Text(
                if (selectedCount == 0) "Chọn tất cả" else "Bỏ chọn",
                color = ShipperColors.Primary
            )
        }
    }
}

@Composable
private fun SelectableOrderCard(
    order: ShipperOrder,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, ShipperColors.Primary, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ShipperColors.PrimaryLight else ShipperColors.Surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) ShipperColors.Primary else Color.Transparent
                    )
                    .border(
                        2.dp,
                        if (isSelected) ShipperColors.Primary else ShipperColors.TextTertiary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Order info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "#${order.orderNumber}",
                        fontWeight = FontWeight.SemiBold,
                        color = ShipperColors.TextPrimary
                    )
                    Text(
                        order.deliveryAddress?.buildingCode 
                            ?: order.deliveryAddress?.building 
                            ?: "",
                        fontWeight = FontWeight.Bold,
                        color = ShipperColors.Primary,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    order.customer?.displayName ?: order.customerSnapshot?.displayName ?: "Khách hàng",
                    fontSize = 13.sp,
                    color = ShipperColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    order.deliveryAddress?.fullAddress ?: "",
                    fontSize = 12.sp,
                    color = ShipperColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyOrdersContent(
    message: String = "Chưa có đơn hàng sẵn sàng để giao",
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Inventory2,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ShipperColors.TextTertiary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Không có đơn hàng",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShipperColors.TextSecondary
        )
        
        Text(
            message,
            fontSize = 14.sp,
            color = ShipperColors.TextTertiary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = onRefresh,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ShipperColors.Primary)
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Làm mới")
        }
    }
}
