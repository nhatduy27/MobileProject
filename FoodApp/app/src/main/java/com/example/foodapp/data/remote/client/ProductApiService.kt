// data/api/client/ProductApiService.kt
package com.example.foodapp.data.remote.client

import com.example.foodapp.data.remote.client.response.product.FavoriteProductsApiResponse
import com.example.foodapp.data.remote.client.response.product.FavoriteResponse
import com.example.foodapp.data.remote.client.response.product.ProductApiResponse
import com.example.foodapp.data.remote.client.response.product.ProductDetailApiResponse
import com.example.foodapp.data.remote.client.response.product.CheckFavoriteResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ProductApiService {

    @GET("products")
    suspend fun getProducts(
        @QueryMap filters: Map<String, String>
    ): ProductApiResponse

    @GET("products/{id}")
    suspend fun getProductDetail(
        @Path("id") productId: String
    ): ProductDetailApiResponse

    @POST("me/favorites/products/{productId}")
    suspend fun addToFavorites(
        @Path("productId") productId: String
    ): FavoriteResponse

    @DELETE("me/favorites/products/{productId}")
    suspend fun deleteFavorites(
        @Path("productId") productId: String
    ): FavoriteResponse

    @GET("me/favorites/products")
    suspend fun getFavoriteProducts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): FavoriteProductsApiResponse

    // Endpoint check favorite hiện tại - giữ nguyên
    @GET("me/favorites/products/{productId}")
    suspend fun checkIsFavorite(
        @Path("productId") productId: String
    ): CheckFavoriteResponse
}