package com.example.foodapp.data.repository.owner.dashboard

import android.util.Log
import com.example.foodapp.data.model.owner.*
import com.example.foodapp.data.remote.owner.ShopApiService
import com.example.foodapp.data.repository.owner.base.OwnerDashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealDashboardRepository(private val apiService: ShopApiService) : OwnerDashboardRepository {

    companion object {
        private const val TAG = "DashboardRepo"
    }

    override suspend fun getShopAnalytics(
        from: String?,
        to: String?
    ): Result<DashboardData> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Calling getShopDashboard API...")
                val response = apiService.getShopDashboard(from, to)
                Log.d(TAG, "Response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d(TAG, "Response success: ${body.success}")
                    Log.d(TAG, "All-time revenue: ${body.data.allTime.revenue}")
                    Log.d(TAG, "All-time orderCount: ${body.data.allTime.orderCount}")
                    Log.d(TAG, "Recent orders count: ${body.data.recentOrders.size}")
                    if (body.data.recentOrders.isNotEmpty()) {
                        Log.d(TAG, "First order total: ${body.data.recentOrders[0].total}")
                    }
                    
                    if (body.success) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception("Failed to get dashboard data"))
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error"
                    Log.e(TAG, "API Error: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}

