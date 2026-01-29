package com.example.foodapp.pages.shipper.gps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shipper.gps.ShipperTrip
import com.example.foodapp.data.model.shipper.gps.TripStatus
import com.example.foodapp.pages.shipper.theme.ShipperColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Trip History Screen - View all past trips
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    onBack: () -> Unit = {},
    onNavigateToTripDetail: (String) -> Unit = {},
    viewModel: GpsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf<TripStatus?>(null) }
    
    // Load trips on first render
    LaunchedEffect(Unit) {
        viewModel.loadTrips()
    }
    
    // Reload when filter changes
    LaunchedEffect(selectedFilter) {
        viewModel.loadTrips(status = selectedFilter)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        // Filter chips
        FilterChipsRow(
            selectedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it }
        )
        
        // Trip list
        if (uiState.isLoadingTrips && uiState.trips.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ShipperColors.Primary)
            }
        } else if (uiState.trips.isEmpty()) {
            EmptyTripsContent()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total count
                item {
                    Text(
                        "${uiState.totalTrips} chuyến đi",
                        fontSize = 14.sp,
                        color = ShipperColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                items(uiState.trips) { trip ->
                    TripHistoryCard(
                        trip = trip,
                        onClick = { onNavigateToTripDetail(trip.id) }
                    )
                }
                
                // Load more
                if (uiState.hasMoreTrips) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoadingTrips) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = ShipperColors.Primary
                                )
                            } else {
                                TextButton(onClick = { viewModel.loadMoreTrips() }) {
                                    Text("Xem thêm", color = ShipperColors.Primary)
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: TripStatus?,
    onFilterChange: (TripStatus?) -> Unit
) {
    val filters = listOf(
        null to "Tất cả",
        TripStatus.PENDING to "Chờ bắt đầu",
        TripStatus.STARTED to "Đang giao",
        TripStatus.FINISHED to "Hoàn thành",
        TripStatus.CANCELLED to "Đã hủy"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (status, label) ->
            FilterChip(
                selected = selectedFilter == status,
                onClick = { onFilterChange(status) },
                label = { Text(label, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ShipperColors.Primary,
                    selectedLabelColor = ShipperColors.OnPrimary
                )
            )
        }
    }
}

@Composable
private fun TripHistoryCard(
    trip: ShipperTrip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
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
                // Trip ID (shortened)
                Text(
                    "#${trip.id.takeLast(8).uppercase()}",
                    fontWeight = FontWeight.SemiBold,
                    color = ShipperColors.TextPrimary
                )
                
                // Status badge
                TripStatusChip(status = trip.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Buildings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ShipperColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${trip.totalBuildings} điểm",
                        fontSize = 13.sp,
                        color = ShipperColors.TextSecondary
                    )
                }
                
                // Orders
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ShipperColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${trip.totalOrders} đơn",
                        fontSize = 13.sp,
                        color = ShipperColors.TextSecondary
                    )
                }
                
                // Distance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Straighten,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ShipperColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        trip.getFormattedDistance(),
                        fontSize = 13.sp,
                        color = ShipperColors.TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Divider(color = ShipperColors.Divider)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatDate(trip.createdAt),
                    fontSize = 12.sp,
                    color = ShipperColors.TextTertiary
                )
                
                // Show additional timestamp based on status
                when (trip.status) {
                    TripStatus.STARTED -> trip.startedAt?.let {
                        Text(
                            "Bắt đầu: ${formatTime(it)}",
                            fontSize = 12.sp,
                            color = ShipperColors.TextTertiary
                        )
                    }
                    TripStatus.FINISHED -> trip.finishedAt?.let {
                        Text(
                            "Hoàn thành: ${formatTime(it)}",
                            fontSize = 12.sp,
                            color = ShipperColors.TextTertiary
                        )
                    }
                    TripStatus.CANCELLED -> trip.cancelledAt?.let {
                        Text(
                            "Hủy: ${formatTime(it)}",
                            fontSize = 12.sp,
                            color = ShipperColors.TextTertiary
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun TripStatusChip(status: TripStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TripStatus.PENDING -> Triple(ShipperColors.WarningLight, ShipperColors.Warning, "Chờ")
        TripStatus.STARTED -> Triple(ShipperColors.SuccessLight, ShipperColors.Success, "Đang giao")
        TripStatus.FINISHED -> Triple(ShipperColors.InfoLight, ShipperColors.Info, "Hoàn thành")
        TripStatus.CANCELLED -> Triple(ShipperColors.ErrorLight, ShipperColors.Error, "Đã hủy")
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun EmptyTripsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Route,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ShipperColors.TextTertiary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Chưa có chuyến đi",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShipperColors.TextSecondary
        )
        
        Text(
            "Các chuyến đi của bạn sẽ xuất hiện ở đây",
            fontSize = 14.sp,
            color = ShipperColors.TextTertiary
        )
    }
}

// Helper functions
private fun formatDate(isoString: String?): String {
    if (isoString == null) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(isoString.substringBefore("."))
        date?.let { outputFormat.format(it) } ?: isoString
    } catch (e: Exception) {
        isoString
    }
}

private fun formatTime(isoString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(isoString.substringBefore("."))
        date?.let { outputFormat.format(it) } ?: isoString
    } catch (e: Exception) {
        isoString
    }
}
