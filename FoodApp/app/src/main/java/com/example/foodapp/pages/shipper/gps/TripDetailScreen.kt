package com.example.foodapp.pages.shipper.gps

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shipper.gps.ShipperTrip
import com.example.foodapp.data.model.shipper.gps.TripStatus
import com.example.foodapp.data.model.shipper.gps.TripWaypoint
import com.example.foodapp.pages.shipper.theme.ShipperColors
import com.example.foodapp.R

/**
 * Trip Detail Screen - View and manage active trip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onBack: () -> Unit = {},
    onNavigateToMap: (String) -> Unit = {},
    viewModel: GpsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    
    // Load trip on first render
    LaunchedEffect(tripId) {
        viewModel.loadTrip(tripId)
    }
    
    // Handle navigation to map after starting trip
    LaunchedEffect(uiState.navigateToMap) {
        uiState.navigateToMap?.let { mapTripId ->
            onNavigateToMap(mapTripId)
            viewModel.clearMapNavigation()
        }
    }
    
    // Handle messages and navigation
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
            
            // Navigate back when trip is completed or cancelled
            if (message.contains("Hoàn thành") || message.contains("hủy")) {
                onBack()
            }
        }
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, context.getString(R.string.shipper_trip_error_prefix, message), Toast.LENGTH_SHORT).show()
            viewModel.clearErrorMessage()
        }
    }
    
    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.shipper_trip_cancel_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.shipper_trip_cancel_dialog_message))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text(stringResource(R.string.shipper_trip_cancel_reason_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelTrip(cancelReason.takeIf { it.isNotBlank() })
                        showCancelDialog = false
                        cancelReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Error)
                ) {
                    Text(stringResource(R.string.shipper_trip_cancel))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.shipper_close))
                }
            }
        )
    }
    
    val trip = uiState.currentTrip
    
    if (trip == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ShipperColors.Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ShipperColors.Primary)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ShipperColors.Background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trip Summary Card
            item {
                TripSummaryCard(trip = trip)
            }
            
            // Route Progress
            item {
                Text(
                    stringResource(R.string.shipper_trip_route, trip.waypoints.size),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = ShipperColors.TextPrimary
                )
            }
            
            // Waypoints list
            itemsIndexed(trip.waypoints) { index, waypoint ->
                WaypointCard(
                    index = index,
                    waypoint = waypoint,
                    ordersCount = trip.getOrdersForStop(waypoint.order).size,
                    isFirst = index == 0,
                    isLast = index == trip.waypoints.size - 1,
                    tripStatus = trip.status
                )
            }
            
            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TripActionButtons(
                    trip = trip,
                    isStarting = uiState.isStartingTrip,
                    isFinishing = uiState.isFinishingTrip,
                    isCancelling = uiState.isCancellingTrip,
                    onStart = { viewModel.startTrip() },
                    onFinish = { viewModel.finishTrip() },
                    onCancel = { showCancelDialog = true }
                )
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun TripSummaryCard(trip: ShipperTrip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.shipper_trip_info),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ShipperColors.TextPrimary
                )
                
                TripStatusBadge(status = trip.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Outlined.LocationOn,
                    value = "${trip.totalBuildings}",
                    label = stringResource(R.string.shipper_trip_stops)
                )
                StatItem(
                    icon = Icons.Outlined.ShoppingBag,
                    value = "${trip.totalOrders}",
                    label = stringResource(R.string.shipper_trip_orders)
                )
                StatItem(
                    icon = Icons.Outlined.Straighten,
                    value = trip.getFormattedDistance(),
                    label = stringResource(R.string.shipper_trip_distance)
                )
                StatItem(
                    icon = Icons.Outlined.Schedule,
                    value = trip.getFormattedDuration(),
                    label = stringResource(R.string.shipper_trip_estimated)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = ShipperColors.Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = ShipperColors.TextPrimary
        )
        Text(
            label,
            fontSize = 12.sp,
            color = ShipperColors.TextSecondary
        )
    }
}

@Composable
private fun TripStatusBadge(status: TripStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TripStatus.PENDING -> Triple(ShipperColors.WarningLight, ShipperColors.Warning, stringResource(R.string.shipper_trip_status_pending))
        TripStatus.STARTED -> Triple(ShipperColors.SuccessLight, ShipperColors.Success, stringResource(R.string.shipper_trip_status_started))
        TripStatus.FINISHED -> Triple(ShipperColors.InfoLight, ShipperColors.Info, stringResource(R.string.shipper_trip_status_finished))
        TripStatus.CANCELLED -> Triple(ShipperColors.ErrorLight, ShipperColors.Error, stringResource(R.string.shipper_trip_status_cancelled))
    }
    
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun WaypointCard(
    index: Int,
    waypoint: TripWaypoint,
    ordersCount: Int,
    isFirst: Boolean,
    isLast: Boolean,
    tripStatus: TripStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(ShipperColors.Divider)
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // Stop number circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            tripStatus == TripStatus.FINISHED -> ShipperColors.Success
                            tripStatus == TripStatus.STARTED && index == 0 -> ShipperColors.Primary
                            else -> ShipperColors.TextTertiary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(ShipperColors.Divider)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Waypoint info
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.shipper_trip_building, waypoint.buildingCode),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = ShipperColors.TextPrimary
                    )
                    Text(
                        waypoint.location.name ?: stringResource(R.string.shipper_trip_point, index + 1),
                        fontSize = 13.sp,
                        color = ShipperColors.TextSecondary
                    )
                }
                
                // Orders count badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ShipperColors.PrimaryLight
                ) {
                    Text(
                        stringResource(R.string.shipper_trip_orders_count, ordersCount),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = ShipperColors.Primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TripActionButtons(
    trip: ShipperTrip,
    isStarting: Boolean,
    isFinishing: Boolean,
    isCancelling: Boolean,
    onStart: () -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (trip.status) {
            TripStatus.PENDING -> {
                // Start button
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isStarting,
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Success),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isStarting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.shipper_trip_start_delivery), fontWeight = FontWeight.SemiBold)
                    }
                }
                
                // Cancel button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isCancelling,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ShipperColors.Error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ShipperColors.Error,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.shipper_trip_cancel), fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            TripStatus.STARTED -> {
                // Finish button
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isFinishing,
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isFinishing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.shipper_trip_finish), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            TripStatus.FINISHED -> {
                // Completion summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ShipperColors.SuccessLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = ShipperColors.Success,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.shipper_trip_completed),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ShipperColors.Success
                        )
                        Text(
                            stringResource(R.string.shipper_trip_delivered_count, trip.totalOrders),
                            color = ShipperColors.TextSecondary
                        )
                    }
                }
            }
            
            TripStatus.CANCELLED -> {
                // Cancellation info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ShipperColors.ErrorLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Cancel,
                            contentDescription = null,
                            tint = ShipperColors.Error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.shipper_trip_cancelled_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ShipperColors.Error
                        )
                        trip.cancelReason?.let { reason ->
                            Text(
                                stringResource(R.string.shipper_trip_cancel_reason, reason),
                                color = ShipperColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
