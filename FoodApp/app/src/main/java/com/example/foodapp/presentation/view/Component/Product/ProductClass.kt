package com.example.foodapp.presentation.view.Component.Product
import java.util.UUID


enum class FoodCategory {
    FOOD,
    DRINK,
    SNACK
}

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val priceValue: Double = 0.0,
    val imageRes: Int = 0,
    val category: FoodCategory = FoodCategory.FOOD
)