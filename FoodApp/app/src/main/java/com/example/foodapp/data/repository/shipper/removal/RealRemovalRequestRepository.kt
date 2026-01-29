package com.example.foodapp.data.repository.shipper.removal

import android.util.Log
import com.example.foodapp.data.model.shipper.removal.CreateRemovalRequestDto
import com.example.foodapp.data.model.shipper.removal.RemovalRequest
import com.example.foodapp.data.model.shipper.removal.RemovalRequestStatus
import com.example.foodapp.data.remote.shipper.ShipperApiService
import com.example.foodapp.data.repository.shipper.base.RemovalRequestRepository
import org.json.JSONObject
import retrofit2.Response

/**
 * Real implementation of RemovalRequestRepository
 * Connects to backend API
 */
class RealRemovalRequestRepository(
    private val apiService: ShipperApiService
) : RemovalRequestRepository {
    
    companion object {
        private const val TAG = "RemovalRequestRepo"
    }
    
    override suspend fun createRemovalRequest(dto: CreateRemovalRequestDto): Result<RemovalRequest> {
        return try {
            Log.d(TAG, "Creating removal request for shop: ${dto.shopId}")
            val response = apiService.createRemovalRequest(dto)
            
            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data
                if (data != null) {
                    Log.d(TAG, "Removal request created: ${data.id}")
                    Result.success(data)
                } else {
                    Result.failure(Exception(wrapper?.message ?: "Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "Create removal request failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create removal request exception", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getMyRemovalRequests(status: RemovalRequestStatus?): Result<List<RemovalRequest>> {
        return try {
            val statusStr = status?.name
            Log.d(TAG, "Getting removal requests with status: $statusStr")
            val response = apiService.getMyRemovalRequests(statusStr)
            
            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data ?: emptyList()
                Log.d(TAG, "Got ${data.size} removal requests")
                Result.success(data)
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "Get removal requests failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get removal requests exception", e)
            Result.failure(e)
        }
    }
    
    private fun <T> parseErrorBody(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                json.optString("message", "Error: ${response.code()}")
            } else {
                "Error: ${response.code()} ${response.message()}"
            }
        } catch (e: Exception) {
            "Error: ${response.code()} ${response.message()}"
        }
    }
}
