package com.example.foodapp.data.model.owner.product

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Product Entity - Matches backend ProductEntity
 * Collection: products
 * 
 * Backend now returns imageUrls as a list instead of a single imageUrl
 */
@Parcelize
data class Product(
    @SerializedName("id")
    val id: String,

    @SerializedName("shopId")
    val shopId: String,

    @SerializedName("shopName")
    val shopName: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("categoryId")
    val categoryId: String,

    @SerializedName("categoryName")
    val categoryName: String,

    @SerializedName("imageUrls")
    val imageUrls: List<String>? = null,

    @SerializedName("isAvailable")
    val isAvailable: Boolean = true,

    @SerializedName("preparationTime")
    val preparationTime: Int = 15,

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
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = ""
) : Parcelable {
    /**
     * Computed property for backward compatibility
     * Returns the first image URL from the list (main image)
     */
    @IgnoredOnParcel
    val imageUrl: String?
        get() = imageUrls?.firstOrNull()
}

/**
 * Request DTO for creating a product
 */
data class CreateProductRequest(
    val name: String,
    val description: String,
    val price: Double,
    val categoryId: String,
    val preparationTime: Int = 15
)

/**
 * Request DTO for updating a product
 */
data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val categoryId: String? = null,
    val preparationTime: Int? = null
)

/**
 * Request DTO for toggling product availability
 */
data class ToggleAvailabilityRequest(
    val isAvailable: Boolean
)

/**
 * Response wrapper for list of products
 */
data class ProductsResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: ProductsData
)

data class ProductsData(
    @SerializedName("products")
    val products: List<Product>,

    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int
)

/**
 * Response wrapper for single product
 */
data class ProductResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: Product
)

/**
 * Response for image upload
 */
data class ImageUploadResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: ImageUploadData? = null
)

data class ImageUploadData(
    @SerializedName("imageUrls")
    val imageUrls: List<String>
)

/**
 * Generic message response
 */
data class MessageResponse(
    @SerializedName("message")
    val message: String
)
