package com.example.foodapp.pages.owner.foods

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.*
import coil.compose.AsyncImage
import com.example.foodapp.data.model.shared.food.Food

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(
    initialFood: Food? = null,
    onBack: () -> Unit,
    onSave: (Food) -> Unit
) {
    BackHandler { onBack() }
    
    var foodName by remember(initialFood) { mutableStateOf(initialFood?.name ?: "") }
    var selectedCategory by remember(initialFood) { mutableStateOf(initialFood?.category ?: "Cơm") }
    var selectedType by remember(initialFood) { mutableStateOf(initialFood?.type ?: "Món chính") }
    var foodPrice by remember(initialFood) { mutableStateOf(if (initialFood != null) initialFood.price.toString() else "") }
    var isAvailable by remember(initialFood) { mutableStateOf(initialFood?.isAvailable ?: true) }
    var imageUrl by remember(initialFood) { mutableStateOf(initialFood?.imageUrl ?: "") }

    val categories = listOf("Cơm", "Phở/Bún", "Đồ uống", "Ăn vặt")
    val types = listOf("Món chính", "Thức uống", "Ăn vặt")

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Hình ảnh món ăn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5))
                                .clickable { /* TODO: open image picker */ },
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
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B35),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Chạm để chọn ảnh",
                                        fontSize = 14.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Link ảnh (tùy chọn)") },
                            placeholder = { Text("https://...") },
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFFFF6B35))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }

                // Food Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Thông tin món ăn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            label = { Text("Tên món ăn") },
                            leadingIcon = {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFFFF6B35))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
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
                                leadingIcon = {
                                    Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFFFF6B35))
                                },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF6B35),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
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
                                leadingIcon = {
                                    Icon(Icons.Default.LocalDining, contentDescription = null, tint = Color(0xFFFF6B35))
                                },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF6B35),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
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
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFFFF6B35))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Còn hàng",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1A1A)
                            )
                            Switch(
                                checked = isAvailable,
                                onCheckedChange = { isAvailable = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFF6B35)
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Save Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 0.dp
            ) {
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
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(
                        imageVector = if (initialFood == null) Icons.Default.Add else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialFood == null) "Thêm món ăn" else "Lưu thay đổi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
