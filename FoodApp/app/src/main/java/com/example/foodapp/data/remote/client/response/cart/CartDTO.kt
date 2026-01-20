package com.example.foodapp.data.remote.client.response.cart

import com.google.gson.annotations.SerializedName

// ========== API RESULT ==========
sealed class CartApiResult<out T> {
    data class Success<out T>(val data: T) : CartApiResult<T>()
    data class Failure(val exception: Exception) : CartApiResult<Nothing>()
}

// ========== REQUEST ==========
data class AddToCartRequest(
    @SerializedName("productId")
    val productId: String,

    @SerializedName("quantity")
    val quantity: Int
)

// ========== RESPONSE ==========
data class AddToCartResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: CartData
)

data class CartData(
    @SerializedName("id")
    val id: String,

    @SerializedName("groups")
    val groups: List<CartGroup>
)

data class CartGroup(
    @SerializedName("shopId")
    val shopId: String,

    @SerializedName("shopName")
    val shopName: String,

    @SerializedName("isOpen")
    val isOpen: Boolean,

    @SerializedName("shipFee")
    val shipFee: Int,

    @SerializedName("items")
    val items: List<CartItem>,

    @SerializedName("subtotal")
    val subtotal: Int
)

data class CartItem(
    @SerializedName("productId")
    val productId: String,

    @SerializedName("shopId")
    val shopId: String,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("productImage")
    val productImage: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("price")
    val price: Int,

    @SerializedName("subtotal")
    val subtotal: Int,

    @SerializedName("addedAt")
    val addedAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)


// ========== GET CART RESPONSE ==========
data class GetCartResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: GetCartData,

    @SerializedName("timestamp")
    val timestamp: String
)

data class GetCartData(
    @SerializedName("groups")
    val groups: List<CartGroup>,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("totalGroups")
    val totalGroups: Int,

    @SerializedName("totalPages")
    val totalPages: Int
)



// ========== ERROR RESPONSE FOR CLEAR CART ==========
data class ClearCartErrorResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("errorCode")
    val errorCode: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timestamp: String
)

// ==========  RESPONSE (Generic) ==========
data class DeleteItemFromCartResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("errorCode")
    val errorCode: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timestamp: String
)


// ========== UPDATE QUANTITY REQUEST ==========
data class UpdateQuantityRequest(
    @SerializedName("quantity")
    val quantity: Int
)

// ========== UPDATE QUANTITY RESPONSE ==========
data class UpdateQuantityResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: CartData? = null,

    @SerializedName("errorCode")
    val errorCode: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

// ========== COMMON ERROR RESPONSE ==========
data class CartErrorResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("errorCode")
    val errorCode: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timestamp: String
)


// ========== DELETE SHOP ITEMS FROM CART RESPONSE ==========
data class DeleteShopItemsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: DeleteShopItemsData?,

    @SerializedName("errorCode")
    val errorCode: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

data class DeleteShopItemsData(
    @SerializedName("removedCount")
    val removedCount: Int,

    @SerializedName("groups")
    val groups: List<CartGroup>
)


// ========== GET CART BY SHOP RESPONSE ==========
data class GetCartByShopResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: CartByShopData? = null,

    @SerializedName("errorCode")
    val errorCode: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String
)

data class CartByShopData(
    @SerializedName("group")
    val group: CartGroup
)