package com.example.foodapp.data.repository.owner.dashboard

import com.example.foodapp.data.model.owner.*
import com.example.foodapp.data.remote.owner.ShopApiService
import com.example.foodapp.data.repository.owner.base.OwnerDashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RealDashboardRepository(private val apiService: ShopApiService) : OwnerDashboardRepository {

    override suspend fun getShopAnalytics(
        from: String?,
        to: String?
    ): Result<DashboardData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getShopDashboard(from, to)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception("Failed to get dashboard data"))
                    }
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
