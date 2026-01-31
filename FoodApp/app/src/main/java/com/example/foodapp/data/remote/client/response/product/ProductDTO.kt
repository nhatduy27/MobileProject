package com.example.foodapp.data.remote.client.response.product

import com.example.foodapp.data.model.shared.product.*
import com.google.gson.annotations.SerializedName

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}


/**
 * Response cho API check favorite status
 * Format thực tế từ API (dựa vào log):
 * {
 *   "success": true,
 *   "data": {
 *     "isFavorited": true
 *   },
 *   "timestamp": "2026-01-17T07:20:00.410Z"
 * }
 */
data class CheckFavoriteResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: CheckFavoriteData? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
) {
    val isFavorited: Boolean get() = data?.isFavorited == true
    val isValid: Boolean get() = success && data != null
}

data class CheckFavoriteData @JvmOverloads constructor(
    @SerializedName("isFavorited")
    val isFavorited: Boolean = false
)

fun CheckFavoriteResponse.isFavorited(): Boolean = isFavorited

// ============== BASE FAVORITE RESPONSE ==============

data class FavoriteResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
) {
    val isSuccess: Boolean get() = success
}

// ============== FAVORITE PRODUCTS API RESPONSE ==============

data class FavoriteProductsApiResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: FavoriteProductsData? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
) {
    val isValid: Boolean get() = success && data != null
    val isEmpty: Boolean get() = data?.data?.isEmpty() ?: true
}

data class FavoriteProductsData @JvmOverloads constructor(
    @SerializedName("data")
    val data: List<FavoriteProductItem> = emptyList(),

    @SerializedName("pagination")
    val pagination: FavoritePagination? = null
) {
    val isEmpty: Boolean get() = data.isEmpty()
    val hasData: Boolean get() = data.isNotEmpty()
}

/**
 * Item sản phẩm yêu thích
 * Format: {
 *   "productId": "prod_123",
 *   "productName": "Cơm sườn",
 *   "productPrice": 35000,
 *   "productImages": ["url1", "url2"], // ĐỔI: productImage -> productImages (List)
 *   "shopId": "shop_abc",
 *   "shopName": "Quán A Mập",
 *   "createdAt": "2026-01-05T10:00:00Z"
 * }
 */
data class FavoriteProductItem @JvmOverloads constructor(
    @SerializedName("productId")
    val productId: String = "",

    @SerializedName("productName")
    val productName: String = "",

    @SerializedName("productPrice")
    val productPrice: Double = 0.0,

    @SerializedName("productImage")
    val productImage: String? = null,

    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("shopName")
    val shopName: String = "",

    @SerializedName("createdAt")
    val createdAt: String = ""
) {
    val formattedPrice: String get() = "%,.0f".format(productPrice) + "đ"

    val imageUrls: List<String> get() = if (!productImage.isNullOrBlank()) listOf(productImage) else emptyList()

    fun toProduct(): Product {
        return Product(
            id = productId,
            name = productName,
            description = "",
            price = formattedPrice,
            priceValue = productPrice,
            category = FoodCategory.FOOD,
            imageUrls = imageUrls,
            shopId = shopId,
            shopName = shopName,
            rating = 0.0,
            totalRatings = 0,
            soldCount = 0,
            isAvailable = true,
            preparationTime = 15,
            isDeleted = false,
            createdAt = createdAt,
            updatedAt = null,
            isFavorite = true
        )
    }
}
data class FavoritePagination @JvmOverloads constructor(
    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20,

    @SerializedName("hasMore")
    val hasMore: Boolean = false
) {
    val totalPages: Int get() = if (total == 0) 1 else (total + limit - 1) / limit
    val hasData: Boolean get() = total > 0
}

data class FavoriteQueryParams @JvmOverloads constructor(
    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20
) {
    fun toQueryMap(): Map<String, String> = mapOf(
        "page" to page.toString(),
        "limit" to limit.toString()
    )

    fun copyWithPage(newPage: Int): FavoriteQueryParams = copy(page = newPage)
}

// ============== FAVORITE EXTENSIONS ==============

