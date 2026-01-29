package com.example.foodapp.data.remote.shipper.request

import com.example.foodapp.data.model.shipper.gps.TripLocation

/**
 * Request body for creating an optimized trip
 */
data class CreateOptimizedTripRequest(
    val orderIds: List<String>,
    val origin: TripLocation,
    val returnTo: TripLocation? = null
)

/**
 * Request body for starting a trip
 */
data class StartTripRequest(
    val tripId: String
)

/**
 * Request body for finishing a trip
 */
data class FinishTripRequest(
    val tripId: String
)

/**
 * Request body for cancelling a trip
 */
data class CancelTripRequest(
    val tripId: String,
    val reason: String? = null
)
