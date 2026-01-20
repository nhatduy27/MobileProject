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
 *     "isFavorited": true      <-- TRỰC TIẾP, không có lớp wrapper bên trong
 *   },
 *   "timestamp": "2026-01-17T07:20:00.410Z"
 * }
 */
data class CheckFavoriteResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: CheckFavoriteData? = null, // <-- CHỈNH: Data trực tiếp, không phải wrapper

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
) {
    // Tính toán isFavorited đơn giản
    val isFavorited: Boolean
        get() = data?.isFavorited == true

    val isValid: Boolean get() = success && data != null
}

/**
 * Data chứa isFavorited (trực tiếp)
 * Format: {
 *   "isFavorited": boolean
 * }
 */
data class CheckFavoriteData @JvmOverloads constructor(
    @SerializedName("isFavorited")
    val isFavorited: Boolean = false
)

// Extension để lấy isFavorited
fun CheckFavoriteResponse.isFavorited(): Boolean {
    return isFavorited
}

// ============== BASE FAVORITE RESPONSE ==============

data class FavoriteResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String = ""
) {
    val isSuccess: Boolean get() = success
}

// ============== FAVORITE PRODUCTS API RESPONSE (WRAPPER) ==============

/**
 * WRAPPER response cho danh sách sản phẩm yêu thích từ API
 * Format: {
 *   "success": true,
 *   "data": FavoriteProductsData,
 *   "timestamp": "2026-01-16T19:47:14.275Z"
 * }
 */
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

/**
 * DATA chứa danh sách thực sự và phân trang
 * Format: {
 *   "data": [FavoriteProductItem],
 *   "pagination": {
 *     "total": 0,
 *     "page": 1,
 *     "limit": 20,
 *     "hasMore": false
 *   }
 * }
 */
data class FavoriteProductsData @JvmOverloads constructor(
    @SerializedName("data")
    val data: List<FavoriteProductItem> = emptyList(),  // ← Chỉ có data và pagination, không có success

    @SerializedName("pagination")
    val pagination: FavoritePagination? = null
) {
    val isEmpty: Boolean get() = data.isEmpty()
    val hasData: Boolean get() = data.isNotEmpty()
}

/**
 * Item sản phẩm yêu thích trong danh sách
 * Format: {
 *   "productId": "prod_123",
 *   "productName": "Cơm sườn",
 *   "productPrice": 35000,
 *   "productImage": "...",
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
    // Helper properties
    val formattedPrice: String get() = "%,.0f".format(productPrice) + "đ"
    val hasImage: Boolean get() = !productImage.isNullOrBlank()

    // Convert sang ProductApiModel nếu cần
    fun toProductApiModel(): ProductApiModel {
        return ProductApiModel(
            id = productId,
            name = productName,
            price = productPrice,
            imageUrl = productImage,
            shopId = shopId,
            shopName = shopName,
            createdAt = createdAt
        )
    }

    // Convert sang Product local model
    fun toProduct(): Product {
        return Product(
            id = productId,
            name = productName,
            price = formattedPrice,
            priceValue = productPrice,
            imageUrl = productImage,
            shopId = shopId,
            shopName = shopName,
            createdAt = createdAt,
            isAvailable = true,
            description = "",
            category = FoodCategory.FOOD
        )
    }
}

/**
 * Phân trang cho danh sách yêu thích
 * Format: {
 *   "total": 10,
 *   "page": 1,
 *   "limit": 20,
 *   "hasMore": false
 * }
 */
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
    // Tính toán total pages
    val totalPages: Int get() = if (total == 0) 1 else (total + limit - 1) / limit

    // Kiểm tra có dữ liệu không
    val hasData: Boolean get() = total > 0
}

/**
 * DTO cho query parameters khi lấy danh sách yêu thích
 * Sẽ được convert thành: ?page=1&limit=20
 */
data class FavoriteQueryParams @JvmOverloads constructor(
    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20
) {
    fun toQueryMap(): Map<String, String> {
        return mapOf(
            "page" to page.toString(),
            "limit" to limit.toString()
        )
    }

    // Copy với page mới (cho load more)
    fun copyWithPage(newPage: Int): FavoriteQueryParams {
        return this.copy(page = newPage)
    }
}

