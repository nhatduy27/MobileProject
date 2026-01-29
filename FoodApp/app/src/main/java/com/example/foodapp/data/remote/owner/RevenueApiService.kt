package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.revenue.WrappedRevenueAnalyticsResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service cho Revenue Analytics
 * Base path: /owner/revenue
 */
interface RevenueApiService {

    /**
     * GET /owner/revenue
     * Get revenue analytics for selected period
     * 
     * @param period Period for analytics: today | week | month | year
     */
    @GET("owner/revenue")
    suspend fun getRevenueAnalytics(
        @Query("period") period: String = "today"
    ): Response<WrappedRevenueAnalyticsResponse>
}
