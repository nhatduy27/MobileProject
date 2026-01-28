package com.example.foodapp.data.remote.shipper

import com.example.foodapp.data.model.shipper.order.PaginatedShipperOrdersDto
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import retrofit2.Response
import retrofit2.http.*

interface ShipperApiService {

    // Get assigned orders (filters: status, page, limit)
    @GET("orders/shipper")
    suspend fun getMyOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): Response<PaginatedShipperOrdersDto>

    // Get available orders
    @GET("orders/shipper/available")
    suspend fun getAvailableOrders(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): Response<PaginatedShipperOrdersDto>

    // Get order detail
    @GET("orders/shipper/{id}")
    suspend fun getOrderDetail(@Path("id") id: String): Response<ShipperOrder>

    // Accept order
    @PUT("orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") id: String): Response<ShipperOrder>

    // Mark shipping (pickup)
    @PUT("orders/{id}/shipping")
    suspend fun markShipping(@Path("id") id: String): Response<ShipperOrder>

    // Mark delivered
    @PUT("orders/{id}/delivered")
    suspend fun markDelivered(@Path("id") id: String): Response<ShipperOrder>
}