// ============== FAVORITE EXTENSIONS ==============

// Extension để convert FavoriteProductsApiResponse sang list Product
fun FavoriteProductsApiResponse.toProductList(): List<Product> {
    return if (isValid && data != null) {
        data.data.map { it.toProduct() }
    } else {
        emptyList()
    }
}

// Extension để lấy pagination từ wrapper response
fun FavoriteProductsApiResponse.getPagination(): FavoritePagination? {
    return data?.pagination
}

// Extension để lấy inner data dễ dàng
fun FavoriteProductsApiResponse.getInnerData(): FavoriteProductsData? {
    return if (isValid) data else null
}

// Extension cho FavoriteProductsData
fun FavoriteProductsData.toProductList(): List<Product> {
    return data.map { it.toProduct() }
}

// Extension để lấy productIds từ danh sách yêu thích
fun FavoriteProductsData.getProductIds(): List<String> {
    return data.map { it.productId }
}

// Extension để kiểm tra sản phẩm có trong danh sách yêu thích không
fun FavoriteProductsData.containsProduct(productId: String): Boolean {
    return data.any { it.productId == productId }
}

// Extension cho phân trang
fun FavoritePagination.isFirstPage(): Boolean = page == 1
fun FavoritePagination.isLastPage(): Boolean = !hasMore
fun FavoritePagination.nextPage(): Int = page + 1

// ============== PRODUCT API RESPONSE MODELS ==============

/**
 * Wrapper response cho Product API (danh sách sản phẩm)
 * Format: {
 *   "success": boolean,
 *   "data": ProductListData,
 *   "message": string,
 *   "timestamp": string
 * }
 */
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

/**
 * Response cho chi tiết sản phẩm từ API
 * Format: {
 *   "success": boolean,
 *   "data": ProductApiModel,  // ← CHÚ Ý: data là ProductApiModel trực tiếp, không phải ProductListData
 *   "message": string,
 *   "timestamp": string
 * }
 */
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

