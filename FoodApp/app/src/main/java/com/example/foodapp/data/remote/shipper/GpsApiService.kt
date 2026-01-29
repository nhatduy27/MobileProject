package com.example.foodapp.data.remote.shipper

import com.example.foodapp.data.remote.shipper.request.CancelTripRequest
import com.example.foodapp.data.remote.shipper.request.CreateOptimizedTripRequest
import com.example.foodapp.data.remote.shipper.request.FinishTripRequest
import com.example.foodapp.data.remote.shipper.request.StartTripRequest
import com.example.foodapp.data.remote.shipper.response.WrappedDeliveryPointsResponse
import com.example.foodapp.data.remote.shipper.response.WrappedFinishTripResponse
import com.example.foodapp.data.remote.shipper.response.WrappedTripResponse
import com.example.foodapp.data.remote.shipper.response.WrappedTripsListResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * GPS API Service - Shipper Route Optimization
 * 
 * Endpoints:
 * - POST /api/gps/create-optimized-trip: Create trip with optimized route
 * - GET /api/gps/trip: Get single trip by ID
 * - GET /api/gps/trips: List my trips with pagination
 * - POST /api/gps/start-trip: Start a PENDING trip
 * - POST /api/gps/finish-trip: Finish a STARTED trip
 * - POST /api/gps/cancel-trip: Cancel a PENDING trip
 * - GET /api/delivery-points: Get KTX delivery points
 */
interface GpsApiService {

    /**
     * Create optimized delivery trip
     * Takes 1-15 order IDs and calculates optimal route
     */
    @POST("gps/create-optimized-trip")
    suspend fun createOptimizedTrip(
        @Body request: CreateOptimizedTripRequest
    ): Response<WrappedTripResponse>

    /**
     * Get trip by ID
     */
    @GET("gps/trip")
    suspend fun getTrip(
        @Query("tripId") tripId: String
    ): Response<WrappedTripResponse>

    /**
     * List my trips with pagination and optional status filter
     */
    @GET("gps/trips")
    suspend fun getMyTrips(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<WrappedTripsListResponse>

    /**
     * Start a trip (PENDING -> STARTED)
     * Also updates all trip orders: READY -> SHIPPING
     */
    @POST("gps/start-trip")
    suspend fun startTrip(
        @Body request: StartTripRequest
    ): Response<WrappedTripResponse>

    /**
     * Finish a trip (STARTED -> FINISHED)
     * Also updates all trip orders: SHIPPING -> DELIVERED
     * Returns { trip, ordersDelivered }
     */
    @POST("gps/finish-trip")
    suspend fun finishTrip(
        @Body request: FinishTripRequest
    ): Response<WrappedFinishTripResponse>

    /**
     * Cancel a trip (PENDING -> CANCELLED)
     * Only works for PENDING trips
     */
    @POST("gps/cancel-trip")
    suspend fun cancelTrip(
        @Body request: CancelTripRequest
    ): Response<WrappedTripResponse>

    /**
     * Get list of active KTX delivery points (buildings)
     */
    @GET("delivery-points")
    suspend fun getDeliveryPoints(): Response<WrappedDeliveryPointsResponse>
}
