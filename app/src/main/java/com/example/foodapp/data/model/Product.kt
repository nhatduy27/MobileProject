package com.example.foodapp.data.model
import java.io.Serializable

// Enum để phân loại món ăn
enum class FoodCategory {
    ALL, FOOD, DRINK, SNACK
}


data class Product(
    val name: String,
    val description: String,
    val price: String,
    val priceValue: Double,
    val imageRes: Int, // Lưu Resource ID (ví dụ: R.drawable.matcha)
    val category: FoodCategory
) : Serializable