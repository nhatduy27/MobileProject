package com.example.foodapp.pages.shipper.gps

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodapp.data.model.shipper.gps.ShipperTrip
import com.example.foodapp.data.model.shipper.gps.TripLocation
import com.example.foodapp.data.model.shipper.gps.TripStatus
import com.example.foodapp.data.model.shipper.gps.TripWaypoint
import com.example.foodapp.pages.shipper.theme.ShipperColors
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*

/**
 * Delivery Map Screen - Real-time map tracking during delivery
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryMapScreen(
    tripId: String,
    isOrderId: Boolean = false,  // If true, tripId is actually an orderId
    onBack: () -> Unit = {},
    onFinish: () -> Unit = {},
    viewModel: GpsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var showFinishDialog by remember { mutableStateOf(false) }
    var isMapReady by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                               permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            viewModel.startLocationTracking(context)
        }
    }
    
    // Load trip - support both tripId and orderId
    // Always reload when tripId changes to ensure fresh data
    LaunchedEffect(tripId, isOrderId) {
        // Clear old trip first to ensure we show loading state
        viewModel.clearCurrentTrip()
        
        if (isOrderId) {
            viewModel.loadTripByOrderId(tripId)
        } else {
            viewModel.loadTrip(tripId)
        }
    }
    
    // Request permission and start tracking
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.startLocationTracking(context)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // Request location updates
    val currentLocation by viewModel.currentLocationState.collectAsState()
    
    // Handle messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
            // Navigate back after completing trip
            if (message.contains("Hoàn thành")) {
                onFinish()
            }
        }
    }
    
    // Finish confirmation dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Hoàn thành giao hàng?") },
            text = { 
                Text("Bạn đã giao hết ${uiState.currentTrip?.totalOrders} đơn hàng? " +
                     "Trạng thái các đơn sẽ được cập nhật thành 'Đã giao'.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishTrip()
                        showFinishDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Success)
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    val trip = uiState.currentTrip
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (trip == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ShipperColors.Background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ShipperColors.Primary)
            }
        } else {
            // Map View
            DeliveryMapContent(
                trip = trip,
                currentLocation = currentLocation,
                onMapReady = { isMapReady = true }
            )
            
            // Top Bar Overlay
            TopBarOverlay(
                trip = trip,
                onBack = onBack
            )
            
            // Bottom Control Panel
            BottomControlPanel(
                trip = trip,
                isFinishing = uiState.isFinishingTrip,
                currentLocation = currentLocation,
                onFinish = { showFinishDialog = true }
            )
        }
    }
}

@Composable
private fun DeliveryMapContent(
    trip: ShipperTrip,
    currentLocation: TripLocation?,
    onMapReady: () -> Unit
) {
    // Calculate bounds including all waypoints and current location
    val boundsBuilder = remember { LatLngBounds.builder() }
    
    // Add origin
    boundsBuilder.include(LatLng(trip.origin.lat, trip.origin.lng))
    
    // Add all waypoints
    trip.waypoints.forEach { waypoint ->
        boundsBuilder.include(LatLng(waypoint.location.lat, waypoint.location.lng))
    }
    
    // Add current location if available
    currentLocation?.let {
        boundsBuilder.include(LatLng(it.lat, it.lng))
    }
    
    val bounds = try {
        boundsBuilder.build()
    } catch (e: Exception) {
        // Fallback if no points
        LatLngBounds(
            LatLng(10.880, 106.780),
            LatLng(10.890, 106.790)
        )
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bounds.center, 16f)
    }
    
    // Decode polyline if available
    val routePoints = remember(trip.route.polyline) {
        trip.route.polyline?.let { encoded ->
            try {
                PolyUtil.decode(encoded)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }
    
    // Only auto-center once when trip is loaded (not when location changes)
    var hasInitialCentered by remember { mutableStateOf(false) }
    
    LaunchedEffect(trip.id, hasInitialCentered) {
        if (!hasInitialCentered && trip.waypoints.isNotEmpty()) {
            // Center on first waypoint (destination) on initial load
            val firstWaypoint = trip.waypoints.first()
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(firstWaypoint.location.lat, firstWaypoint.location.lng),
                    16f
                ),
                durationMs = 500
            )
            hasInitialCentered = true
        }
    }
    
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false, // We handle this manually
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        ),
        onMapLoaded = onMapReady
    ) {
        // Draw route polyline
        if (routePoints.isNotEmpty()) {
            Polyline(
                points = routePoints,
                color = ShipperColors.Primary,
                width = 12f,
                geodesic = true
            )
        }
        
        // Origin marker
        Marker(
            state = MarkerState(position = LatLng(trip.origin.lat, trip.origin.lng)),
            title = trip.origin.name ?: "Điểm xuất phát",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        )
        
        // Waypoint markers
        trip.waypoints.forEachIndexed { index, waypoint ->
            Marker(
                state = MarkerState(position = LatLng(waypoint.location.lat, waypoint.location.lng)),
                title = "Tòa ${waypoint.buildingCode}",
                snippet = "Điểm dừng ${index + 1}",
                icon = BitmapDescriptorFactory.defaultMarker(
                    if (index < trip.waypoints.size - 1) 
                        BitmapDescriptorFactory.HUE_ORANGE 
                    else 
                        BitmapDescriptorFactory.HUE_RED
                )
            )
        }
        
        // Current location marker (blue dot)
        currentLocation?.let { location ->
            Circle(
                center = LatLng(location.lat, location.lng),
                radius = 15.0,
                fillColor = Color(0xFF2196F3).copy(alpha = 0.3f),
                strokeColor = Color(0xFF2196F3),
                strokeWidth = 3f
            )
            Marker(
                state = MarkerState(position = LatLng(location.lat, location.lng)),
                title = "Vị trí của bạn",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarOverlay(
    trip: ShipperTrip,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Back button and trip info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ShipperColors.PrimaryLight)
                    ) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = ShipperColors.Primary
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ShipperColors.SuccessLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                tint = ShipperColors.Success,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Đang giao",
                                color = ShipperColors.Success,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Route summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RouteStatItem(
                        icon = Icons.Outlined.LocationOn,
                        value = "${trip.waypoints.size}",
                        label = "điểm"
                    )
                    RouteStatItem(
                        icon = Icons.Outlined.ShoppingBag,
                        value = "${trip.totalOrders}",
                        label = "đơn"
                    )
                    RouteStatItem(
                        icon = Icons.Outlined.Straighten,
                        value = trip.getFormattedDistance(),
                        label = ""
                    )
                    RouteStatItem(
                        icon = Icons.Outlined.Schedule,
                        value = trip.getFormattedDuration(),
                        label = ""
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = ShipperColors.TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "$value $label".trim(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ShipperColors.TextPrimary
        )
    }
}

@Composable
private fun BottomControlPanel(
    trip: ShipperTrip,
    isFinishing: Boolean,
    currentLocation: TripLocation?,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Next waypoint info card
        trip.waypoints.firstOrNull()?.let { nextWaypoint ->
            NextWaypointCard(waypoint = nextWaypoint, trip = trip)
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Finish button
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isFinishing && trip.status == TripStatus.STARTED,
            colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            if (isFinishing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Hoàn thành giao hàng",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun NextWaypointCard(
    waypoint: TripWaypoint,
    trip: ShipperTrip
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ShipperColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Navigation,
                    contentDescription = null,
                    tint = ShipperColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Điểm dừng tiếp theo",
                    fontSize = 12.sp,
                    color = ShipperColors.TextSecondary
                )
                Text(
                    "Tòa ${waypoint.buildingCode}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ShipperColors.TextPrimary
                )
                Text(
                    "${trip.getOrdersForStop(waypoint.order).size} đơn hàng",
                    fontSize = 14.sp,
                    color = ShipperColors.Primary
                )
            }
            
            // Direction button
            IconButton(
                onClick = { /* TODO: Open Google Maps navigation */ },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ShipperColors.Primary)
            ) {
                Icon(
                    Icons.Outlined.Directions,
                    contentDescription = "Chỉ đường",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
