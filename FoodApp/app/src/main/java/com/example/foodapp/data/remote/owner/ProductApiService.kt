package com.example.foodapp.data.remote.owner

import com.example.foodapp.data.model.owner.product.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service cho Owner Products
 * Base URL: /api/owner/products
 *
 * Các endpoint:
 * - POST /owner/products - Create product (với nhiều ảnh)
 * - GET /owner/products - Get my products
 * - GET /owner/products/{id} - Get product detail
 * - PUT /owner/products/{id} - Update product
 * - DELETE /owner/products/{id} - Delete product
 * - PUT /owner/products/{id}/availability - Toggle availability
 * - POST /owner/products/{id}/images - Upload nhiều ảnh sản phẩm
 */
interface ProductApiService {

    /**
     * POST /owner/products
     * Create a new product with multiple images (multipart/form-data)
     * 
     * @param images List of product images - backend receives as "images" field
     */
    @Multipart
    @POST("owner/products")
    suspend fun createProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("preparationTime") preparationTime: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<ProductResponse>

    /**
     * GET /owner/products
     * Get all products of owner's shop with optional filters
     */
    @GET("owner/products")
    suspend fun getMyProducts(
        @Query("categoryId") categoryId: String? = null,
        @Query("isAvailable") isAvailable: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ProductsResponse>

    /**
     * GET /owner/products/{id}
     * Get product detail
     */
    @GET("owner/products/{id}")
    suspend fun getProductDetail(
        @Path("id") productId: String
    ): Response<ProductResponse>

    /**
     * PUT /owner/products/{id}
     * Update product WITH new images (multipart/form-data)
     * Use this when user selects new images
     */
    @Multipart
    @PUT("owner/products/{id}")
    suspend fun updateProductWithImages(
        @Path("id") productId: String,
        @Part("name") name: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("categoryId") categoryId: RequestBody? = null,
        @Part("preparationTime") preparationTime: RequestBody? = null,
        @Part images: List<MultipartBody.Part>
    ): Response<MessageResponse>

    /**
     * PUT /owner/products/{id}
     * Update product WITHOUT new images (multipart/form-data)
     * Use this when user keeps existing images
     */
    @Multipart
    @PUT("owner/products/{id}")
    suspend fun updateProductWithoutImages(
        @Path("id") productId: String,
        @Part("name") name: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("categoryId") categoryId: RequestBody? = null,
        @Part("preparationTime") preparationTime: RequestBody? = null
    ): Response<MessageResponse>

    /**
     * PUT /owner/products/{id}/availability
     * Toggle product availability
     */
    @PUT("owner/products/{id}/availability")
    suspend fun toggleAvailability(
        @Path("id") productId: String,
        @Body request: ToggleAvailabilityRequest
    ): Response<MessageResponse>

    /**
     * DELETE /owner/products/{id}
     * Delete product (soft delete)
     */
    @DELETE("owner/products/{id}")
    suspend fun deleteProduct(
        @Path("id") productId: String
    ): Response<MessageResponse>

    /**
     * POST /owner/products/{id}/images
     * Upload multiple product images
     * 
     * @param images List of images to upload
     */
    @Multipart
    @POST("owner/products/{id}/images")
    suspend fun uploadProductImages(
        @Path("id") productId: String,
        @Part images: List<MultipartBody.Part>
    ): Response<ImageUploadResponse>
}
