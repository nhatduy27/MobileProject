package com.example.foodapp.pages.owner.shopsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import com.example.foodapp.ui.theme.PrimaryOrange
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopSetupScreen(
    onSetupComplete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ShopSetupViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ShopSetupViewModel(context) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    
    // Auto-clear error message after 3 seconds
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            delay(3000)
            viewModel.clearError()
        }
    }
    
    // Navigate on success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(1000)
            onSetupComplete()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cài đặt cửa hàng",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryOrange,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Section
            ShopSetupHeader()
            
            Spacer(modifier = Modifier.height(24.dp))
            
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
                ImagePickerCard(
                    label = "Ảnh bìa",
                    imageUri = uiState.coverImageUri,
                    error = uiState.coverImageError,
                    onImageSelected = viewModel::updateCoverImage
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ImagePickerCard(
                    label = "Logo",
                    imageUri = uiState.logoUri,
                    error = uiState.logoError,
                    onImageSelected = viewModel::updateLogo
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit Button
            Button(
                onClick = { viewModel.createShop(onSetupComplete) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryOrange,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Hoàn tất cài đặt",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ShopSetupHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryOrange.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                tint = PrimaryOrange,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    "Chào mừng bạn!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryOrange
                )
                Text(
                    "Hãy cung cấp thông tin cửa hàng để bắt đầu",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
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

@Composable
fun ImagePickerCard(
    label: String,
    imageUri: android.net.Uri?,
    error: String?,
    onImageSelected: (android.net.Uri?) -> Unit
) {
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        onImageSelected(uri)
    }
    
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clickable { launcher.launch("image/*") },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (error != null) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)
            ),
            border = if (error != null) androidx.compose.foundation.BorderStroke(1.dp, Color.Red) else null
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    coil.compose.AsyncImage(
                        model = imageUri,
                        contentDescription = label,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = if (error != null) Color.Red else Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chọn $label",
                            color = if (error != null) Color.Red else Color.Gray
                        )
                    }
                }
            }
        }
        
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