fun FavoriteProductsApiResponse.toProductList(): List<Product> = data?.data?.map { it.toProduct() } ?: emptyList()
fun FavoriteProductsApiResponse.getPagination(): FavoritePagination? = data?.pagination
fun FavoriteProductsApiResponse.getInnerData(): FavoriteProductsData? = if (isValid) data else null
fun FavoriteProductsData.toProductList(): List<Product> = data.map { it.toProduct() }
fun FavoriteProductsData.getProductIds(): List<String> = data.map { it.productId }
fun FavoriteProductsData.containsProduct(productId: String): Boolean = data.any { it.productId == productId }
fun FavoritePagination.isFirstPage(): Boolean = page == 1
fun FavoritePagination.isLastPage(): Boolean = !hasMore
fun FavoritePagination.nextPage(): Int = page + 1

// ============== PRODUCT API RESPONSE MODELS ==============

data class ProductApiResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: ProductListData? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
) {
    val isValid: Boolean get() = success && data != null
}

data class ProductDetailApiResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: ProductApiModel? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
) {
    val isValid: Boolean get() = success && data != null && data.isValid
}

data class ProductListData @JvmOverloads constructor(
    @SerializedName("products")
    val products: List<ProductApiModel> = emptyList(),

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20
) {
    val hasMore: Boolean get() = (page * limit) < total
    val isEmpty: Boolean get() = products.isEmpty()
}

/**
 * Model sản phẩm từ API - KHỚP VỚI Product local model
 */
data class ProductApiModel @JvmOverloads constructor(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("shopId")
    val shopId: String = "",

    @SerializedName("shopName")
    val shopName: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName("categoryId")
    val categoryId: String = "",

    @SerializedName("categoryName")
    val categoryName: String = "",

    @SerializedName("imageUrls") // KHỚP: imageUrls: List<String>
    val imageUrls: List<String> = emptyList(),

    @SerializedName("isAvailable")
    val isAvailable: Boolean = true,

    @SerializedName("preparationTime")
    val preparationTime: Int? = null,

    @SerializedName("rating")
    val rating: Double = 0.0,

    @SerializedName("totalRatings")
    val totalRatings: Int = 0,

    @SerializedName("soldCount")
    val soldCount: Int = 0,

    @SerializedName("sortOrder")
    val sortOrder: Int = 0,

    @SerializedName("isDeleted")
    val isDeleted: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    // Helper methods - KHỚP với Product local model
    val formattedPrice: String get() = "%,.0f".format(price) + "đ"
    val hasImage: Boolean get() = imageUrls.isNotEmpty()
    val mainImageUrl: String? get() = imageUrls.firstOrNull()
    val additionalImageUrls: List<String> get() = imageUrls.drop(1)
    val isInStock: Boolean get() = isAvailable && !isDeleted
    val ratingText: String get() = if (totalRatings > 0) "%.1f".format(rating) else "Mới"
    val imageCount: Int get() = imageUrls.size

    val soldCountText: String get() = when {
        soldCount >= 1000000 -> "%.1fM".format(soldCount / 1000000.0)
        soldCount >= 1000 -> "%.1fk".format(soldCount / 1000.0)
        else -> soldCount.toString()
    }

    val preparationTimeText: String get() = preparationTime?.let { "$it phút" } ?: "10-15 phút"
    val isValid: Boolean get() = id.isNotBlank() && name.isNotBlank() && !isDeleted

    // Convert sang Product local model - KHỚP HOÀN TOÀN
    fun toProduct(): Product {
        return Product(
            id = id,
            name = name,
            description = description,
            price = formattedPrice,
            priceValue = price,
            category = FoodCategory.fromName(categoryName),
            imageUrls = imageUrls, // KHỚP: List<String>
            shopId = shopId,
            shopName = shopName,
            rating = rating,
            totalRatings = totalRatings,
            soldCount = soldCount,
            isAvailable = isAvailable,
            preparationTime = preparationTime ?: 15,
            isDeleted = isDeleted,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isFavorite = false
        )
    }
}

// ============== PRODUCT FILTER DTO ==============

