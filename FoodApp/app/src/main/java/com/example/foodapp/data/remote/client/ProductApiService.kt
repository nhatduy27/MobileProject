// data/api/client/ProductApiService.kt
package com.example.foodapp.data.remote.client

import com.example.foodapp.data.model.client.product.ProductApiResponse
import com.example.foodapp.data.model.client.product.ProductDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ProductApiService {

    @GET("products")
    suspend fun getProducts(
        @QueryMap filters: Map<String, String>
    ): ProductApiResponse

    @GET("products/{id}")
    suspend fun getProductDetail(
        @Path("id") productId: String
    ): ProductDetailResponse
}