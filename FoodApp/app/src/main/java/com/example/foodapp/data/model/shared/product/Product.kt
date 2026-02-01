package com.example.foodapp.data.model.shared.product

import android.os.Parcelable
import com.example.foodapp.data.remote.client.response.product.ProductApiModel
import kotlinx.parcelize.Parcelize

// Enum để phân loại món ăn
enum class FoodCategory {
    ALL, FOOD, DRINK, SNACK;

    companion object {
        fun fromName(name: String): FoodCategory {
            return when (name.lowercase()) {
                "all", "tất cả" -> ALL
                "food", "món ăn", "com", "cơm" -> FOOD
                "drink", "đồ uống", "nước", "trà sữa & đồ uống" -> DRINK
                "snack", "ăn vặt", "đồ ăn vặt" -> SNACK
                else -> ALL
            }
        }

        fun toApiString(category: FoodCategory): String? {
            return when (category) {
                ALL -> null
                FOOD -> "FOOD"
                DRINK -> "DRINK"
                SNACK -> "SNACK"
            }
        }
    }
}

@Parcelize
data class Product(
    // Các field cơ bản
    val id: String = "",
    val name: String,
    val description: String,
    val price: String,
    val priceValue: Double,
    val category: FoodCategory,

    val imageRes: Int? = null,
    val imageUrls: List<String> = emptyList(),

    val shopId: String = "",
    val shopName: String = "",
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    val soldCount: Int = 0,
    val isAvailable: Boolean = true,
    val preparationTime: Int = 15,
    val isDeleted: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,

    val isFavorite: Boolean = false
) : Parcelable {

    // Lấy ảnh chính (ảnh đầu tiên trong danh sách)
    val mainImageUrl: String? get() = imageUrls.firstOrNull()

    val displayPrice: String get() = price
    val hasLocalImage: Boolean get() = imageRes != null
    val hasRemoteImage: Boolean get() = imageUrls.isNotEmpty() // ĐỔI: Kiểm tra list không rỗng
    val hasAnyImage: Boolean get() = hasLocalImage || hasRemoteImage
    val isInStock: Boolean get() = isAvailable && !isDeleted

    // Format rating
    val ratingText: String get() = "%.1f".format(rating)

    // Format số lượng bán
    val soldCountText: String get() {
        return when {
            soldCount >= 1000000 -> "%.1fM".format(soldCount / 1000000.0)
            soldCount >= 1000 -> "%.1fk".format(soldCount / 1000.0)
            else -> soldCount.toString()
        }
    }

    // Thời gian chuẩn bị
    val preparationTimeText: String get() = "$preparationTime phút"

    // Số lượng ảnh
    val imageCount: Int get() = imageUrls.size

    // Kiểm tra sản phẩm hợp lệ
    val isValid: Boolean get() = id.isNotBlank() && name.isNotBlank() && !isDeleted

    // Factory method từ ProductApiModel
    companion object {
        fun fromApiModel(apiModel: ProductApiModel): Product {
            return Product(
                id = apiModel.id,
                name = apiModel.name,
                description = apiModel.description,
                price = apiModel.formattedPrice,
                priceValue = apiModel.price,
                category = FoodCategory.fromName(apiModel.categoryName),
                imageUrls = apiModel.imageUrls, // ĐỔI: Dùng imageUrls thay vì mainImageUrl
                shopId = apiModel.shopId,
                shopName = apiModel.shopName,
                rating = apiModel.rating,
                totalRatings = apiModel.totalRatings,
                soldCount = apiModel.soldCount,
                isAvailable = apiModel.isAvailable,
                preparationTime = apiModel.preparationTime ?: 15,
                isDeleted = apiModel.isDeleted,
                createdAt = apiModel.createdAt,
                updatedAt = apiModel.updatedAt,
                isFavorite = false
            )
        }

        // Tạo dummy product cho testing
        fun createDummy(): Product {
            return Product(
                id = "prod_dummy_${System.currentTimeMillis()}",
                name = "Sản phẩm mẫu",
                description = "Mô tả sản phẩm mẫu",
                price = "50,000đ",
                priceValue = 50000.0,
                category = FoodCategory.FOOD,
                imageRes = null,
                imageUrls = listOf( // ĐỔI: Tạo list ảnh
                    "https://picsum.photos/200/300",
                    "https://picsum.photos/201/300",
                    "https://picsum.photos/202/300"
                ),
                shopId = "shop_dummy",
                shopName = "Cửa hàng mẫu",
                rating = 4.5,
                totalRatings = 120,
                soldCount = 500,
                isAvailable = true,
                preparationTime = 20,
                isFavorite = false
            )
        }
    }

    // Update methods (immutable)
    fun copyWithImageUrls(imageUrls: List<String>): Product { // ĐỔI: Đổi tên method
        return this.copy(imageUrls = imageUrls)
    }

    fun copyWithAvailability(isAvailable: Boolean): Product {
        return this.copy(isAvailable = isAvailable)
    }

    fun copyWithRating(newRating: Double, newTotalRatings: Int): Product {
        return this.copy(
            rating = newRating,
            totalRatings = newTotalRatings
        )
    }

    fun copyWithFavorite(isFavorite: Boolean): Product {
        return this.copy(isFavorite = isFavorite)
    }

    // THÊM: Thêm ảnh vào danh sách
    fun addImageUrl(imageUrl: String): Product {
        val newList = imageUrls.toMutableList().apply {
            add(imageUrl)
        }
        return this.copy(imageUrls = newList)
    }

    // THÊM: Xóa ảnh khỏi danh sách
    fun removeImageUrl(imageUrl: String): Product {
        val newList = imageUrls.toMutableList().apply {
            remove(imageUrl)
        }
        return this.copy(imageUrls = newList)
    }
}

// Extension function để chuyển đổi
fun ProductApiModel.toProduct(): Product {
    return Product.fromApiModel(this)
}

// List extension
fun List<ProductApiModel>.toProductList(): List<Product> {
    return this.map { it.toProduct() }
}