data class ProductFilterDto @JvmOverloads constructor(
    @SerializedName("categoryId")
    val categoryId: String? = null,

    @SerializedName("shopId")
    val shopId: String? = null,

    @SerializedName("q")
    val searchQuery: String? = null,

    @SerializedName("minPrice")
    val minPrice: Double? = null,

    @SerializedName("maxPrice")
    val maxPrice: Double? = null,

    @SerializedName("sort")
    val sort: String? = null,

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20,

    @SerializedName("isAvailable")
    val isAvailable: Boolean? = null
) {
    fun toQueryMap(): Map<String, String> = buildMap {
        categoryId?.let { put("categoryId", it) }
        shopId?.let { put("shopId", it) }
        searchQuery?.let { put("q", it) }
        minPrice?.let { put("minPrice", it.toString()) }
        maxPrice?.let { put("maxPrice", it.toString()) }
        sort?.let { put("sort", it) }
        put("page", page.toString())
        put("limit", limit.toString())
        isAvailable?.let { put("isAvailable", it.toString()) }
    }

    val hasFilters: Boolean get() =
        !categoryId.isNullOrBlank() ||
                !shopId.isNullOrBlank() ||
                !searchQuery.isNullOrBlank() ||
                minPrice != null ||
                maxPrice != null ||
                sort != null

    fun copyWithPage(newPage: Int): ProductFilterDto = copy(page = newPage)
}

// ============== REQUEST/API MODELS ==============

data class ProductDetailRequest(
    @SerializedName("id")
    val productId: String
)

data class SearchProductsRequestDto @JvmOverloads constructor(
    @SerializedName("q")
    val q: String = "",

    @SerializedName("shopId")
    val shoplid: String? = null,

    @SerializedName("categoryId")
    val categoryld: String? = null,

    @SerializedName("minPrice")
    val minPrice: Double? = null,

    @SerializedName("maxPrice")
    val maxPrice: Double? = null,

    @SerializedName("limit")
    val limit: Int = 20
) {
    val isValid: Boolean get() = q.length >= 2

    fun toQueryMap(): Map<String, String> = buildMap {
        if (q.isNotBlank()) put("q", q)
        shoplid?.let { put("shoplid", it) }
        categoryld?.let { put("categoryld", it) }
        minPrice?.let { put("minPrice", it.toString()) }
        maxPrice?.let { put("maxPrice", it.toString()) }
        put("limit", limit.toString())
    }
}

data class SearchProductsApiResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: SearchProductsData? = null
) {
    val isValid: Boolean get() = success && data != null
}

data class SearchProductsData @JvmOverloads constructor(
    @SerializedName("products")
    val products: List<ProductApiModel> = emptyList(),

    @SerializedName("total")
    val total: Int = 0
) {
    val hasData: Boolean get() = products.isNotEmpty()
    val isEmpty: Boolean get() = products.isEmpty()
}

// ============== EXTENSIONS ==============

// KHỚP VỚI FoodCategory trong Product local model
fun FoodCategory.Companion.fromName(name: String): FoodCategory {
    return when (name.lowercase()) {
        "all", "tất cả" -> FoodCategory.ALL
        "food", "món ăn", "com", "cơm" -> FoodCategory.FOOD
        "drink", "đồ uống", "nước", "trà sữa & đồ uống" -> FoodCategory.DRINK
        "snack", "ăn vặt", "đồ ăn vặt" -> FoodCategory.SNACK
        else -> FoodCategory.ALL
    }
}

fun List<ProductApiModel>.toProductList(): List<Product> = mapNotNull {
    if (it.isValid) it.toProduct() else null
}

fun ProductApiResponse.toProductList(): List<Product> =
    if (isValid && data != null) data.products.toProductList() else emptyList()

fun ProductListData.toProductList(): List<Product> = products.toProductList()
fun FavoriteProductsData.isProductFavorite(productId: String): Boolean = containsProduct(productId)
fun FavoriteProductsApiResponse.getProductIds(): List<String> = data?.getProductIds() ?: emptyList()
fun FavoriteProductsApiResponse.isProductFavorite(productId: String): Boolean = data?.containsProduct(productId) ?: false

// ============== CHECK FAVORITE EXTENSIONS ==============

fun CheckFavoriteResponse.toApiResult(): ApiResult<Boolean> =
    if (success && data != null) ApiResult.Success(data.isFavorited)
    else ApiResult.Failure(Exception(message ?: "Check favorite failed"))

fun CheckFavoriteResponse.getIsFavorited(): Boolean = data?.isFavorited ?: false

fun CheckFavoriteResponse.parseToBoolean(): Boolean {
    println("DEBUG: [CheckFavoriteResponse] Parsing: success=$success, data=$data, isFavorited=${data?.isFavorited}")
    return data?.isFavorited ?: false
}