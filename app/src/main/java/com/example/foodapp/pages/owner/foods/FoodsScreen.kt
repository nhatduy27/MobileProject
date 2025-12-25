package com.example.foodapp.pages.owner.foods

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FoodsScreen() {
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var showAddDialog by remember { mutableStateOf(false) }
    var foodList by remember { mutableStateOf(listOf(
        Food(1, "Cơm gà xối mỡ", "Cơm", "Món chính", 4.8, 156, 45000, true),
        Food(2, "Phở bò", "Phở/Bún", "Món chính", 4.7, 203, 50000, true),
        Food(3, "Bún chả Hà Nội", "Phở/Bún", "Món chính", 4.9, 178, 55000, true),
        Food(4, "Trà sữa trân châu", "Đồ uống", "Thức uống", 4.6, 142, 25000, true),
        Food(5, "Cơm tấm sườn bì", "Cơm", "Món chính", 4.5, 89, 40000, false),
        Food(6, "Cafe sữa đá", "Đồ uống", "Thức uống", 4.4, 95, 20000, true),
        Food(7, "Bánh mì thịt nướng", "Ăn vặt", "Ăn vặt", 4.3, 67, 30000, true),
        Food(8, "Gỏi cuốn tôm thịt", "Ăn vặt", "Ăn vặt", 4.6, 120, 35000, true),
    )) }

    val categories = listOf("Tất cả", "Cơm", "Phở/Bún", "Đồ uống", "Ăn vặt")

    // Lọc thực đơn theo category được chọn
    val filteredFoods = if (selectedCategory == "Tất cả") {
        foodList
    } else {
        foodList.filter { it.category == selectedCategory }
    }

    val totalFoods = foodList.size
    val availableFoods = foodList.count { it.isAvailable }
    val outOfStockFoods = foodList.count { !it.isAvailable }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        FoodsHeader(
            onAddClick = { showAddDialog = true }
        )

        // Show Add Food Dialog
        if (showAddDialog) {
            AddFoodDialog(
                onDismiss = { showAddDialog = false },
                onAddFood = { newFood ->
                    foodList = foodList + newFood
                    showAddDialog = false
                }
            )
        }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredFoods) { food ->
                FoodItem(food = food)
            }
        }
    }
}

@Composable
fun FoodsHeader(onAddClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFF6B35))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            // Add Food Button
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFFF6B35)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Thêm",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
