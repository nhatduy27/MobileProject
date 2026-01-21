package com.example.foodapp.data.repository.owner.base

import com.example.foodapp.data.model.owner.order.OrderDetail
import com.example.foodapp.data.model.owner.order.PaginatedOrders
import com.example.foodapp.data.model.owner.order.ShopOrder

/**
 * Interface for Owner Orders Repository.
 * Defines methods for order management through backend API.
 */
interface OwnerOrdersRepository {
    
    /**
     * Get paginated list of shop orders
     * @param status Optional status filter (null = all orders)
     * @param page Page number (1-indexed)
     * @param limit Items per page
     */
    suspend fun getOrders(
        status: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<PaginatedOrders>
    
    /**
     * Get full order detail
     * @param orderId Order ID
     */
    suspend fun getOrderDetail(orderId: String): Result<OrderDetail>
    
    /**
     * Confirm a pending order (PENDING -> CONFIRMED)
     * @param orderId Order ID
     */
    suspend fun confirmOrder(orderId: String): Result<ShopOrder>
    
    /**
     * Mark order as preparing (CONFIRMED -> PREPARING)
     * @param orderId Order ID
     */
    suspend fun markPreparing(orderId: String): Result<ShopOrder>
    
    /**
     * Mark order as ready (PREPARING -> READY)
     * @param orderId Order ID
     */
    suspend fun markReady(orderId: String): Result<ShopOrder>
    
    /**
     * Cancel an order with optional reason
     * @param orderId Order ID
     * @param reason Optional cancellation reason
     */
    suspend fun cancelOrder(orderId: String, reason: String? = null): Result<ShopOrder>
}
