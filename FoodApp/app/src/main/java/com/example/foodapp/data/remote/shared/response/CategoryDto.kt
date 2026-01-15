package com.example.foodapp.data.remote.shared.response

import com.example.foodapp.data.model.shared.category.Category
import com.google.gson.annotations.SerializedName

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val exception: Exception) : ApiResult<Nothing>()
}

// Response chính từ API
data class CategoryApiResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: CategoryDataWrapper? = null,  // Đây là object wrapper, không phải array

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

// Wrapper bên trong data field
data class CategoryDataWrapper(
    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("data")
    val data: List<CategoryDto>? = null,  // Array categories thực sự ở đây

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

// DTO cho từng category
data class CategoryDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("status")  // API dùng "status", không phải "isActive"
    val status: String? = null,

    @SerializedName("slug")
    val slug: String? = null,

    @SerializedName("displayOrder")
    val displayOrder: Int? = null,

    @SerializedName("productCount")
    val productCount: Int? = null,

    @SerializedName("createdAt")
    val createdAt: Map<String, Any>? = null,

    @SerializedName("updatedAt")
    val updatedAt: Map<String, Any>? = null
)

fun CategoryDto.toCategory(): Category {
    return Category(
        id = id ?: "",
        name = name ?: "",
        description = description,
        icon = icon,
        // Chuyển đổi status string sang boolean
        isActive = status?.equals("active", ignoreCase = true) == true
    )
}