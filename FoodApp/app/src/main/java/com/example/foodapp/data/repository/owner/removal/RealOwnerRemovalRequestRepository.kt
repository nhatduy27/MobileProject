package com.example.foodapp.data.repository.owner.removal

import android.util.Log
import com.example.foodapp.data.model.owner.removal.*
import com.example.foodapp.data.remote.owner.RemovalRequestApiService
import com.example.foodapp.data.repository.owner.base.OwnerRemovalRequestRepository
import org.json.JSONObject
import retrofit2.Response

/**
 * Real implementation of OwnerRemovalRequestRepository
 * Calls actual backend API
 */
class RealOwnerRemovalRequestRepository(
    private val apiService: RemovalRequestApiService
) : OwnerRemovalRequestRepository {

    companion object {
        private const val TAG = "OwnerRemovalRepo"
    }

    override suspend fun getShopRemovalRequests(
        shopId: String,
        status: OwnerRemovalRequestStatus?
    ): Result<List<OwnerRemovalRequest>> {
        return try {
            Log.d(TAG, "🔄 Fetching removal requests for shop: $shopId, status: $status")
            
            val statusStr = status?.name
            val response = apiService.getShopRemovalRequests(shopId, statusStr)

            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data ?: emptyList()
                Log.d(TAG, "✅ Got ${data.size} removal requests")
                Result.success(data)
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error fetching removal requests: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching removal requests", e)
            Result.failure(e)
        }
    }

    override suspend fun approveRequest(requestId: String): Result<OwnerRemovalRequest> {
        return try {
            Log.d(TAG, "🔄 Approving removal request: $requestId")
            
            val dto = ProcessRemovalRequestDto(action = "APPROVE")
            val response = apiService.processRemovalRequest(requestId, dto)

            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data
                if (data != null) {
                    Log.d(TAG, "✅ Removal request approved: ${data.id}")
                    Result.success(data)
                } else {
                    Result.failure(Exception(wrapper?.message ?: "Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error approving request: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception approving request", e)
            Result.failure(e)
        }
    }

    override suspend fun rejectRequest(requestId: String, reason: String): Result<OwnerRemovalRequest> {
        return try {
            Log.d(TAG, "🔄 Rejecting removal request: $requestId")
            
            val dto = ProcessRemovalRequestDto(action = "REJECT", rejectionReason = reason)
            val response = apiService.processRemovalRequest(requestId, dto)

            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data
                if (data != null) {
                    Log.d(TAG, "✅ Removal request rejected: ${data.id}")
                    Result.success(data)
                } else {
                    Result.failure(Exception(wrapper?.message ?: "Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error rejecting request: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception rejecting request", e)
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
