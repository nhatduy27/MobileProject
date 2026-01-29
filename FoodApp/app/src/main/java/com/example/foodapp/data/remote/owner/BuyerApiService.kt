package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.buyer.WrappedBuyerDetailResponse
import com.example.foodapp.data.model.owner.buyer.WrappedBuyerListResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service cho Buyer Management
 * Base path: /owner/buyers
 */
interface BuyerApiService {

    /**
     * GET /owner/buyers
     * List buyers with filters and pagination
     * 
     * @param page Page number (default: 1)
     * @param limit Items per page (default: 20, max: 50)
     * @param tier Filter by tier: ALL | VIP | NORMAL | NEW
     * @param search Search by name or phone
     * @param sort Sort by: createdAt | totalSpent
     */
    @GET("owner/buyers")
    suspend fun listBuyers(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20,
        @Query("tier") tier: String? = "ALL",
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = "createdAt"
    ): Response<WrappedBuyerListResponse>

    /**
     * GET /owner/buyers/{customerId}
     * Get buyer detail with recent orders
     */
    @GET("owner/buyers/{customerId}")
    suspend fun getBuyerDetail(
        @Path("customerId") customerId: String
    ): Response<WrappedBuyerDetailResponse>
}
