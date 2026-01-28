package com.example.foodapp.data.repository.shipper.orders

import android.util.Log
import com.example.foodapp.data.model.shipper.order.PaginatedShipperOrdersDto
import com.example.foodapp.data.model.shipper.order.ShipperOrder
import com.example.foodapp.data.remote.shipper.ShipperApiService
import com.example.foodapp.data.repository.shipper.base.ShipperOrderRepository
import org.json.JSONObject
import retrofit2.Response

class RealShipperOrderRepository(
    private val apiService: ShipperApiService
) : ShipperOrderRepository {

    override suspend fun getMyOrders(status: String?, page: Int, limit: Int): Result<PaginatedShipperOrdersDto> {
        return safeApiCall { apiService.getMyOrders(status, page, limit) }
    }

    override suspend fun getAvailableOrders(page: Int, limit: Int): Result<PaginatedShipperOrdersDto> {
        return safeApiCall { apiService.getAvailableOrders(page, limit) }
    }

    override suspend fun getOrderDetail(id: String): Result<ShipperOrder> {
        return safeApiCall { apiService.getOrderDetail(id) }
    }

    override suspend fun acceptOrder(id: String): Result<ShipperOrder> {
        return safeApiCall { apiService.acceptOrder(id) }
    }

    override suspend fun markShipping(id: String): Result<ShipperOrder> {
        return safeApiCall { apiService.markShipping(id) }
    }

    override suspend fun markDelivered(id: String): Result<ShipperOrder> {
        return safeApiCall { apiService.markDelivered(id) }
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

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                // Parse error body to get detailed message
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
