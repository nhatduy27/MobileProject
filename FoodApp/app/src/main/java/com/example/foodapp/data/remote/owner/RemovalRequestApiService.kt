package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.removal.ProcessRemovalRequestDto
import com.example.foodapp.data.model.owner.removal.WrappedOwnerRemovalRequestListResponse
import com.example.foodapp.data.model.owner.removal.WrappedOwnerRemovalRequestResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service for Owner Removal Requests management
 */
interface RemovalRequestApiService {
    
    /**
     * GET /owner/shops/{shopId}/removal-requests
     * Get list of removal requests for a shop
     */
    @GET("owner/shops/{shopId}/removal-requests")
    suspend fun getShopRemovalRequests(
        @Path("shopId") shopId: String,
        @Query("status") status: String? = null
    ): Response<WrappedOwnerRemovalRequestListResponse>
    
    /**
     * PUT /owner/removal-requests/{requestId}
     * Process (approve/reject) a removal request
     */
    @PUT("owner/removal-requests/{requestId}")
    suspend fun processRemovalRequest(
        @Path("requestId") requestId: String,
        @Body dto: ProcessRemovalRequestDto
    ): Response<WrappedOwnerRemovalRequestResponse>
}
