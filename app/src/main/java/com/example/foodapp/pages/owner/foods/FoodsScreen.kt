package com.example.foodapp.pages.owner.foods

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Màn hình quản lý món ăn - FoodsScreen
 * 
 * Màn hình này hiển thị danh sách món ăn với các tính năng:
 * - Lọc món ăn theo category (Tất cả, Cơm, Phở/Bún, Đồ uống, Ăn vặt)
 * - Hiển thị thống kê: Tổng món, Còn hàng, Hết hàng
 * - Thêm món ăn mới
 * - Xem chi tiết món ăn
 * 
 * Kiến trúc:
 * - FoodsScreen (UI Layer) - Composable chính hiển thị giao diện
 * - FoodsViewModel (Presentation Layer) - Quản lý state và logic nghiệp vụ
 * - MockFoodRepository (Data Layer) - Nguồn dữ liệu
 */
@Composable
fun FoodsScreen(
    viewModel: FoodsViewModel = viewModel()
) {
    // Lắng nghe state từ ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    // Các danh sách categories và filtered foods
    val categories = viewModel.categories
    val filteredFoods = remember(uiState.foods, uiState.selectedCategory) {
        viewModel.getFilteredFoods()
    }
    
    // Statistics
    val totalFoods = remember(uiState.foods) { viewModel.getTotalFoods() }
    val availableFoods = remember(uiState.foods) { viewModel.getAvailableFoods() }
    val outOfStockFoods = remember(uiState.foods) { viewModel.getOutOfStockFoods() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        FoodsHeader(
            onAddClick = { viewModel.showAddDialog() }
        )

        // Show Add Food Dialog
        if (uiState.showAddDialog) {
            AddFoodDialog(
                onDismiss = { viewModel.hideAddDialog() },
                onAddFood = { newFood ->
                    viewModel.addFood(newFood)
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
                    isSelected = uiState.selectedCategory == category,
                    onClick = { viewModel.onCategorySelected(category) }
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
