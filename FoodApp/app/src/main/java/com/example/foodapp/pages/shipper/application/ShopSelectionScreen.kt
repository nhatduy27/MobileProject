package com.example.foodapp.pages.shipper.application

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodapp.data.model.shipper.application.ShopForApplication
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopSelectionScreen(
    onApplicationSubmitted: () -> Unit = {},
    viewModel: ShopSelectionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Error handling
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    // Success handling
    LaunchedEffect(uiState.applySuccess) {
        if (uiState.applySuccess) {
            Toast.makeText(context, "Đã gửi đơn xin làm shipper thành công!", Toast.LENGTH_LONG).show()
            viewModel.clearSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn cửa hàng") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Tìm kiếm cửa hàng...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            // Pending applications banner
            if (uiState.hasPendingApplication) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Đang chờ phê duyệt",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                "Bạn đã gửi đơn và đang chờ cửa hàng phê duyệt",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF795548)
                            )
                        }
                    }
                }
            }
            
            // Info text
            Text(
                text = "Chọn một cửa hàng để đăng ký làm shipper",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Shops list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.shops.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.StoreMallDirectory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Không tìm thấy cửa hàng nào",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.shops) { shop ->
                        ShopCard(
                            shop = shop,
                            isApplied = uiState.myApplications.any { it.shopId == shop.id },
                            onClick = { viewModel.onShopSelected(shop) }
                        )
                    }
                }
            }
        }
    }
    
    // Apply Dialog
    if (uiState.showApplyDialog && uiState.selectedShop != null) {
        ApplyShipperDialog(
            shop = uiState.selectedShop!!,
            isApplying = uiState.isApplying,
            onDismiss = { viewModel.onDismissApplyDialog() },
            onApply = { vehicleType, vehicleNumber, idCardNumber, message, front, back, license ->
                viewModel.applyToShop(
                    vehicleType, vehicleNumber, idCardNumber, message,
                    front, back, license
                )
            }
        )
    }
}

@Composable
fun ShopCard(
    shop: ShopForApplication,
    isApplied: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = !isApplied, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isApplied) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shop logo
            AsyncImage(
                model = shop.logoUrl ?: shop.coverImageUrl,
                contentDescription = shop.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = shop.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (shop.isOpen) {
                        Surface(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Mở cửa",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (shop.address != null) {
                    Text(
                        text = shop.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (shop.rating != null && shop.rating > 0) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", shop.rating),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (shop.totalRatings != null && shop.totalRatings > 0) {
                            Text(
                                text = " (${shop.totalRatings})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (isApplied) {
                        Text(
                            "Đã gửi đơn",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            if (!isApplied) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Chọn",
                    tint = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyShipperDialog(
    shop: ShopForApplication,
    isApplying: Boolean,
    onDismiss: () -> Unit,
    onApply: (
        vehicleType: String,
        vehicleNumber: String,
        idCardNumber: String,
        message: String?,
        idCardFront: File,
        idCardBack: File,
        driverLicense: File
    ) -> Unit
) {
    val context = LocalContext.current
    
    var vehicleType by remember { mutableStateOf("MOTORBIKE") }
    var vehicleNumber by remember { mutableStateOf("") }
    var idCardNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    var idCardFrontUri by remember { mutableStateOf<Uri?>(null) }
    var idCardBackUri by remember { mutableStateOf<Uri?>(null) }
    var driverLicenseUri by remember { mutableStateOf<Uri?>(null) }
    
    val idCardFrontLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> idCardFrontUri = uri }
    
    val idCardBackLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> idCardBackUri = uri }
    
    val driverLicenseLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> driverLicenseUri = uri }
    
    fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
    
    AlertDialog(
        onDismissRequest = { if (!isApplying) onDismiss() },
        title = { 
            Text(
                "Đăng ký làm shipper",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Cửa hàng: ${shop.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Vehicle Type
                Text("Loại xe", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = vehicleType == "MOTORBIKE",
                        onClick = { vehicleType = "MOTORBIKE" },
                        label = { Text("Xe máy") }
                    )
                    FilterChip(
                        selected = vehicleType == "CAR",
                        onClick = { vehicleType = "CAR" },
                        label = { Text("Ô tô") }
                    )
                    FilterChip(
                        selected = vehicleType == "BICYCLE",
                        onClick = { vehicleType = "BICYCLE" },
                        label = { Text("Xe đạp") }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Vehicle Number
                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = { Text("Biển số xe") },
                    placeholder = { Text("VD: 59X1-12345") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // ID Card Number
                OutlinedTextField(
                    value = idCardNumber,
                    onValueChange = { idCardNumber = it },
                    label = { Text("Số CMND/CCCD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Message
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Lời nhắn (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Ảnh giấy tờ", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                // File upload buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageUploadButton(
                        label = "CMND trước",
                        hasFile = idCardFrontUri != null,
                        onClick = { idCardFrontLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                    ImageUploadButton(
                        label = "CMND sau",
                        hasFile = idCardBackUri != null,
                        onClick = { idCardBackLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ImageUploadButton(
                    label = "Bằng lái xe",
                    hasFile = driverLicenseUri != null,
                    onClick = { driverLicenseLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val frontFile = idCardFrontUri?.let { uriToFile(it) }
                    val backFile = idCardBackUri?.let { uriToFile(it) }
                    val licenseFile = driverLicenseUri?.let { uriToFile(it) }
                    
                    if (vehicleNumber.isBlank() || idCardNumber.isBlank()) {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    if (frontFile == null || backFile == null || licenseFile == null) {
                        Toast.makeText(context, "Vui lòng tải lên đầy đủ 3 ảnh", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    onApply(
                        vehicleType,
                        vehicleNumber,
                        idCardNumber,
                        message.ifBlank { null },
                        frontFile,
                        backFile,
                        licenseFile
                    )
                },
                enabled = !isApplying
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Gửi đơn")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isApplying
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ImageUploadButton(
    label: String,
    hasFile: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (hasFile) Color(0xFFE8F5E9) else Color.Transparent
        )
    ) {
        Icon(
            if (hasFile) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
            contentDescription = null,
            tint = if (hasFile) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
