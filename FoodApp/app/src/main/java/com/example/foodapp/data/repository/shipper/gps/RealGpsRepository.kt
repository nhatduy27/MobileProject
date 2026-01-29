package com.example.foodapp.data.repository.shipper.gps

import android.util.Log
import com.example.foodapp.data.model.shipper.gps.DeliveryPoint
import com.example.foodapp.data.model.shipper.gps.ShipperTrip
import com.example.foodapp.data.model.shipper.gps.TripLocation
import com.example.foodapp.data.model.shipper.gps.TripStatus
import com.example.foodapp.data.remote.shipper.GpsApiService
import com.example.foodapp.data.remote.shipper.request.CancelTripRequest
import com.example.foodapp.data.remote.shipper.request.CreateOptimizedTripRequest
import com.example.foodapp.data.remote.shipper.request.FinishTripRequest
import com.example.foodapp.data.remote.shipper.request.StartTripRequest
import com.example.foodapp.data.remote.shipper.response.PaginatedTripsData
import com.example.foodapp.data.repository.shipper.base.GpsRepository
import org.json.JSONObject
import retrofit2.Response

/**
 * Real GPS Repository Implementation
 * Connects to backend GPS APIs for route optimization
 */
class RealGpsRepository(
    private val apiService: GpsApiService
) : GpsRepository {
    
    companion object {
        private const val TAG = "GpsRepository"
    }
    
    override suspend fun createOptimizedTrip(
        orderIds: List<String>,
        origin: TripLocation,
        returnTo: TripLocation?
    ): Result<ShipperTrip> {
        return try {
            Log.d(TAG, "Creating optimized trip with ${orderIds.size} orders")
            
            val request = CreateOptimizedTripRequest(
                orderIds = orderIds,
                origin = origin,
                returnTo = returnTo
            )
            
            val response = apiService.createOptimizedTrip(request)
            
            if (response.isSuccessful) {
                val trip = response.body()?.data
                if (trip != null) {
                    Log.d(TAG, "Trip created: ${trip.id}, ${trip.totalBuildings} stops, ${trip.totalOrders} orders")
                    Result.success(trip)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "Create trip failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create trip exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getTrip(tripId: String): Result<ShipperTrip> {
        return try {
            Log.d(TAG, "Getting trip: $tripId")
            
            val response = apiService.getTrip(tripId)
            
            if (response.isSuccessful) {
                val trip = response.body()?.data
                if (trip != null) {
                    Log.d(TAG, "Got trip: ${trip.id}, status: ${trip.status}")
                    Result.success(trip)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "Get trip failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get trip exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getMyTrips(
        status: TripStatus?,
        page: Int,
        limit: Int
    ): Result<PaginatedTripsData> {
        return try {
            Log.d(TAG, "Getting trips: status=$status, page=$page, limit=$limit")
            
            val response = apiService.getMyTrips(
                status = status?.name,
                page = page,
                limit = limit
            )
            
            if (response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) {
                    Log.d(TAG, "Got ${data.items.size} trips, total: ${data.total}")
                    Result.success(data)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "Get trips failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get trips exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getTripByOrderId(orderId: String): Result<ShipperTrip?> {
        return try {
            Log.d(TAG, "Getting trip by order ID: $orderId")
            
            // Get active trips (PENDING and STARTED)
            val pendingTrips = apiService.getMyTrips(
                status = TripStatus.PENDING.name,
                page = 1,
                limit = 20
            )
            
            val startedTrips = apiService.getMyTrips(
                status = TripStatus.STARTED.name,
                page = 1,
                limit = 20
            )
            
            // Combine trips from both responses
            val allTrips = mutableListOf<ShipperTrip>()
            
            if (pendingTrips.isSuccessful) {
                pendingTrips.body()?.data?.items?.let { allTrips.addAll(it) }
            }
            
            if (startedTrips.isSuccessful) {
                startedTrips.body()?.data?.items?.let { allTrips.addAll(it) }
            }
            
            // Find trip containing the order
            val tripWithOrder = allTrips.find { trip ->
                trip.orders.any { it.orderId == orderId }
            }
            
            if (tripWithOrder != null) {
                // Load full trip details
                val fullTrip = getTrip(tripWithOrder.id)
                if (fullTrip.isSuccess) {
                    Log.d(TAG, "Found trip ${tripWithOrder.id} for order $orderId")
                    Result.success(fullTrip.getOrNull())
                } else {
                    Result.success(tripWithOrder)
                }
            } else {
                Log.d(TAG, "No trip found for order $orderId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get trip by order exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun startTrip(tripId: String): Result<ShipperTrip> {
        return try {
            Log.d(TAG, "Starting trip: $tripId")
            
            val request = StartTripRequest(tripId = tripId)
            val response = apiService.startTrip(request)
            
            if (response.isSuccessful) {
                val trip = response.body()?.data
                if (trip != null) {
                    Log.d(TAG, "Trip started: ${trip.id}, status: ${trip.status}")
                    Result.success(trip)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "Start trip failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Start trip exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun finishTrip(tripId: String): Result<Pair<ShipperTrip, Int>> {
        return try {
            Log.d(TAG, "Finishing trip: $tripId")
            
            val request = FinishTripRequest(tripId = tripId)
            val response = apiService.finishTrip(request)
            
            if (response.isSuccessful) {
                val data = response.body()?.data
                val trip = data?.trip
                val ordersDelivered = data?.ordersDelivered ?: 0
                
                if (trip != null) {
                    Log.d(TAG, "Trip finished: ${trip.id}, orders delivered: $ordersDelivered")
                    Result.success(Pair(trip, ordersDelivered))
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "Finish trip failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Finish trip exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun cancelTrip(tripId: String, reason: String?): Result<ShipperTrip> {
        return try {
            Log.d(TAG, "Cancelling trip: $tripId, reason: $reason")
            
            val request = CancelTripRequest(tripId = tripId, reason = reason)
            val response = apiService.cancelTrip(request)
            
            if (response.isSuccessful) {
                val trip = response.body()?.data
                if (trip != null) {
                    Log.d(TAG, "Trip cancelled: ${trip.id}")
                    Result.success(trip)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "Cancel trip failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cancel trip exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getDeliveryPoints(): Result<List<DeliveryPoint>> {
        return try {
            Log.d(TAG, "Getting delivery points")
            
            val response = apiService.getDeliveryPoints()
            
            if (response.isSuccessful) {
                val points = response.body()?.data ?: emptyList()
                Log.d(TAG, "Got ${points.size} delivery points")
                Result.success(points)
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "Get delivery points failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get delivery points exception", e)
            Result.failure(e)
        }
    }
    
    private fun <T> parseErrorBody(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                val message = json.optString("message", "Error: ${response.code()}")
                translateErrorMessage(message)
            } else {
                "Đã có lỗi xảy ra. Vui lòng thử lại."
            }
        } catch (e: Exception) {
            "Đã có lỗi xảy ra. Vui lòng thử lại."
        }
    }
    
    /**
     * Translate common backend error messages to user-friendly Vietnamese
     */
    private fun translateErrorMessage(message: String): String {
        return when {
            // Order status errors
            message.contains("must be in READY status to start shipping") -> 
                "Đơn hàng đã được giao hoặc đang được xử lý. Không thể bắt đầu lại."
            message.contains("Current status: SHIPPING") -> 
                "Đơn hàng đang trong quá trình giao. Vui lòng tiếp tục giao hàng."
            message.contains("Current status: DELIVERED") -> 
                "Đơn hàng đã được giao thành công."
            message.contains("Current status: CANCELLED") -> 
                "Đơn hàng đã bị hủy."
                
            // Trip errors
            message.contains("Trip not found") -> 
                "Không tìm thấy lộ trình. Vui lòng tải lại."
            message.contains("Trip is already started") -> 
                "Lộ trình đã được bắt đầu. Bạn có thể tiếp tục giao hàng."
            message.contains("Trip is already finished") -> 
                "Lộ trình đã hoàn thành."
            message.contains("Trip is cancelled") -> 
                "Lộ trình đã bị hủy."
            message.contains("Cannot cancel started trip") -> 
                "Không thể hủy lộ trình đã bắt đầu. Vui lòng hoàn thành giao hàng."
            message.contains("must be in PENDING status") -> 
                "Lộ trình đang được xử lý. Vui lòng thử lại sau."
            message.contains("must be in STARTED status") -> 
                "Lộ trình chưa được bắt đầu. Vui lòng bắt đầu trước."
                
            // Order assignment errors
            message.contains("not assigned to shipper") -> 
                "Đơn hàng không thuộc về bạn. Vui lòng kiểm tra lại."
            message.contains("Order not found") -> 
                "Không tìm thấy đơn hàng."
                
            // Network/Auth errors
            message.contains("Unauthorized") || message.contains("401") -> 
                "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại."
            message.contains("Forbidden") || message.contains("403") -> 
                "Bạn không có quyền thực hiện thao tác này."
            message.contains("timeout") || message.contains("Timeout") -> 
                "Kết nối chậm. Vui lòng thử lại."
                
            // Default - return original if no match
            else -> message
        }
    }
}
