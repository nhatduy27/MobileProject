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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodapp.data.model.shipper.application.ShopForApplication
import com.example.foodapp.pages.shipper.theme.ShipperColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopSelectionScreen(
    onBack: () -> Unit = {},
    onApplicationSubmitted: () -> Unit = {},
    viewModel: ShopSelectionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.applySuccess) {
        if (uiState.applySuccess) {
            Toast.makeText(context, "Đã gửi đơn xin làm shipper thành công!", Toast.LENGTH_LONG).show()
            viewModel.clearSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Chọn cửa hàng",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = ShipperColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShipperColors.Surface,
                    titleContentColor = ShipperColors.TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ShipperColors.Background)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Tìm kiếm cửa hàng...") },
                leadingIcon = { 
                    Icon(
                        Icons.Outlined.Search, 
                        contentDescription = null,
                        tint = ShipperColors.TextSecondary
                    ) 
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShipperColors.Primary,
                    unfocusedBorderColor = ShipperColors.Divider,
                    focusedContainerColor = ShipperColors.Surface,
                    unfocusedContainerColor = ShipperColors.Surface
                )
            )
            
            // Pending applications banner
            if (uiState.hasPendingApplication) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = ShipperColors.WarningLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.HourglassEmpty,
                            contentDescription = null,
                            tint = ShipperColors.Warning
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Đang chờ phê duyệt",
                                fontWeight = FontWeight.SemiBold,
                                color = ShipperColors.Warning
                            )
                            Text(
                                "Bạn đã gửi đơn và đang chờ cửa hàng phê duyệt",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShipperColors.TextSecondary
                            )
                        }
                    }
                }
            }
            
            // Info text
            Text(
                text = "Chọn một cửa hàng để đăng ký làm shipper",
                style = MaterialTheme.typography.bodyMedium,
                color = ShipperColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Shops list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ShipperColors.Primary)
                }
            } else if (uiState.shops.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = ShipperColors.TextTertiary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Không tìm thấy cửa hàng nào",
                            color = ShipperColors.TextSecondary
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = !isApplied, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isApplied) ShipperColors.SuccessLight else ShipperColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shop logo
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(10.dp),
                color = ShipperColors.PrimaryLight
            ) {
                val imageUrl = shop.logoUrl ?: shop.coverImageUrl
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = shop.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Store,
                            contentDescription = null,
                            tint = ShipperColors.Primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = shop.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ShipperColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (shop.isOpen) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = ShipperColors.Success.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Mở cửa",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShipperColors.Success,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                if (shop.address != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shop.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = ShipperColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (shop.rating != null && shop.rating > 0) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            tint = ShipperColors.Warning,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", shop.rating),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = ShipperColors.TextPrimary
                        )
                        
                        if (shop.totalRatings != null && shop.totalRatings > 0) {
                            Text(
                                text = " (${shop.totalRatings})",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShipperColors.TextSecondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (isApplied) {
                        Text(
                            "Đã gửi đơn",
                            style = MaterialTheme.typography.labelSmall,
                            color = ShipperColors.Success,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            if (!isApplied) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = "Chọn",
                    tint = ShipperColors.TextTertiary
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
                fontWeight = FontWeight.SemiBold
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
                    fontWeight = FontWeight.Medium,
                    color = ShipperColors.Primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Vehicle Type
                Text(
                    "Loại xe", 
                    style = MaterialTheme.typography.labelMedium,
                    color = ShipperColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = vehicleType == "MOTORBIKE",
                        onClick = { vehicleType = "MOTORBIKE" },
                        label = { Text("Xe máy") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ShipperColors.PrimaryLight,
                            selectedLabelColor = ShipperColors.Primary
                        )
                    )
                    FilterChip(
                        selected = vehicleType == "CAR",
                        onClick = { vehicleType = "CAR" },
                        label = { Text("Ô tô") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ShipperColors.PrimaryLight,
                            selectedLabelColor = ShipperColors.Primary
                        )
                    )
                    FilterChip(
                        selected = vehicleType == "BICYCLE",
                        onClick = { vehicleType = "BICYCLE" },
                        label = { Text("Xe đạp") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ShipperColors.PrimaryLight,
                            selectedLabelColor = ShipperColors.Primary
                        )
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShipperColors.Primary,
                        unfocusedBorderColor = ShipperColors.Divider
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // ID Card Number
                OutlinedTextField(
                    value = idCardNumber,
                    onValueChange = { idCardNumber = it },
                    label = { Text("Số CMND/CCCD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShipperColors.Primary,
                        unfocusedBorderColor = ShipperColors.Divider
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Message
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Lời nhắn (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShipperColors.Primary,
                        unfocusedBorderColor = ShipperColors.Divider
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Ảnh giấy tờ", 
                    style = MaterialTheme.typography.labelMedium,
                    color = ShipperColors.TextSecondary
                )
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
                enabled = !isApplying,
                colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ShipperColors.Surface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Gửi đơn", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isApplying
            ) {
                Text("Hủy", color = ShipperColors.TextSecondary)
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
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (hasFile) ShipperColors.SuccessLight else ShipperColors.Surface
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (hasFile) ShipperColors.Success else ShipperColors.Divider
            )
        )
    ) {
        Icon(
            if (hasFile) Icons.Outlined.CheckCircle else Icons.Outlined.AddPhotoAlternate,
            contentDescription = null,
            tint = if (hasFile) ShipperColors.Success else ShipperColors.Primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = if (hasFile) ShipperColors.Success else ShipperColors.TextPrimary
        )
    }
}
