package com.example.foodapp.data.remote.shipper.response

import com.example.foodapp.data.model.shipper.gps.DeliveryPoint
import com.example.foodapp.data.model.shipper.gps.ShipperTrip

/**
 * Wrapped response for single Trip
 */
data class WrappedTripResponse(
    val success: Boolean = false,
    val data: ShipperTrip? = null,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Paginated trips list response
 */
data class PaginatedTripsData(
    val items: List<ShipperTrip> = emptyList(),
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false
)

/**
 * Wrapped response for trips list
 */
data class WrappedTripsListResponse(
    val success: Boolean = false,
    val data: PaginatedTripsData? = null,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Finish trip response data (special format)
 */
data class FinishTripData(
    val trip: ShipperTrip? = null,
    val ordersDelivered: Int = 0
)

/**
 * Wrapped response for finish trip
 */
data class WrappedFinishTripResponse(
    val success: Boolean = false,
    val data: FinishTripData? = null,
    val message: String? = null,
    val timestamp: String? = null
)

/**
 * Wrapped response for delivery points list
 */
data class WrappedDeliveryPointsResponse(
    val success: Boolean = false,
    val data: List<DeliveryPoint>? = null,
    val message: String? = null,
    val timestamp: String? = null
)
