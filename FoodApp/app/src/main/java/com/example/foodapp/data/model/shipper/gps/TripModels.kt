package com.example.foodapp.data.model.shipper.gps

import com.google.gson.annotations.SerializedName

/**
 * Trip Status Enum - trạng thái của chuyến đi
 */
enum class TripStatus {
    @SerializedName("PENDING") PENDING,
    @SerializedName("STARTED") STARTED,
    @SerializedName("FINISHED") FINISHED,
    @SerializedName("CANCELLED") CANCELLED
}

/**
 * Trip Delivery Status - trạng thái giao hàng của order trong trip
 */
enum class TripDeliveryStatus {
    @SerializedName("NOT_VISITED") NOT_VISITED,
    @SerializedName("VISITED") VISITED,
    @SerializedName("FAILED") FAILED
}

/**
 * Location with GPS coordinates
 */
data class TripLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val name: String? = null
)

/**
 * Waypoint in optimized route - điểm dừng trên lộ trình
 */
data class TripWaypoint(
    val buildingCode: String = "",
    val location: TripLocation = TripLocation(),
    val order: Int = 0  // 1-based visiting order (1st stop, 2nd stop...)
)

/**
 * Order associated with trip
 */
data class TripOrder(
    val orderId: String = "",
    val buildingCode: String = "",
    val tripDeliveryStatus: TripDeliveryStatus = TripDeliveryStatus.NOT_VISITED,
    val stopIndex: Int = 0  // 1-based stop number matching waypoint.order
)

/**
 * Route optimization result
 */
data class TripRoute(
    val distance: Int = 0,      // meters
    val duration: Int = 0,      // seconds  
    val polyline: String? = null,  // encoded polyline for map
    val waypointOrder: List<Int> = emptyList()
)

/**
 * Shipper Trip - Full trip data
 */
data class ShipperTrip(
    val id: String = "",
    val shipperId: String = "",
    val status: TripStatus = TripStatus.PENDING,
    val origin: TripLocation = TripLocation(),
    val returnTo: TripLocation = TripLocation(),
    val waypoints: List<TripWaypoint> = emptyList(),
    val orders: List<TripOrder> = emptyList(),
    val route: TripRoute = TripRoute(),
    val totalDistance: Int = 0,
    val totalDuration: Int = 0,
    val totalOrders: Int = 0,
    val totalBuildings: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: String? = null
) {
    /**
     * Get formatted distance (e.g., "2.4 km")
     */
    fun getFormattedDistance(): String {
        return if (totalDistance >= 1000) {
            String.format("%.1f km", totalDistance / 1000.0)
        } else {
            "$totalDistance m"
        }
    }
    
    /**
     * Get formatted duration (e.g., "7 phút")
     */
    fun getFormattedDuration(): String {
        val minutes = totalDuration / 60
        return if (minutes >= 60) {
            String.format("%d giờ %d phút", minutes / 60, minutes % 60)
        } else {
            "$minutes phút"
        }
    }
    
    /**
     * Get orders for a specific stop/waypoint
     */
    fun getOrdersForStop(stopIndex: Int): List<TripOrder> {
        return orders.filter { it.stopIndex == stopIndex }
    }
}