/**
 * Data chứa danh sách sản phẩm và phân trang
 * Format: {
 *   "products": [ProductApiModel],
 *   "total": number,
 *   "page": number,
 *   "limit": number
 * }
 */
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
 * Model sản phẩm từ API
 * Format: {
 *   "id": string,
 *   "shopId": string,
 *   "shopName": string,
 *   "name": string,
 *   "description": string,
 *   "price": number,
 *   "categoryId": string,
 *   "categoryName": string,
 *   "imageUrl": string,
 *   "isAvailable": boolean,
 *   "preparationTime": number,
 *   "rating": number,
 *   "totalRatings": number,
 *   "soldCount": number,
 *   "sortOrder": number,
 *   "isDeleted": boolean,
 *   "createdAt": string,
 *   "updatedAt": string
 * }
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

    @SerializedName("imageUrl")
    val imageUrl: String? = null,

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
    // Helper methods
    val formattedPrice: String get() = "%,.0f".format(price) + "đ"
    val hasImage: Boolean get() = !imageUrl.isNullOrBlank()
    val isInStock: Boolean get() = isAvailable && !isDeleted
    val ratingText: String get() = if (totalRatings > 0) "%.1f".format(rating) else "Mới"
    val soldCountText: String get() = when {
        soldCount >= 1000000 -> "%.1fM".format(soldCount / 1000000.0)
        soldCount >= 1000 -> "%.1fk".format(soldCount / 1000.0)
        else -> soldCount.toString()
    }

    // Định dạng thời gian chuẩn bị
    val preparationTimeText: String get() = preparationTime?.let { "$it phút" } ?: "10-15 phút"

    // Kiểm tra sản phẩm hợp lệ
    val isValid: Boolean get() = id.isNotBlank() && name.isNotBlank() && !isDeleted

    // Convert sang local Product model
    fun toProduct(): Product {
        return Product(
            id = id,
            name = name,
            description = description,
            price = formattedPrice,
            priceValue = price,
            category = FoodCategory.fromName(categoryName),
            imageUrl = imageUrl,
            shopId = shopId,
            shopName = shopName,
            rating = rating,
            totalRatings = totalRatings,
            soldCount = soldCount,
            isAvailable = isAvailable,
            preparationTime = preparationTime ?: 15,
            isDeleted = isDeleted,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

// ============== PRODUCT FILTER DTO ==============

/**
 * DTO cho query parameters khi gọi API product feed
 * Sẽ được convert thành query string: ?categoryId=cat_123&q=phở&page=1&limit=20
 */
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
    val sort: String? = null, // "newest", "popular", "rating", "price"

    @SerializedName("page")
    val page: Int = 1,

    @SerializedName("limit")
    val limit: Int = 20,

    @SerializedName("isAvailable")
    val isAvailable: Boolean? = null
) {
    // Convert thành Map<String, String> cho query parameters
    fun toQueryMap(): Map<String, String> {
        return buildMap {
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
    }

    // Check xem có filter nào không
    val hasFilters: Boolean get() =
        !categoryId.isNullOrBlank() ||
                !shopId.isNullOrBlank() ||
                !searchQuery.isNullOrBlank() ||
                minPrice != null ||
                maxPrice != null ||
                sort != null

    // Copy với page mới (cho phân trang)
    fun copyWithPage(newPage: Int): ProductFilterDto {
        return this.copy(page = newPage)
    }
}

// ============== REQUEST/API MODELS ==============

/**
 * Request cho lấy chi tiết sản phẩm
 */
data class ProductDetailRequest(
    @SerializedName("id")
    val productId: String
)

// ============== EXTENSIONS ==============

// Extension để hỗ trợ FoodCategory
fun FoodCategory.Companion.fromName(name: String): FoodCategory {
    return when (name.trim().lowercase()) {
        "cơm", "com", "rice", "món ăn", "food", "đồ ăn" -> FoodCategory.FOOD
        "nước", "nuoc", "drink", "đồ uống", "beverage", "thức uống" -> FoodCategory.DRINK
        "snack", "ăn vặt", "an vat", "đồ ăn vặt", "món phụ" -> FoodCategory.SNACK
        "all", "tất cả", "", "null" -> FoodCategory.ALL
        else -> {
            // Log để debug nếu có category mới
            println("⚠️ Unknown category from API: '$name', defaulting to FOOD")
            FoodCategory.FOOD
        }
    }
}

// Extension để chuyển đổi list ProductApiModel
fun List<ProductApiModel>.toProductList(): List<Product> {
    return this.mapNotNull { apiModel ->
        if (apiModel.isValid) apiModel.toProduct() else null
    }
}

// Extension cho ProductApiResponse để lấy products dễ dàng
fun ProductApiResponse.toProductList(): List<Product> {
    return if (isValid && data != null) {
        data.products.toProductList()
    } else {
        emptyList()
    }
}

// Extension cho ProductListData
fun ProductListData.toProductList(): List<Product> {
    return products.toProductList()
}

// Extension để kiểm tra sản phẩm có trong danh sách không (tiện lợi)
fun FavoriteProductsData.isProductFavorite(productId: String): Boolean {
    return containsProduct(productId)
}

// Extension để lấy danh sách productIds từ FavoriteProductsApiResponse
fun FavoriteProductsApiResponse.getProductIds(): List<String> {
    return data?.getProductIds() ?: emptyList()
}

// Extension để kiểm tra sản phẩm có trong danh sách yêu thích từ wrapper response
fun FavoriteProductsApiResponse.isProductFavorite(productId: String): Boolean {
    return data?.containsProduct(productId) ?: false
}

// ============== NEW EXTENSIONS FOR CHECK FAVORITE ==============

// Extension để parse check favorite response
fun CheckFavoriteResponse.toApiResult(): ApiResult<Boolean> {
    return if (success && data != null) {
        ApiResult.Success(data.isFavorited)
    } else {
        ApiResult.Failure(Exception(message ?: "Check favorite failed"))
    }
}

// Extension để get boolean directly
fun CheckFavoriteResponse.getIsFavorited(): Boolean = data?.isFavorited ?: false

// Extension cho repository để parse response đúng
fun CheckFavoriteResponse.parseToBoolean(): Boolean {
    println("DEBUG: [CheckFavoriteResponse] Parsing: success=$success, data=$data, isFavorited=${data?.isFavorited}")
    return data?.isFavorited ?: false
}