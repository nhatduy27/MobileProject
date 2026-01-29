package com.example.foodapp.data.repository.shipper.base

import com.example.foodapp.data.model.shipper.gps.DeliveryPoint
import com.example.foodapp.data.model.shipper.gps.ShipperTrip
import com.example.foodapp.data.model.shipper.gps.TripLocation
import com.example.foodapp.data.model.shipper.gps.TripStatus
import com.example.foodapp.data.remote.shipper.response.PaginatedTripsData

/**
 * GPS Repository Interface - Shipper Route Optimization
 * 
 * Handles:
 * - Create optimized trips from orders
 * - Trip lifecycle (start, finish, cancel)
 * - Trip listing and detail
 * - Delivery points (KTX buildings)
 */
interface GpsRepository {
    
    /**
     * Create an optimized delivery trip
     * @param orderIds List of order IDs (1-15 orders)
     * @param origin Shipper's current location
     * @param returnTo Optional return destination (defaults to origin)
     * @return Result with created trip
     */
    suspend fun createOptimizedTrip(
        orderIds: List<String>,
        origin: TripLocation,
        returnTo: TripLocation? = null
    ): Result<ShipperTrip>
    
    /**
     * Get trip by ID
     * @param tripId Trip ID
     * @return Result with trip details
     */
    suspend fun getTrip(tripId: String): Result<ShipperTrip>
    
    /**
     * Get active trip containing a specific order
     * @param orderId Order ID to search for
     * @return Result with trip details (null if not found)
     */
    suspend fun getTripByOrderId(orderId: String): Result<ShipperTrip?>
    
    /**
     * List shipper's trips with pagination
     * @param status Optional status filter
     * @param page Page number (1-based)
     * @param limit Items per page (max 50)
     * @return Result with paginated trips
     */
    suspend fun getMyTrips(
        status: TripStatus? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<PaginatedTripsData>
    
    /**
     * Start a trip (PENDING -> STARTED)
     * Updates all trip orders: READY -> SHIPPING
     * @param tripId Trip ID
     * @return Result with updated trip
     */
    suspend fun startTrip(tripId: String): Result<ShipperTrip>
    
    /**
     * Finish a trip (STARTED -> FINISHED)
     * Updates all trip orders: SHIPPING -> DELIVERED
     * @param tripId Trip ID
     * @return Result with pair of (trip, ordersDelivered count)
     */
    suspend fun finishTrip(tripId: String): Result<Pair<ShipperTrip, Int>>
    
    /**
     * Cancel a trip (PENDING -> CANCELLED)
     * Only works for PENDING trips
     * @param tripId Trip ID
     * @param reason Optional cancellation reason
     * @return Result with updated trip
     */
    suspend fun cancelTrip(tripId: String, reason: String? = null): Result<ShipperTrip>
    
    /**
     * Get list of active KTX delivery points (buildings)
     * @return Result with list of delivery points
     */
    suspend fun getDeliveryPoints(): Result<List<DeliveryPoint>>
}
