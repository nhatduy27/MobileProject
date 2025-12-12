package com.example.foodapp.pages.foods

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Food(
    val id: Int,
    val name: String,
    val category: String,
    val type: String,
    val rating: Double,
    val reviewCount: Int,
    val price: Int,
    val isAvailable: Boolean
)
@Composable
fun FoodsScreen() {
    var selectedCategory by remember { mutableStateOf("Tất cả") }

    val categories = listOf("Tất cả", "Cơm", "Phở/Bún", "Đồ uống", "Ăn vặt")

    val foods = listOf(
        Food(1, "Cơm gà xối mỡ", "Cơm", "Món chính", 4.8, 156, 45000, true),
        Food(2, "Phở bò", "Phở/Bún", "Món chính", 4.7, 203, 50000, true),
        Food(3, "Bún chả Hà Nội", "Phở/Bún", "Món chính", 4.9, 178, 55000, true),
        Food(4, "Trà sữa trân châu", "Đồ uống", "Thức uống", 4.6, 142, 25000, true),
        Food(5, "Cơm tấm sườn bì", "Cơm", "Món chính", 4.5, 89, 40000, false),
        Food(6, "Cafe sữa đá", "Đồ uống", "Thức uống", 4.4, 95, 20000, true)
    )

    val totalFoods = foods.size
    val availableFoods = foods.count { it.isAvailable }
    val outOfStockFoods = foods.count { !it.isAvailable }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        FoodsHeader()

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }

        // Statistics Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(title = "Tổng món", value = totalFoods.toString(), color = Color(0xFFFF6B35))
            StatCard(title = "Còn hàng", value = availableFoods.toString(), color = Color(0xFF4CAF50))
            StatCard(title = "Hết hàng", value = outOfStockFoods.toString(), color = Color(0xFFF44336))
        }

        // Foods List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            foods.forEach { food ->
                FoodItem(food = food)
            }
        }
    }
}

@Composable
fun FoodsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF6B35))
            .padding(20.dp)
    ) {
        Text(
            text = "Quản lý món ăn",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Quản lý thực đơn và giá cả",
            fontSize = 14.sp,
            color = Color(0xFFFFE5D9),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
