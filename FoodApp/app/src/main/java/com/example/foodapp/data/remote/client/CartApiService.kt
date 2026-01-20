package com.example.foodapp.data.remote.client


import com.example.foodapp.data.remote.client.response.cart.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApiService {

    @POST("cart/items")
    suspend fun addToCart(
        @Body request: AddToCartRequest
    ): AddToCartResponse

    @GET("cart")
    suspend fun getCart(): GetCartResponse

    @DELETE("cart")
    suspend fun clearCart(): Response<Unit>

    @DELETE("cart/items/{productId}")
    suspend fun removeCartItem(
        @Path("productId") productId: String
    ): Response<DeleteItemFromCartResponse>

    @PUT("cart/items/{productId}")
    suspend fun updateItemQuantity(
        @Path("productId") productId: String,
        @Body request: UpdateQuantityRequest
    ): Response<UpdateQuantityResponse>

    @DELETE("cart/shops/{shopId}")
    suspend fun deleteShopItems(
        @Path("shopId") shopId: String
    ): DeleteShopItemsResponse

    @GET("cart/shops/{shopId}")
    suspend fun getCartByShop(
        @Path("shopId") shopId: String
    ): GetCartByShopResponse

}