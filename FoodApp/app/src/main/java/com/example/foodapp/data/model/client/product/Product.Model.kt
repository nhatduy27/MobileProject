package com.example.foodapp.data.model.client.product

import com.example.foodapp.data.model.shared.product.*
import com.google.gson.annotations.SerializedName

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

// ============== PRODUCT API RESPONSE MODELS ==============

/**
 * Wrapper response cho Product API
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

/**
 * Response cho chi tiết sản phẩm
 */
data class ProductDetailResponse @JvmOverloads constructor(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val product: ProductApiModel? = null,

    @SerializedName("message")
    val message: String? = null
) {
    val isValid: Boolean get() = success && product != null && product.isValid
}

// ============== EXTENSIONS (nên để trong file riêng) ==============

// Extension để convert ProductApiModel sang Product (đã có trong ProductApiModel)
// Nên xóa hàm toLocalProduct() cũ để tránh trùng lặp

// Extension để hỗ trợ FoodCategory (CẬP NHẬT)
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

// Extension để chuyển đổi list
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