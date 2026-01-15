package com.example.foodapp.pages.owner.shopmanagement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodapp.ui.theme.PrimaryOrange
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopManagementScreen(navController: androidx.navigation.NavHostController? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ShopManagementViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ShopManagementViewModel(context) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    
    // Auto-clear messages
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            delay(3000)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(2000)
            viewModel.clearSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quản lý cửa hàng",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryOrange,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Error Message
                if (uiState.errorMessage != null) {
                    ErrorCard(message = uiState.errorMessage!!)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Success Message
                if (uiState.successMessage != null) {
                    SuccessCard(message = uiState.successMessage!!)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Basic Information Section
                SectionCard(title = "Thông tin cơ bản") {
                    ShopTextField(
                        value = uiState.shopName,
                        onValueChange = viewModel::updateShopName,
                        label = "Tên cửa hàng",
                        icon = Icons.Default.Store,
                        error = uiState.shopNameError,
                        placeholder = "VD: Quán Phở Việt"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ShopTextField(
                        value = uiState.description,
                        onValueChange = viewModel::updateDescription,
                        label = "Mô tả",
                        icon = Icons.Default.Description,
                        error = uiState.descriptionError,
                        placeholder = "Mô tả ngắn về cửa hàng của bạn",
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ShopTextField(
                        value = uiState.address,
                        onValueChange = viewModel::updateAddress,
                        label = "Địa chỉ",
                        icon = Icons.Default.LocationOn,
                        error = uiState.addressError,
                        placeholder = "VD: Tòa A, Tầng 1, KTX ĐHQG"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ShopTextField(
                        value = uiState.phone,
                        onValueChange = viewModel::updatePhone,
                        label = "Số điện thoại",
                        icon = Icons.Default.Phone,
                        error = uiState.phoneError,
                        placeholder = "0901234567",
                        keyboardType = KeyboardType.Phone
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Operating Hours Section
                SectionCard(title = "Giờ hoạt động") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ShopTextField(
                            value = uiState.openTime,
                            onValueChange = viewModel::updateOpenTime,
                            label = "Giờ mở cửa",
                            icon = Icons.Default.AccessTime,
                            error = uiState.openTimeError,
                            placeholder = "07:00",
                            modifier = Modifier.weight(1f)
                        )
                        
                        ShopTextField(
                            value = uiState.closeTime,
                            onValueChange = viewModel::updateCloseTime,
                            label = "Giờ đóng cửa",
                            icon = Icons.Default.AccessTime,
                            error = uiState.closeTimeError,
                            placeholder = "21:00",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pricing Section
                SectionCard(title = "Chính sách giá") {
                    ShopTextField(
                        value = uiState.shipFee,
                        onValueChange = viewModel::updateShipFee,
                        label = "Phí ship mỗi đơn (đ)",
                        icon = Icons.Default.DeliveryDining,
                        error = uiState.shipFeeError,
                        placeholder = "5000",
                        keyboardType = KeyboardType.Number
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ShopTextField(
                        value = uiState.minOrderAmount,
                        onValueChange = viewModel::updateMinOrderAmount,
                        label = "Đơn hàng tối thiểu (đ)",
                        icon = Icons.Default.ShoppingCart,
                        error = uiState.minOrderAmountError,
                        placeholder = "20000",
                        keyboardType = KeyboardType.Number
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Images Section
                SectionCard(title = "Hình ảnh cửa hàng") {
                    UpdateImageCard(
                        label = "Ảnh bìa",
                        currentImageUrl = uiState.coverImageUrl,
                        newImageUri = uiState.newCoverImageUri,
                        onImageSelected = viewModel::updateCoverImage
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    UpdateImageCard(
                        label = "Logo",
                        currentImageUrl = uiState.logoUrl,
                        newImageUri = uiState.newLogoUri,
                        onImageSelected = viewModel::updateLogo
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Save Button
                Button(
                    onClick = { viewModel.updateShop {} },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOrange,
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Lưu thay đổi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (error != null) Color.Red else PrimaryOrange
                )
            },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            maxLines = maxLines,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryOrange,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                errorBorderColor = Color.Red,
                focusedLabelColor = PrimaryOrange,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun UpdateImageCard(
    label: String,
    currentImageUrl: String,
    newImageUri: android.net.Uri?,
    onImageSelected: (android.net.Uri?) -> Unit
) {
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        onImageSelected(uri)
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            
            if (newImageUri != null) {
                TextButton(onClick = { onImageSelected(null) }) {
                    Text("Hủy", color = Color.Red)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clickable { launcher.launch("image/*") },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    newImageUri != null -> {
                        // Show new selected image
                        AsyncImage(
                            model = newImageUri,
                            contentDescription = label,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Badge to indicate new image
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(PrimaryOrange, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Mới",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    currentImageUrl.isNotEmpty() -> {
                        // Show current image from URL
                        AsyncImage(
                            model = currentImageUrl,
                            contentDescription = label,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        // No image
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Chọn $label",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFFD32F2F),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SuccessCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF388E3C),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFF388E3C),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
