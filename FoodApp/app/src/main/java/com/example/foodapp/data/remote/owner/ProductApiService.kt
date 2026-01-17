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
 * - POST /owner/products - Create product
 * - GET /owner/products - Get my products
 * - GET /owner/products/{id} - Get product detail
 * - PUT /owner/products/{id} - Update product
 * - DELETE /owner/products/{id} - Delete product
 * - PUT /owner/products/{id}/availability - Toggle availability
 * - POST /owner/products/{id}/image - Upload product image
 */
interface ProductApiService {

    /**
     * POST /owner/products
     * Create a new product with image (multipart/form-data)
     */
    @Multipart
    @POST("owner/products")
    suspend fun createProduct(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("categoryId") categoryId: RequestBody,
        @Part("preparationTime") preparationTime: RequestBody,
        @Part image: MultipartBody.Part
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
     * Update product (multipart/form-data, image optional)
     */
    @Multipart
    @PUT("owner/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: String,
        @Part("name") name: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("categoryId") categoryId: RequestBody? = null,
        @Part("preparationTime") preparationTime: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
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
     * POST /owner/products/{id}/image
     * Upload product image
     */
    @Multipart
    @POST("owner/products/{id}/image")
    suspend fun uploadProductImage(
        @Path("id") productId: String,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>
}
