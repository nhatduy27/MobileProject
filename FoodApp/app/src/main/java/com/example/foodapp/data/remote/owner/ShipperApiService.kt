package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.shipper.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service cho Owner Shippers Management
 * Base URL: /api/owner/shippers
 */
interface ShipperApiService {

    /**
     * GET /owner/shippers/applications
     * Lấy danh sách đơn xin làm shipper
     */
    @GET("owner/shippers/applications")
    suspend fun getApplications(
        @Query("status") status: String? = null
    ): Response<ApplicationsResponse>

    /**
     * POST /owner/shippers/applications/{id}/approve
     * Duyệt đơn xin làm shipper
     */
    @POST("owner/shippers/applications/{id}/approve")
    suspend fun approveApplication(
        @Path("id") applicationId: String
    ): Response<MessageResponse>

    /**
     * POST /owner/shippers/applications/{id}/reject
     * Từ chối đơn xin làm shipper
     */
    @POST("owner/shippers/applications/{id}/reject")
    suspend fun rejectApplication(
        @Path("id") applicationId: String,
        @Body request: RejectApplicationRequest
    ): Response<MessageResponse>

    /**
     * GET /owner/shippers
     * Lấy danh sách shipper đang hoạt động
     */
    @GET("owner/shippers")
    suspend fun getShippers(): Response<ShippersResponse>

    /**
     * DELETE /owner/shippers/{id}
     * Xóa shipper khỏi shop
     */
    @DELETE("owner/shippers/{id}")
    suspend fun removeShipper(
        @Path("id") shipperId: String
    ): Response<MessageResponse>
}
