package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodapp.data.model.owner.buyer.BuyerTier

/**
 * Màn hình thêm / xem chi tiết khách hàng với đầy đủ các trường thông tin.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    onBack: () -> Unit,
    customerId: String? = null,
    isReadOnly: Boolean = false,
    customerViewModel: CustomerViewModel = viewModel(),
    onSave: (name: String, phone: String, type: String, avatar: String, orders: String, revenue: String) -> Unit = { _,_,_,_,_,_ -> }
) {
    val uiState by customerViewModel.uiState.collectAsState()
    val buyerDetail = uiState.selectedBuyer
    
    // Load buyer detail when customerId is present
    LaunchedEffect(customerId) {
        if (customerId != null) {
            customerViewModel.loadBuyerDetail(customerId)
        }
    }
    
    // Clear on exit
    DisposableEffect(Unit) {
        onDispose {
            customerViewModel.clearSelectedBuyer()
        }
    }
    
    // --- STATE QUẢN LÝ DỮ LIỆU ---
    var name by remember(buyerDetail) { mutableStateOf(buyerDetail?.displayName ?: "") }
    var phone by remember(buyerDetail) { mutableStateOf(buyerDetail?.phone ?: "") }
    var avatarUrl by remember(buyerDetail) { mutableStateOf(buyerDetail?.avatar ?: "") }

    // Dropdown Type
    val customerTypes = listOf("Mới", "Thường xuyên", "VIP")
    var selectedType by remember(buyerDetail) { 
        mutableStateOf(
            when (buyerDetail?.tier) {
                BuyerTier.VIP -> "VIP"
                BuyerTier.NORMAL -> "Thường xuyên"
                BuyerTier.NEW -> "Mới"
                null -> customerTypes[0]
            }
        ) 
    }
    var expandedType by remember { mutableStateOf(false) }

    var ordersInfo by remember(buyerDetail) { mutableStateOf("${buyerDetail?.totalOrders ?: 0} đơn") }
    var revenueInfo by remember(buyerDetail) { mutableStateOf(buyerDetail?.totalSpentFormatted ?: "0đ") }

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        bottomBar = {
            if (!isReadOnly) {
                Button(
                    onClick = { onSave(name, phone, selectedType, avatarUrl, ordersInfo, revenueInfo); onBack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                ) {
                    Text("Lưu khách hàng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        // Loading state for detail
        if (uiState.isLoadingDetail && customerId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF6B35))
            }
        }
        // Error state
        else if (uiState.errorMessage != null && customerId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "Đã xảy ra lỗi",
                        color = Color(0xFFE53935),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                    ) {
                        Text("Quay lại")
                    }
                }
            }
        }
        // Content
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- 1. AVATAR UPLOAD SECTION ---
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .then(
                            if (!isReadOnly) Modifier.clickable { /* TODO: Mở thư viện ảnh */ } else Modifier
                        )
                ) {
                    if (avatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar Placeholder",
                            tint = Color.Gray,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
                Text("Chạm để đổi ảnh đại diện", fontSize = 12.sp, color = Color.Gray)

                // --- 2. FORM FIELDS ---

                // Tên khách hàng
                CustomTextField(
                    label = "Tên khách hàng",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nhập tên đầy đủ",
                    enabled = !isReadOnly
                )

                // Số điện thoại (Contact)
                CustomTextField(
                    label = "Số điện thoại",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "09xx xxx xxx",
                    keyboardType = KeyboardType.Phone,
                    enabled = !isReadOnly
                )

                // Loại khách hàng (Dropdown)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        label = { Text("Loại khách hàng") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                "Select type",
                                Modifier.clickable(enabled = !isReadOnly) { if (!isReadOnly) expandedType = true }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B35),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    if (!isReadOnly) {
                        DropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            customerTypes.forEach { type ->
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
                }

                // Avatar URL
                CustomTextField(
                    label = "Link Avatar (Tùy chọn)",
                    value = avatarUrl,
                    onValueChange = { avatarUrl = it },
                    placeholder = "https://...",
                    enabled = !isReadOnly
                )

                HorizontalDivider(color = Color(0xFFEEEEEE))

                Text(
                    "Thông tin lịch sử (Khởi tạo)",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                // Orders Info
                CustomTextField(
                    label = "Tổng đơn hàng",
                    value = ordersInfo,
                    onValueChange = { ordersInfo = it },
                    placeholder = "Ví dụ: 5 đơn",
                    enabled = !isReadOnly
                )

                // Revenue Info
                CustomTextField(
                    label = "Tổng chi tiêu",
                    value = revenueInfo,
                    onValueChange = { revenueInfo = it },
                    placeholder = "Ví dụ: 1.200.000đ",
                    keyboardType = KeyboardType.Number,
                    enabled = !isReadOnly
                )

                // Recent Orders (nếu có)
                if (buyerDetail != null && buyerDetail.recentOrders.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    
                    Text(
                        "Đơn hàng gần đây",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    
                    buyerDetail.recentOrders.forEach { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "#${order.orderNumber}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = order.statusText,
                                        fontSize = 12.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                                Text(
                                    text = order.totalFormatted,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B35)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

// --- COMPONENT CON TÁI SỬ DỤNG ---
@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF424242),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFBDBDBD)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF6B35),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
