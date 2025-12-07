package com.example.foodapp.data.remote.model

data class MenuItemRemote(
    val id: String? = null,
    val restaurantId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Long? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean? = null,
    val preparationTime: Int? = null,
    val rating: Double? = null,
    val totalOrders: Int? = null,
    val createdAt: Long? = null
)
