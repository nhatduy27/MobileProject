package com.example.foodapp.data.remote.shipper

import com.example.foodapp.data.model.shipper.order.WrappedPaginatedOrdersResponse
import com.example.foodapp.data.model.shipper.order.WrappedShipperOrderResponse
import com.example.foodapp.data.remote.shipper.response.GoOnlineResponse
import com.example.foodapp.data.remote.shipper.response.GoOfflineResponse
import retrofit2.Response
import retrofit2.http.*

interface ShipperApiService {

    // Get assigned orders (filters: status, page, limit)
    @GET("orders/shipper")
    suspend fun getMyOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): Response<WrappedPaginatedOrdersResponse>

    // Get available orders
    @GET("orders/shipper/available")
    suspend fun getAvailableOrders(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): Response<WrappedPaginatedOrdersResponse>

    // Get order detail
    @GET("orders/shipper/{id}")
    suspend fun getOrderDetail(@Path("id") id: String): Response<WrappedShipperOrderResponse>

    // Accept order
    @PUT("orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") id: String): Response<WrappedShipperOrderResponse>

    // Mark shipping (pickup)
    @PUT("orders/{id}/shipping")
    suspend fun markShipping(@Path("id") id: String): Response<WrappedShipperOrderResponse>

    // Mark delivered
    @PUT("orders/{id}/delivered")
    suspend fun markDelivered(@Path("id") id: String): Response<WrappedShipperOrderResponse>
    
    // Shipper goes online - subscribe to topic for ORDER_READY broadcasts
    @POST("shippers/notifications/online")
    suspend fun goOnline(): Response<GoOnlineResponse>
    
    // Shipper goes offline - unsubscribe from topic
    @DELETE("shippers/notifications/online")
    suspend fun goOffline(): Response<GoOfflineResponse>
}

