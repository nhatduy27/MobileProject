package com.example.foodapp.pages.owner.foods

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight

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
    val filteredFoods = remember(uiState.foods, uiState.selectedCategory, uiState.searchQuery) {
        viewModel.getFilteredFoods()
    }

    var isEditing by remember { mutableStateOf(false) }
    var editingFood by remember { mutableStateOf<com.example.foodapp.data.model.owner.Food?>(null) }

    if (!isEditing) {
        // Màn hình danh sách món ăn
        // Statistics
        val totalFoods = remember(uiState.foods) { viewModel.getTotalFoods() }
        val availableFoods = remember(uiState.foods) { viewModel.getAvailableFoods() }
        val outOfStockFoods = remember(uiState.foods) { viewModel.getOutOfStockFoods() }

        Scaffold(
            topBar = {
                FoodsSearchHeader(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingFood = null
                        isEditing = true
                    },
                    containerColor = Color(0xFFFF6B35),
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Thêm món")
                }
            },
            containerColor = Color(0xFFF5F5F5)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(
                        bottom = 80.dp // Giữ khoảng cách với cuối màn hình (tránh bị FAB che)
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Statistics Cards ở đầu list để cuộn theo
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(title = "Tổng món", value = totalFoods.toString(), color = Color(0xFFFF6B35))
                            StatCard(title = "Còn hàng", value = availableFoods.toString(), color = Color(0xFF4CAF50))
                            StatCard(title = "Hết hàng", value = outOfStockFoods.toString(), color = Color(0xFFF44336))
                        }
                    }

                    items(filteredFoods) { food ->
                        FoodItem(
                            food = food,
                            onClick = {
                                editingFood = food
                                isEditing = true
                            }
                        )
                    }
                }
            }
        }
    } else {
        // Màn hình thêm / sửa món ăn
        AddEditFoodScreen(
            initialFood = editingFood,
            onBack = {
                isEditing = false
                editingFood = null
            },
            onSave = { food ->
                if (editingFood == null) {
                    viewModel.addFood(food)
                } else {
                    viewModel.updateFood(food)
                }
            }
        )
    }
}

// Thanh tìm kiếm có hiệu ứng giống màn Customer
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
        else focusManager.clearFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // STATE 1: Header bình thường
        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn(tween(300)) + slideInHorizontally(),
            exit = fadeOut(tween(300)) + slideOutHorizontally { -it / 2 }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Món ăn", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Text("Quản lý thực đơn", fontSize = 14.sp, color = Color.Gray)
                }
                IconButton(
                    onClick = { isSearchActive = true },
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF1A1A1A))
                }
            }
        }

        // STATE 2: Thanh Search mở rộng
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End, animationSpec = tween(300)),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End, animationSpec = tween(300))
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isSearchActive = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A1A1A))
                    }
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Tìm món ăn...", color = Color.Gray, fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFFFF6B35)
                        ),
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
