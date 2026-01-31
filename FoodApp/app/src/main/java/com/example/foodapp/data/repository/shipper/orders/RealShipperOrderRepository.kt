package com.example.foodapp.data.repository.shipper.orders

import android.util.Log
import com.example.foodapp.data.model.shipper.order.PaginatedShipperOrdersDto
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import com.example.foodapp.data.model.shipper.order.WrappedPaginatedOrdersResponse
import com.example.foodapp.data.model.shipper.order.WrappedShipperOrderResponse
import com.example.foodapp.data.remote.shipper.ShipperApiService
import com.example.foodapp.data.repository.shipper.base.ShipperOrderRepository
import org.json.JSONObject
import retrofit2.Response

class RealShipperOrderRepository(
    private val apiService: ShipperApiService
) : ShipperOrderRepository {

    override suspend fun getMyOrders(status: String?, page: Int, limit: Int): Result<PaginatedShipperOrdersDto> {
        return try {
            val response = apiService.getMyOrders(status, page, limit)
            Log.d("ShipperOrderRepo", "🔍 getMyOrders response: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data
                Log.d("ShipperOrderRepo", "📦 getMyOrders body: success=${wrapper?.success}, orders=${data?.orders?.size ?: 0}, total=${data?.total ?: 0}")
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e("ShipperOrderRepo", "❌ getMyOrders error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("ShipperOrderRepo", "❌ getMyOrders exception", e)
            Result.failure(e)
        }
    }

    override suspend fun getAvailableOrders(page: Int, limit: Int): Result<PaginatedShipperOrdersDto> {
        return try {
            val response = apiService.getAvailableOrders(page, limit)
            Log.d("ShipperOrderRepo", "🔍 getAvailableOrders response: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data
                Log.d("ShipperOrderRepo", "📦 getAvailableOrders body: success=${wrapper?.success}, orders=${data?.orders?.size ?: 0}, total=${data?.total ?: 0}")
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e("ShipperOrderRepo", "❌ getAvailableOrders error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("ShipperOrderRepo", "❌ getAvailableOrders exception", e)
            Result.failure(e)
        }
    }

    override suspend fun getOrderDetail(id: String): Result<ShipperOrder> {
        return safeApiCallWrapped { apiService.getOrderDetail(id) }
    }

    override suspend fun acceptOrder(id: String): Result<ShipperOrder> {
        return safeApiCallWrapped { apiService.acceptOrder(id) }
    }

    override suspend fun markShipping(id: String): Result<ShipperOrder> {
        return safeApiCallWrapped { apiService.markShipping(id) }
    }

    override suspend fun markDelivered(id: String): Result<ShipperOrder> {
        return safeApiCallWrapped { apiService.markDelivered(id) }
    }
    
    override suspend fun getOnlineStatus(): Result<Boolean> {
        return try {
            val response = apiService.getOnlineStatus()
            if (response.isSuccessful) {
                val isOnline = response.body()?.data?.isOnline ?: false
                Log.d("ShipperOrderRepo", "Get online status success: $isOnline")
                Result.success(isOnline)
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e("ShipperOrderRepo", "Get online status failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("ShipperOrderRepo", "Get online status exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun goOnline(): Result<String> {
        return try {
            val response = apiService.goOnline()
            if (response.isSuccessful) {
                val topic = response.body()?.data?.topic ?: ""
                Log.d("ShipperOrderRepo", "Go online success, topic: $topic")
                Result.success(topic)
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e("ShipperOrderRepo", "Go online failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("ShipperOrderRepo", "Go online exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun goOffline(): Result<String> {
        return try {
            val response = apiService.goOffline()
            if (response.isSuccessful) {
                val topic = response.body()?.data?.topic ?: ""
                Log.d("ShipperOrderRepo", "Go offline success, topic: $topic")
                Result.success(topic)
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e("ShipperOrderRepo", "Go offline failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("ShipperOrderRepo", "Go offline exception", e)
            Result.failure(e)
        }
    }

    // Helper for wrapped ShipperOrder responses
    private suspend fun safeApiCallWrapped(apiCall: suspend () -> Response<WrappedShipperOrderResponse>): Result<ShipperOrder> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("ShipperOrderRepo", "API call failed", e)
            Result.failure(e)
        }
    }
    
    private fun <T> parseErrorBody(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                // Backend trả về { code, message, statusCode } hoặc { message }
                json.optString("message", "Error: ${response.code()}")
            } else {
                "Error: ${response.code()} ${response.message()}"
            }
        } catch (e: Exception) {
            "Error: ${response.code()} ${response.message()}"
        }
    }
}
