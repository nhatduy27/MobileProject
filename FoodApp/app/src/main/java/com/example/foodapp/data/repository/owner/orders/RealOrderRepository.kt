package com.example.foodapp.data.repository.owner.orders

import com.example.foodapp.data.model.owner.order.*
import com.example.foodapp.data.remote.owner.OrderApiService
import com.example.foodapp.data.remote.owner.response.WrappedOrderActionResponse
import com.example.foodapp.data.repository.owner.base.OwnerOrdersRepository
import retrofit2.Response

/**
 * Real implementation of OwnerOrdersRepository using backend API
 */
class RealOrderRepository(
    private val apiService: OrderApiService
) : OwnerOrdersRepository {

    override suspend fun getOrders(
        status: String?,
        page: Int,
        limit: Int
    ): Result<PaginatedOrders> {
        return try {
            val response = apiService.getShopOrders(status, page, limit)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.toPaginatedOrders())
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to load orders"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderDetail(orderId: String): Result<OrderDetail> {
        return try {
            val response = apiService.getOrderDetail(orderId)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                if (wrapper.success && wrapper.data != null) {
                    Result.success(wrapper.data.toOrderDetail())
                } else {
                    Result.failure(Exception(wrapper.message ?: "Failed to load order detail"))
                }
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmOrder(orderId: String): Result<ShopOrder> {
        return try {
            val response = apiService.confirmOrder(orderId)
            handleActionResponse(response, orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markPreparing(orderId: String): Result<ShopOrder> {
        return try {
            val response = apiService.markPreparing(orderId)
            handleActionResponse(response, orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markReady(orderId: String): Result<ShopOrder> {
        return try {
            val response = apiService.markReady(orderId)
            handleActionResponse(response, orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelOrder(orderId: String, reason: String?): Result<ShopOrder> {
        return try {
            val response = apiService.cancelOrder(orderId, CancelOrderRequest(reason))
            handleActionResponse(response, orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Handle action response - since action endpoints return OrderEntity,
     * we refetch the order to get the full ShopOrder data
     */
    private suspend fun handleActionResponse(
        response: Response<WrappedOrderActionResponse>,
        orderId: String
    ): Result<ShopOrder> {
        if (!response.isSuccessful) {
            return Result.failure(Exception(getErrorMessage(response)))
        }
        
        val wrapper = response.body()
        if (wrapper == null || !wrapper.success) {
            return Result.failure(Exception(wrapper?.message ?: "Action failed"))
        }
        
        // Refetch the order to get updated data with all fields
        val detailResponse = apiService.getOrderDetail(orderId)
        if (detailResponse.isSuccessful && detailResponse.body() != null) {
            val detailWrapper = detailResponse.body()!!
            if (detailWrapper.success && detailWrapper.data != null) {
                val detail = detailWrapper.data.toOrderDetail()
                return Result.success(detail.toShopOrder())
            }
        }
        
        // If refetch fails, create a minimal ShopOrder from action response
        val actionData = wrapper.data
        if (actionData != null) {
            return Result.success(
                ShopOrder(
                    id = actionData.id,
                    orderNumber = actionData.orderNumber ?: "",
                    shopId = "",
                    shopName = "",
                    status = ShopOrderStatus.fromApiValue(actionData.status),
                    paymentStatus = PaymentStatus.fromApiValue(actionData.paymentStatus ?: "UNPAID"),
                    paymentMethod = null,
                    total = 0,
                    itemCount = 0,
                    createdAt = null,
                    updatedAt = actionData.updatedAt,
                    customer = null,
                    deliveryAddress = null
                )
            )
        }
        
        return Result.failure(Exception("Action completed but failed to get updated order"))
    }

    private fun <T> getErrorMessage(response: Response<T>): String {
        return try {
            response.errorBody()?.string() ?: "Unknown error occurred"
        } catch (e: Exception) {
            "Error: ${response.code()}"
        }
    }
}

/**
 * Extension to convert OrderDetail to ShopOrder
 */
private fun OrderDetail.toShopOrder(): ShopOrder {
    return ShopOrder(
        id = id,
        orderNumber = orderNumber,
        shopId = shopId,
        shopName = shopName,
        status = status,
        paymentStatus = paymentStatus,
        paymentMethod = paymentMethod,
        total = total,
        itemCount = items.size,
        createdAt = createdAt,
        updatedAt = updatedAt,
        itemsPreview = items,
        itemsPreviewCount = items.size,
        customer = customer,
        deliveryAddress = deliveryAddress,
        shipperId = shipperId,
        shipper = shipper
    )
}
