package com.example.foodapp.data.repository.owner.revenue

import android.util.Log
import com.example.foodapp.data.model.owner.revenue.RevenueAnalytics
import com.example.foodapp.data.model.owner.revenue.RevenuePeriod
import com.example.foodapp.data.remote.owner.RevenueApiService
import org.json.JSONObject
import retrofit2.Response

/**
 * Real Repository cho Revenue Analytics
 * Kết nối với backend API
 */
class RealRevenueRepository(
    private val apiService: RevenueApiService
) {
    private val TAG = "RevenueRepository"

    /**
     * Lấy revenue analytics theo period
     */
    suspend fun getRevenueAnalytics(period: RevenuePeriod): Result<RevenueAnalytics> {
        return try {
            Log.d(TAG, "🔍 Fetching revenue analytics: period=${period.apiValue}")
            val response = apiService.getRevenueAnalytics(period.apiValue)
            
            if (response.isSuccessful) {
                val wrapper = response.body()
                val data = wrapper?.data
                Log.d(TAG, "✅ Got revenue analytics: stats=${data?.stats?.size}, timeSlots=${data?.timeSlots?.size}")
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMessage = parseErrorBody(response)
                Log.e(TAG, "❌ Error fetching revenue: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception fetching revenue", e)
            Result.failure(e)
        }
    }

    /**
     * Lấy danh sách periods có sẵn
     */
    fun getAvailablePeriods(): List<RevenuePeriod> {
        return RevenuePeriod.values().toList()
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
