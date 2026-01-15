package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.CreateShopRequest
import com.example.foodapp.data.model.owner.CreateShopResponse
import com.example.foodapp.data.model.owner.Shop
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service cho Owner Shop Management
 * Base URL: /owner/shop
 */
interface ShopApiService {
    
    /**
     * POST /owner/shop
     * Tạo shop mới cho owner (JSON)
     */
    @POST("owner/shop")
    suspend fun createShop(
        @Body request: CreateShopRequest
    ): Response<CreateShopResponse>
    
    /**
     * POST /owner/shop
     * Tạo shop mới với ảnh (Multipart)
     */
    @Multipart
    @POST("owner/shop")
    suspend fun createShopWithImages(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("address") address: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("openTime") openTime: RequestBody,
        @Part("closeTime") closeTime: RequestBody,
        @Part("shipFeePerOrder") shipFeePerOrder: RequestBody,
        @Part("minOrderAmount") minOrderAmount: RequestBody,
        @Part coverImage: MultipartBody.Part,
        @Part logo: MultipartBody.Part
    ): Response<CreateShopResponse>
    
    /**
     * GET /owner/shop
     * Lấy thông tin shop của owner hiện tại
     */
    @GET("owner/shop")
    suspend fun getMyShop(): Response<Shop>
    
    /**
     * PUT /owner/shop
     * Cập nhật thông tin shop
     */
    @PUT("owner/shop")
    suspend fun updateShop(
        @Body request: CreateShopRequest
    ): Response<Map<String, String>>
}
