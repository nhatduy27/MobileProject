package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.review.*
import retrofit2.Response
import retrofit2.http.*

interface ReviewApiService {

    /**
     * Tạo đánh giá cho đơn hàng
     */
    @POST("reviews")
    suspend fun createOrderReview(
        @Header("Authorization") token: String,
        @Body request: CreateOrderReviewRequest
    ): Response<CreateOrderReviewResponse>

    /**
     * Lấy danh sách đánh giá đơn hàng của tôi
     */
    @GET("reviews/my")
    suspend fun getMyOrderReviews(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<GetMyOrderReviewsResponse>

    /**
     * Lấy danh sách đánh giá đơn hàng của shop
     */
    @GET("reviews/shop/{shopId}")
    suspend fun getShopOrderReviews(
        @Header("Authorization") token: String,
        @Path("shopId") shopId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<GetShopOrderReviewsResponse>

}