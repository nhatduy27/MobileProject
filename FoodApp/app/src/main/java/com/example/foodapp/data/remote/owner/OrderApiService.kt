package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.order.CancelOrderRequest
import com.example.foodapp.data.remote.owner.response.WrappedPaginatedOrdersResponse
import com.example.foodapp.data.remote.owner.response.WrappedOrderDetailResponse
import com.example.foodapp.data.remote.owner.response.WrappedOrderActionResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for Owner Order Management
 * 
 * Endpoints:
 * - GET /orders/shop - Get shop orders (paginated)
 * - GET /orders/shop/{id} - Get order detail
 * - PUT /orders/{id}/confirm - Confirm order
 * - PUT /orders/{id}/preparing - Mark as preparing
 * - PUT /orders/{id}/ready - Mark as ready
 * - PUT /orders/{id}/owner-cancel - Cancel order
 */
interface OrderApiService {

    /**
     * GET /orders/shop
     * Get paginated list of shop orders
     */
    @GET("orders/shop")
    suspend fun getShopOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<WrappedPaginatedOrdersResponse>

    /**
     * GET /orders/shop/{id}
     * Get full order detail
     */
    @GET("orders/shop/{id}")
    suspend fun getOrderDetail(
        @Path("id") orderId: String
    ): Response<WrappedOrderDetailResponse>

    /**
     * PUT /orders/{id}/confirm
     * Confirm a pending order
     */
    @PUT("orders/{id}/confirm")
    suspend fun confirmOrder(
        @Path("id") orderId: String
    ): Response<WrappedOrderActionResponse>

    /**
     * PUT /orders/{id}/preparing
     * Mark order as preparing
     */
    @PUT("orders/{id}/preparing")
    suspend fun markPreparing(
        @Path("id") orderId: String
    ): Response<WrappedOrderActionResponse>

    /**
     * PUT /orders/{id}/ready
     * Mark order as ready for pickup
     */
    @PUT("orders/{id}/ready")
    suspend fun markReady(
        @Path("id") orderId: String
    ): Response<WrappedOrderActionResponse>

    /**
     * PUT /orders/{id}/owner-cancel
     * Cancel an order with optional reason
     */
    @PUT("orders/{id}/owner-cancel")
    suspend fun cancelOrder(
        @Path("id") orderId: String,
        @Body request: CancelOrderRequest
    ): Response<WrappedOrderActionResponse>
}
