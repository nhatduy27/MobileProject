package com.example.foodapp.data.remote.owner.response

// Minimal DTO to satisfy import in OwnerApiService.kt
// Extend fields/mappers later to match domain `Food` model

data class FoodsResponse(
    val foods: List<FoodDto>
) {
    data class FoodDto(
        val id: String,
        val name: String,
        val price: Int,
        val available: Boolean,
        val imageUrl: String?
    )
}
