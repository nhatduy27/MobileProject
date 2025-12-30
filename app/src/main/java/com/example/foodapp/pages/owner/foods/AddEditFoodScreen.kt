package com.example.foodapp.pages.owner.foods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import coil.compose.AsyncImage
import com.example.foodapp.data.model.owner.Food

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(
    initialFood: Food? = null,
    onBack: () -> Unit,
    onSave: (Food) -> Unit
) {
    var foodName by remember(initialFood) { mutableStateOf(initialFood?.name ?: "") }
    var selectedCategory by remember(initialFood) { mutableStateOf(initialFood?.category ?: "Cơm") }
    var selectedType by remember(initialFood) { mutableStateOf(initialFood?.type ?: "Món chính") }
    var foodPrice by remember(initialFood) { mutableStateOf(if (initialFood != null) initialFood.price.toString() else "") }
    var isAvailable by remember(initialFood) { mutableStateOf(initialFood?.isAvailable ?: true) }
    var imageUrl by remember(initialFood) { mutableStateOf(initialFood?.imageUrl ?: "") }

    val categories = listOf("Cơm", "Phở/Bún", "Đồ uống", "Ăn vặt")
    val types = listOf("Món chính", "Thức uống", "Ăn vặt")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (initialFood == null) "Thêm món ăn" else "Chỉnh sửa món ăn",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9F9F9),
        bottomBar = {
            Button(
                onClick = {
                    if (foodName.isNotBlank() && foodPrice.isNotBlank()) {
                        val priceValue = foodPrice.toIntOrNull() ?: 0
                        val food = Food(
                            id = initialFood?.id ?: System.currentTimeMillis().toInt(),
                            name = foodName,
                            category = selectedCategory,
                            type = selectedType,
                            rating = initialFood?.rating ?: 4.5,
                            reviewCount = initialFood?.reviewCount ?: 0,
                            price = priceValue,
                            isAvailable = isAvailable,
                            imageUrl = imageUrl.ifBlank { null }
                        )
                        onSave(food)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
            ) {
                Text(
                    text = if (initialFood == null) "Thêm món" else "Lưu thay đổi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ảnh món ăn
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5))
                    .clickable { /* TODO: mở thư viện ảnh hoặc picker */ },
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Ảnh món ăn",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                        Text("Chạm để chọn / đổi ảnh", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Tên món ăn") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Link ảnh (tùy chọn)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            var expandedCategory by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Danh mục") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            var expandedType by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    label = { Text("Loại") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    types.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                expandedType = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = foodPrice,
                onValueChange = { foodPrice = it },
                label = { Text("Giá (đ)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Còn hàng", fontSize = 14.sp)
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it }
                )
            }
        }
    }
}
