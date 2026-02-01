package com.example.foodapp.data.repository.shipper.base

import com.example.foodapp.data.model.shipper.order.PaginatedShipperOrdersDto
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import kotlinx.coroutines.flow.Flow

interface ShipperOrderRepository {
    suspend fun getMyOrders(status: String?, page: Int, limit: Int): Result<PaginatedShipperOrdersDto>
    suspend fun getAvailableOrders(page: Int, limit: Int): Result<PaginatedShipperOrdersDto>
    suspend fun getOrderDetail(id: String): Result<ShipperOrder>
    suspend fun acceptOrder(id: String): Result<ShipperOrder>
    suspend fun markShipping(id: String): Result<ShipperOrder>
    suspend fun markDelivered(id: String): Result<ShipperOrder>
    
    // Online status management
    suspend fun getOnlineStatus(): Result<Boolean>
    suspend fun goOnline(): Result<String>
    suspend fun goOffline(): Result<String>
}

