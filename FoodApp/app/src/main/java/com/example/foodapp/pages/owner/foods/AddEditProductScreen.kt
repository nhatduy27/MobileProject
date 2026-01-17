package com.example.foodapp.pages.owner.foods

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import coil.compose.AsyncImage
import com.example.foodapp.data.model.owner.product.Product
import java.io.File

/**
 * Màn hình thêm/sửa sản phẩm
 * Hỗ trợ upload ảnh từ gallery
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    initialProduct: Product? = null,
    categories: List<CategoryItem>,
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onSave: (
        name: String,
        description: String,
        price: Double,
        categoryId: String,
        preparationTime: Int,
        imageFile: File?
    ) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val isEditMode = initialProduct != null

    // Form state
    var name by remember(initialProduct) { mutableStateOf(initialProduct?.name ?: "") }
    var description by remember(initialProduct) { mutableStateOf(initialProduct?.description ?: "") }
    var price by remember(initialProduct) { mutableStateOf(initialProduct?.price?.toLong()?.toString() ?: "") }
    var preparationTime by remember(initialProduct) { mutableStateOf(initialProduct?.preparationTime?.toString() ?: "15") }

    // Category selection
    var selectedCategoryId by remember(initialProduct) { mutableStateOf(initialProduct?.categoryId ?: "") }
    var selectedCategoryName by remember(initialProduct) {
        mutableStateOf(
            initialProduct?.categoryName ?: categories.firstOrNull()?.name ?: ""
        )
    }

    // Image handling
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    val existingImageUrl = initialProduct?.imageUrl

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Copy to temp file for upload
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = File.createTempFile("product_image", ".jpg", context.cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                imageFile = tempFile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Validation
    val isFormValid = name.isNotBlank() &&
            description.isNotBlank() &&
            price.isNotBlank() &&
            (price.toDoubleOrNull() ?: 0.0) >= 1000 &&
            selectedCategoryId.isNotBlank() &&
            (preparationTime.toIntOrNull() ?: 0) >= 5 &&
            (isEditMode || imageFile != null) // Image required for new products

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Sửa sản phẩm" else "Thêm sản phẩm",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Hình ảnh sản phẩm",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            if (!isEditMode) {
                                Text(
                                    "* Bắt buộc",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF44336)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                selectedImageUri != null -> {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                !existingImageUrl.isNullOrBlank() -> {
                                    AsyncImage(
                                        model = existingImageUrl,
                                        contentDescription = "Product image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.AddPhotoAlternate,
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

                            // Change button overlay
                            if (selectedImageUri != null || !existingImageUrl.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .background(Color(0xFFFF6B35), RoundedCornerShape(8.dp))
                                        .clickable { imagePickerLauncher.launch("image/*") }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Đổi ảnh",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Product Information Card
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
                            "Thông tin sản phẩm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Tên sản phẩm *") },
                            placeholder = { Text("VD: Cơm sườn nướng") },
                            leadingIcon = {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFFFF6B35))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            isError = name.isBlank() && name.isNotEmpty()
                        )

                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Mô tả *") },
                            placeholder = { Text("VD: Cơm sườn nướng mật ong kèm trứng ốp la") },
                            leadingIcon = {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFFF6B35))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            maxLines = 4
                        )

                        // Category Dropdown
                        var expandedCategory by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedCategory,
                            onExpandedChange = { expandedCategory = !expandedCategory }
                        ) {
                            OutlinedTextField(
                                value = selectedCategoryName,
                                onValueChange = {},
                                label = { Text("Danh mục *") },
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
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategoryId = category.id ?: ""
                                            selectedCategoryName = category.name
                                            expandedCategory = false
                                        }
                                    )
                                }
                            }
                        }

                        // Price
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it.filter { c -> c.isDigit() } },
                            label = { Text("Giá (đ) *") },
                            placeholder = { Text("VD: 35000") },
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
                            ),
                            supportingText = {
                                if (price.isNotBlank() && (price.toDoubleOrNull() ?: 0.0) < 1000) {
                                    Text("Giá tối thiểu 1,000đ", color = Color(0xFFF44336))
                                }
                            }
                        )

                        // Preparation Time
                        OutlinedTextField(
                            value = preparationTime,
                            onValueChange = { preparationTime = it.filter { c -> c.isDigit() } },
                            label = { Text("Thời gian chuẩn bị (phút) *") },
                            placeholder = { Text("VD: 15") },
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFFF6B35))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            supportingText = {
                                if (preparationTime.isNotBlank() && (preparationTime.toIntOrNull() ?: 0) < 5) {
                                    Text("Tối thiểu 5 phút", color = Color(0xFFF44336))
                                }
                            }
                        )
                    }
                }
            }

            // Bottom Save Button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        if (isFormValid) {
                            onSave(
                                name,
                                description,
                                price.toDouble(),
                                selectedCategoryId,
                                preparationTime.toInt(),
                                imageFile
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35),
                        disabledContainerColor = Color(0xFFBDBDBD)
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    enabled = isFormValid && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang xử lý...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditMode) "Lưu thay đổi" else "Thêm sản phẩm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
