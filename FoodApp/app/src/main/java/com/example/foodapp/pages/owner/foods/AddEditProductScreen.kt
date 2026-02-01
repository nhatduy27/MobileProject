package com.example.foodapp.pages.owner.foods

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
 * Sealed class để đại diện cho một ảnh trong gallery
 * - ExistingImage: Ảnh đã có trên server (URL)
 * - NewImage: Ảnh mới được chọn từ device (URI + File)
 */
sealed class ProductImage {
    data class ExistingImage(val url: String) : ProductImage()
    data class NewImage(val uri: Uri, val file: File) : ProductImage()
    
    val displayUrl: String
        get() = when (this) {
            is ExistingImage -> url
            is NewImage -> uri.toString()
        }
}

/**
 * Màn hình thêm/sửa sản phẩm
 * Hỗ trợ upload NHIỀU ẢNH từ gallery
 * - Ảnh đầu tiên là ảnh chính
 * - Có thể thêm/xóa ảnh
 * - Tối đa 10 ảnh
 * - Khi edit: Giữ ảnh cũ + thêm ảnh mới
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
        imageFiles: List<File>
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

    // Image handling - unified list containing both existing and new images
    var productImages by remember(initialProduct) { 
        mutableStateOf<List<ProductImage>>(
            initialProduct?.imageUrls?.map { ProductImage.ExistingImage(it) } ?: emptyList()
        )
    }

    // Helper function to convert URI to File
    fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile(
                "product_image_${System.currentTimeMillis()}", 
                ".jpg", 
                context.cacheDir
            )
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Multi-image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val remainingSlots = 10 - productImages.size
            val urisToAdd = uris.take(remainingSlots)
            
            val newImages = urisToAdd.mapNotNull { uri ->
                uriToFile(uri)?.let { file ->
                    ProductImage.NewImage(uri, file)
                }
            }
            
            productImages = productImages + newImages
        }
    }

    // Single image picker (for adding one image at a time)
    val singleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (productImages.size < 10) {
                uriToFile(it)?.let { file ->
                    productImages = productImages + ProductImage.NewImage(it, file)
                }
            }
        }
    }

    // Get only the new image files to upload
    val newImageFiles = productImages.filterIsInstance<ProductImage.NewImage>().map { it.file }

    // Validation
    val hasImages = productImages.isNotEmpty()
    val isFormValid = name.isNotBlank() &&
            description.isNotBlank() &&
            price.isNotBlank() &&
            (price.toDoubleOrNull() ?: 0.0) >= 1000 &&
            selectedCategoryId.isNotBlank() &&
            (preparationTime.toIntOrNull() ?: 0) >= 5 &&
            (isEditMode || hasImages) // Images required for new products

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
                // Image Card - Hỗ trợ nhiều ảnh
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
                            Column {
                                Text(
                                    "Hình ảnh sản phẩm",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Text(
                                    "Ảnh đầu tiên sẽ là ảnh chính (tối đa 10 ảnh)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            if (!isEditMode) {
                                Text(
                                    "* Bắt buộc",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF44336)
                                )
                            }
                        }

                        // Image Gallery - Horizontal scroll
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Display all images (existing + new)
                            productImages.forEachIndexed { index, productImage ->
                                ImageCard(
                                    imageSource = productImage.displayUrl,
                                    isMainImage = index == 0,
                                    isExisting = productImage is ProductImage.ExistingImage,
                                    onRemove = {
                                        productImages = productImages.filterIndexed { i, _ -> i != index }
                                    }
                                )
                            }

                            // Add image button
                            if (productImages.size < 10) {
                                AddImageCard(
                                    onClick = { singleImagePickerLauncher.launch("image/*") }
                                )
                            }
                        }

                        // Quick action buttons
                        if (productImages.isEmpty()) {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFFF6B35)
                                )
                            ) {
                                Icon(Icons.Default.Collections, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Chọn nhiều ảnh từ thư viện")
                            }
                        }

                        // Info text for edit mode
                        if (isEditMode && newImageFiles.isNotEmpty()) {
                            Text(
                                "📷 Đã thêm ${newImageFiles.size} ảnh mới",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
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
                                newImageFiles  // Only send NEW files
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

/**
 * Card hiển thị 1 ảnh trong gallery
 */
@Composable
private fun ImageCard(
    imageSource: String,
    isMainImage: Boolean,
    isExisting: Boolean = false,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isMainImage) 2.dp else 1.dp,
                color = if (isMainImage) Color(0xFFFF6B35) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        AsyncImage(
            model = imageSource,
            contentDescription = "Product image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Main image badge
        if (isMainImage) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .background(Color(0xFFFF6B35), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "Ảnh chính",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Existing image badge (small indicator)
        if (isExisting && !isMainImage) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .background(Color(0xFF2196F3), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    "Cũ",
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove image",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Card để thêm ảnh mới
 */
@Composable
private fun AddImageCard(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add image",
                tint = Color(0xFFFF6B35),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Thêm ảnh",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }
    }
}
