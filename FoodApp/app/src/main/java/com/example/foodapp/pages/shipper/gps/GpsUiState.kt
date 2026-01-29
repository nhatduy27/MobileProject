package com.example.foodapp.pages.shipper.gps

import com.example.foodapp.data.model.shipper.gps.DeliveryPoint
import com.example.foodapp.data.model.shipper.gps.ShipperTrip
import com.example.foodapp.data.model.shipper.gps.TripStatus
import com.example.foodapp.data.model.shipper.order.ShipperOrder

/**
 * UI State for GPS/Trip screens
 */
data class GpsUiState(
    // Available orders for trip creation (status = READY)
    val availableOrders: List<ShipperOrder> = emptyList(),
    // Orders that have been accepted but not yet delivered (status = SHIPPING)
    val shippingOrders: List<ShipperOrder> = emptyList(),
    val selectedOrderIds: Set<String> = emptySet(),
    val isLoadingOrders: Boolean = false,
    
    // Current trip
    val currentTrip: ShipperTrip? = null,
    val isCreatingTrip: Boolean = false,
    val isStartingTrip: Boolean = false,
    val isFinishingTrip: Boolean = false,
    val isCancellingTrip: Boolean = false,
    
    // Trip list
    val trips: List<ShipperTrip> = emptyList(),
    val isLoadingTrips: Boolean = false,
    val tripsPage: Int = 1,
    val hasMoreTrips: Boolean = false,
    val totalTrips: Int = 0,
    val tripStatusFilter: TripStatus? = null,
    
    // Delivery points
    val deliveryPoints: List<DeliveryPoint> = emptyList(),
    val isLoadingDeliveryPoints: Boolean = false,
    
    // Messages
    val successMessage: String? = null,
    val errorMessage: String? = null,
    
    // Navigation
    val navigateToTripDetail: String? = null,
    val navigateToMap: String? = null, // Trip ID to navigate to map screen
    val ordersDeliveredCount: Int = 0
) {
    val canCreateTrip: Boolean
        get() = selectedOrderIds.isNotEmpty() && selectedOrderIds.size <= 15 && !isCreatingTrip
    
    val selectedOrdersCount: Int
        get() = selectedOrderIds.size
    
    val isActiveTrip: Boolean
        get() = currentTrip?.status == TripStatus.PENDING || currentTrip?.status == TripStatus.STARTED
}
